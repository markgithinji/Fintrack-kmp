package com.fintrack.shared.feature.transaction.util

import android.content.Context
import android.provider.Telephony
import com.fintrack.shared.feature.account.domain.repository.AccountRepository
import com.fintrack.shared.feature.core.logger.KMPLogger
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import com.fintrack.shared.feature.transaction.domain.util.TransactionImporter
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import kotlin.time.Instant
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MpesaImporter(
    private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : TransactionImporter {
    private val logger = KMPLogger()

    @OptIn(ExperimentalTime::class)
    override suspend fun importHistory(onProgress: (Float) -> Unit): Unit = withContext(Dispatchers.IO) {
        onProgress(0.05f)
        val accountsResult = accountRepository.getAccounts()
        val accounts = (accountsResult as? Result.Success)?.data ?: emptyList()
        val accountId = accounts.find { it.isMpesa }?.id 
            ?: accounts.find { it.name.lowercase() == "mpesa" }?.id 
            ?: "mpesa"

        val cursor = context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(Telephony.Sms.Inbox.BODY, Telephony.Sms.Inbox.ADDRESS, Telephony.Sms.Inbox.DATE),
            "${Telephony.Sms.Inbox.ADDRESS} = ?",
            arrayOf("MPESA"),
            "${Telephony.Sms.Inbox.DATE} DESC"
        )

        cursor?.use {
            val bodyIndex = it.getColumnIndex(Telephony.Sms.Inbox.BODY)
            val dateIndex = it.getColumnIndex(Telephony.Sms.Inbox.DATE)
            val transactions = mutableListOf<Transaction>()
            var latestBalance: Double? = null
            var loggedCount = 0
            val totalMessages = it.count

            while (it.moveToNext()) {
                val body = it.getString(bodyIndex)
                val timestamp = it.getLong(dateIndex)
                val smsInstant = Instant.fromEpochMilliseconds(timestamp)

                // Log the first 2000 messages to help improve the parser
                if (loggedCount in 0 until 5000) {
                    logger.debug("MPESA_PARSER_DEBUG", "Message ${loggedCount + 1}: $body")
                }

                // Keep the first balance we find (most recent message)
                if (latestBalance == null) {
                    latestBalance = MpesaParser.parseBalance(body)
                }

                val transaction = MpesaParser.parse(body, accountId, smsInstant)
                if (transaction != null) {
                    transactions.add(transaction)
                    if (loggedCount < 10) {
                        logger.debug("MPESA_IMPORTER", "Parsed transaction: ${transaction.externalId} on ${transaction.dateTime}")
                    }
                } else {
                    if (body.contains("Confirmed", ignoreCase = true)) {
                        logger.warning("MPESA_PARSER_DEBUG", "Failed to parse 'Confirmed' message: $body")
                    }
                }
                
                loggedCount++
                if (loggedCount % 50 == 0) {
                    // Update progress up to 30% during scanning
                    onProgress(0.05f + (loggedCount.toFloat() / totalMessages) * 0.25f)
                }
            }

            if (transactions.isNotEmpty()) {
                onProgress(0.3f)
                // We no longer reverse. Send newest first so that even if sync is interrupted, 
                // the user sees recent transactions.
                val newestFirstTransactions = transactions 
                val chunks = newestFirstTransactions.chunked(250) // Increased chunk size to speed up
                val totalChunks = chunks.size
                
                logger.info("MPESA_IMPORTER", "Starting upload of ${transactions.size} transactions in $totalChunks chunks")
                
                var failedBatchCount = 0
                var lastErrorMessage: String? = null

                // Chunk the import to avoid "Internal Server Error" (often caused by large payloads)
                chunks.forEachIndexed { index, chunk ->
                    val result = transactionRepository.importMpesaTransactions(chunk)
                    if (result is Result.Success) {
                        if (index < 5 || index == totalChunks - 1) {
                           logger.debug("MPESA_IMPORTER", "Successfully imported chunk ${index + 1}/$totalChunks")
                        }
                    } else if (result is Result.Error) {
                        failedBatchCount++
                        lastErrorMessage = result.exception.message
                        logger.error("MPESA_IMPORTER", "Failed to import chunk ${index + 1}: $lastErrorMessage")
                    }
                    // Update progress from 30% to 90% during upload
                    onProgress(0.3f + ((index + 1).toFloat() / totalChunks) * 0.6f)
                }

                if (failedBatchCount > 0) {
                    val summary = "Failed to sync $failedBatchCount out of $totalChunks batches. Last error: $lastErrorMessage"
                    logger.error("MPESA_IMPORTER", summary, null)
                    throw Exception(summary)
                }
            }
            logger.info("MPESA_IMPORTER", "Import process completed successfully")


            // Correct account balance using an adjustment transaction if there's a discrepancy
            if (latestBalance != null) {
                val accountResult = accountRepository.getAccountById(accountId)
                if (accountResult is Result.Success) {
                    val account = accountResult.data
                    val currentAppBalance = account.balance ?: 0.0
                    val discrepancy = latestBalance - currentAppBalance
                    
                    if (kotlin.math.abs(discrepancy) > 0.01) {
                        // Silent update: Update the account balance directly instead of creating an adjustment transaction
                        accountRepository.addOrUpdateAccount(account.copy(balance = latestBalance))
                    }
                }
            }
            onProgress(1.0f)
        }
        Unit
    }
}
