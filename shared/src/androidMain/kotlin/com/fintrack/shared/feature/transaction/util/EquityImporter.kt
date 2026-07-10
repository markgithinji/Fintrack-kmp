package com.fintrack.shared.feature.transaction.util

import android.content.Context
import android.provider.Telephony
import com.fintrack.shared.feature.account.domain.model.AccountType
import com.fintrack.shared.feature.account.domain.repository.AccountRepository
import com.fintrack.shared.feature.core.logger.KMPLogger
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import com.fintrack.shared.feature.transaction.domain.util.TransactionImporter
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

class EquityImporter(
    private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : TransactionImporter {
    private val logger = KMPLogger()

    override suspend fun importHistory(onProgress: (Float) -> Unit): Unit = withContext(Dispatchers.IO) {
        logger.info("SYNC_FLOW", "EquityImporter: importHistory started")
        onProgress(0.05f)
        val accountsResult = accountRepository.getAccounts()
        val accounts = (accountsResult as? Result.Success)?.data ?: emptyList()
        val accountId = accounts.find { it.type == AccountType.EQUITY }?.id
            ?: accounts.find { it.name.lowercase().contains("equity") }?.id
            ?: "equity"

        logger.info("SYNC_FLOW", "Equity account identified as: $accountId")

        // Search for both "EquitBank" and "EquityBank"
        val cursor = context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(Telephony.Sms.Inbox.BODY, Telephony.Sms.Inbox.ADDRESS, Telephony.Sms.Inbox.DATE),
            "${Telephony.Sms.Inbox.ADDRESS} IN (?, ?)",
            arrayOf("EquitBank", "EquityBank"),
            "${Telephony.Sms.Inbox.DATE} DESC"
        )

        cursor?.use {
            val bodyIndex = it.getColumnIndex(Telephony.Sms.Inbox.BODY)
            val dateIndex = it.getColumnIndex(Telephony.Sms.Inbox.DATE)
            val transactions = mutableListOf<Transaction>()
            var latestBalance: Double? = null
            var loggedCount = 0
            val totalMessages = it.count
            
            logger.info("SYNC_FLOW", "Found $totalMessages potential Equity messages")

            while (it.moveToNext()) {
                ensureActive()
                val body = it.getString(bodyIndex)
                val timestamp = it.getLong(dateIndex)
                val smsInstant = Instant.fromEpochMilliseconds(timestamp)

                // Keep the first balance we find (most recent message)
                if (latestBalance == null) {
                    latestBalance = EquityParser.parseBalance(body)
                }

                val transaction = EquityParser.parse(body, accountId, smsInstant)
                if (transaction != null) {
                    transactions.add(transaction)
                    if (loggedCount < 5) {
                        logger.debug("SYNC_FLOW", "Parsed Equity sample: ${transaction.externalId} on ${transaction.dateTime}")
                    }
                }
                
                loggedCount++
                if (loggedCount % 100 == 0) {
                    logger.info("SYNC_FLOW", "Scanning Equity SMS: $loggedCount/$totalMessages processed...")
                    onProgress(0.05f + (loggedCount.toFloat() / totalMessages) * 0.25f)
                }
            }

            logger.info("SYNC_FLOW", "Scanning complete. Found ${transactions.size} valid Equity transactions to upload.")

            if (transactions.isNotEmpty()) {
                onProgress(0.3f)
                val chunks = transactions.chunked(250)
                val totalChunks = chunks.size
                
                logger.info("SYNC_FLOW", "Starting upload of ${transactions.size} Equity transactions in $totalChunks chunks")
                
                var failedBatchCount = 0
                var lastErrorMessage: String? = null

                chunks.forEachIndexed { index, chunk ->
                    ensureActive()
                    logger.debug("SYNC_FLOW", "Uploading Equity chunk ${index + 1}/$totalChunks...")
                    val result = transactionRepository.importEquityTransactions(chunk)
                    if (result is Result.Success) {
                        if (index < 3 || index == totalChunks - 1) {
                           logger.info("SYNC_FLOW", "Successfully uploaded Equity chunk ${index + 1}/$totalChunks")
                        }
                    } else if (result is Result.Error) {
                        failedBatchCount++
                        lastErrorMessage = result.exception.message
                        logger.error("SYNC_FLOW", "Failed to upload Equity chunk ${index + 1}: $lastErrorMessage", result.exception)
                    }
                    onProgress(0.3f + ((index + 1).toFloat() / totalChunks) * 0.6f)
                }

                if (failedBatchCount > 0) {
                    val summary = "Failed to sync $failedBatchCount out of $totalChunks Equity batches. Last error: $lastErrorMessage"
                    logger.error("SYNC_FLOW", summary, null)
                    throw Exception(summary)
                }
            }
            
            logger.info("SYNC_FLOW", "Equity import process completed successfully")

            // Update account balance if we found one
            if (latestBalance != null) {
                logger.info("SYNC_FLOW", "Latest balance from Equity SMS: $latestBalance. Checking for discrepancy...")
                val accountResult = accountRepository.getAccountById(accountId)
                if (accountResult is Result.Success) {
                    val account = accountResult.data
                    val currentAppBalance = account.balance ?: 0.0
                    val discrepancy = latestBalance - currentAppBalance
                    
                    if (kotlin.math.abs(discrepancy) > 0.01) {
                        logger.info("SYNC_FLOW", "Equity balance discrepancy found: $discrepancy. Updating account balance to $latestBalance")
                        accountRepository.addOrUpdateAccount(account.copy(balance = latestBalance))
                    } else {
                        logger.info("SYNC_FLOW", "Equity balance is in sync.")
                    }
                }
            }

            onProgress(1.0f)
        }
        Unit
    }
}
