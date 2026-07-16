package com.fintrack.shared.feature.transaction.util

import android.content.Context
import android.provider.Telephony
import com.fintrack.shared.feature.account.domain.model.AccountType
import com.fintrack.shared.feature.account.domain.repository.AccountRepository
import com.fintrack.shared.feature.category.domain.repository.CategoryRepository
import com.fintrack.shared.feature.core.logger.KMPLogger
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import com.fintrack.shared.feature.transaction.domain.util.TransactionImporter
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

class EquityImporter(
    private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) : TransactionImporter {
    private val logger = KMPLogger()

    override suspend fun importHistory(targetAccountId: String?, onProgress: (Float) -> Unit): Unit = withContext(Dispatchers.IO) {
        logger.info("SYNC_FLOW", "EquityImporter: importHistory started for account: $targetAccountId")
        onProgress(0.05f)
        
        // Fetch categories first to map inferred category names to IDs
        val categoriesResult = categoryRepository.getCategories()
        val categories = (categoriesResult as? Result.Success)?.data ?: emptyList()
        
        val accountsResult = accountRepository.getAccounts()
        val accounts = (accountsResult as? Result.Success)?.data ?: emptyList()
        val accountId = targetAccountId ?: accounts.find { it.linkedSources.contains("equity") || it.type == AccountType.EQUITY }?.id
            ?: accounts.find { it.name.lowercase().contains("equity") }?.id
            ?: "equity"

        logger.info("SYNC_FLOW", "Equity account identified as: $accountId")

        // Search for both "EquitBank" and "EquityBank" and "EQUITY"
        val cursor = context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(Telephony.Sms.Inbox.BODY, Telephony.Sms.Inbox.ADDRESS, Telephony.Sms.Inbox.DATE),
            "${Telephony.Sms.Inbox.ADDRESS} IN (?, ?, ?, ?)",
            arrayOf("EquitBank", "EquityBank", "EQUITYBANK", "EQUITY"),
            "${Telephony.Sms.Inbox.DATE} DESC"
        )

        cursor?.use {
            val bodyIndex = it.getColumnIndex(Telephony.Sms.Inbox.BODY)
            val dateIndex = it.getColumnIndex(Telephony.Sms.Inbox.DATE)
            val transactions = mutableListOf<Transaction>()
            var latestBalance: BigDecimal? = null
            var loggedCount = 0
            val totalMessages = it.count
            
            logger.info("SYNC_FLOW", "Found $totalMessages potential Equity messages")

            while (it.moveToNext()) {
                ensureActive()
                val body = it.getString(bodyIndex)
                val timestamp = it.getLong(dateIndex)
                val smsInstant = Instant.fromEpochMilliseconds(timestamp)

                // Log first 1000 Equity SMS for debugging
                if (loggedCount < 1000) {
                    logger.debug("EQUITY_DEBUG", "SMS Body: $body")
                }

                // Keep the first balance we find (most recent message)
                if (latestBalance == null) {
                    latestBalance = EquityParser.parseBalance(body)
                }

                val parsedTransaction = EquityParser.parse(body, accountId, smsInstant)
                if (parsedTransaction != null) {
                    // Use the ID from the parser if it's already a fixed UUID
                    val finalTransaction = if (parsedTransaction.categoryId != "pending") {
                        parsedTransaction
                    } else {
                        // Map inferred category name to ID (Fallback)
                        val categoryName = parsedTransaction.category
                        val isExpense = !parsedTransaction.isIncome
                        
                        val categoryId = categories.find { 
                            it.name.equals(categoryName, ignoreCase = true) && it.isExpense == isExpense 
                        }?.id
                        
                        val fallbackId = categoryId ?: categories.find {
                            it.name.equals("Transfer", ignoreCase = true) && it.isExpense == isExpense 
                        }?.id ?: categories.find {
                            it.name.contains("Other", ignoreCase = true) && it.isExpense == isExpense
                        }?.id ?: categories.find {
                            it.name.contains("Misc", ignoreCase = true) && it.isExpense == isExpense
                        }?.id ?: categories.firstOrNull { it.isExpense == isExpense }?.id 
                        ?: "pending"

                        parsedTransaction.copy(categoryId = fallbackId)
                    }
                    
                    // Deduplicate: Check for exact externalId OR fuzzy match (same amount, type, and within 2 mins)
                    // We prioritize the first one found (which is the newest due to DESC sort)
                    val isDuplicate = transactions.any { existing ->
                        existing.externalId == finalTransaction.externalId || (
                            existing.amount == finalTransaction.amount &&
                            existing.isIncome == finalTransaction.isIncome &&
                            kotlin.math.abs(existing.dateTime.toEpochMilliseconds() - finalTransaction.dateTime.toEpochMilliseconds()) < 120000
                        )
                    }

                    if (!isDuplicate) {
                        transactions.add(finalTransaction)
                        if (transactions.size <= 20) {
                            logger.debug("SYNC_FLOW", "Parsed Equity SUCCESS: id=${finalTransaction.externalId}, date=${finalTransaction.dateTime}, amount=${finalTransaction.amount}, acc=${finalTransaction.accountId}")
                        }
                    } else {
                        logger.debug("EQUITY_DEBUG", "Skipping duplicate/fuzzy-duplicate Equity Tx: ${finalTransaction.externalId} (${finalTransaction.amount})")
                    }
                } else {
                    if (loggedCount < 10) {
                        // Check if it's an Equity message that we are missing
                        if (body.contains("KES", ignoreCase = true) || body.contains("Ksh", ignoreCase = true)) {
                             logger.debug("SYNC_FLOW", "Parsed Equity FAIL (potential match missed): $body")
                        }
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
                    logger.debug("SYNC_FLOW", "Uploading Equity chunk ${index + 1}/$totalChunks... First Tx Acc: ${chunk.firstOrNull()?.accountId}")
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

            // Update account state (balance and last synced time)
            val accountResult = accountRepository.getAccountById(accountId)
            if (accountResult is Result.Success) {
                val account = accountResult.data
                val currentAppBalance = account.balance ?: BigDecimal.ZERO
                val newBalance = latestBalance ?: currentAppBalance
                val now = Clock.System.now()
                
                logger.info("SYNC_FLOW", "Updating Equity account state. Balance: $newBalance, Synced: $now")
                accountRepository.addOrUpdateAccount(account.copy(
                    balance = newBalance,
                    lastSyncedAt = now
                ))
            }

            onProgress(1.0f)
        }
        Unit
    }
}
