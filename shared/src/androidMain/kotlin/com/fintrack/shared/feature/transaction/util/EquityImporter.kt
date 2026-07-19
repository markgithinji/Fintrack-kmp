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
import com.fintrack.shared.feature.settings.domain.datasource.SettingsDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class EquityImporter(
    private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsDataSource: SettingsDataSource
) : TransactionImporter {
    private val logger = KMPLogger()
    private val portfolioSeeder = PortfolioSeeder()

    override suspend fun importHistory(
        targetAccountId: String?,
        isPortfolioSeed: Boolean,
        onProgress: (Float) -> Unit
    ): Unit = withContext(Dispatchers.IO) {
        // logger.info("EquityImporter: importHistory started for account: $targetAccountId")
        onProgress(0.05f)
        
        // Fetch categories and rules first to map inferred category names to IDs
        val categoriesResult = categoryRepository.getCategories()
        val categories = (categoriesResult as? Result.Success)?.data ?: emptyList()
        
        val rulesResult = categoryRepository.getCategoryRules()
        val rules = (rulesResult as? Result.Success)?.data ?: emptyList()
        // logger.info("EquityImporter: Fetched ${rules.size} dynamic categorization rules from backend")
        
        val accountsResult = accountRepository.getAccounts()
        val accounts = (accountsResult as? Result.Success)?.data ?: emptyList()
        val equityLinkedAccountIds = settingsDataSource.equityLinkedAccountIds.value
        val accountId = targetAccountId ?: accounts.find { equityLinkedAccountIds.contains(it.id) || it.type == AccountType.EQUITY }?.id
            ?: accounts.find { it.name.lowercase().contains("equity") }?.id
            ?: "equity"

        logger.info("SYNC_FLOW", "Equity account identified as: $accountId")

        // Check for SMS permission first explicitly
        val permissionStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS)
        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
            logger.error("SYNC_FLOW", "EquityImporter: SMS permission NOT granted. Status: $permissionStatus")
            if (isPortfolioSeed) {
                logger.warning("SYNC_FLOW", "EquityImporter: Permission missing, but proceeding with portfolio seeding only.")
            } else {
                throw Exception("Permission denied: READ_SMS is required for Equity Bank sync")
            }
        }

        // Search for both "EquitBank" and "EquityBank" and "EQUITY"
        val cursor = try {
            context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                arrayOf(Telephony.Sms.Inbox.BODY, Telephony.Sms.Inbox.ADDRESS, Telephony.Sms.Inbox.DATE),
                "${Telephony.Sms.Inbox.ADDRESS} IN (?, ?, ?, ?)",
                arrayOf("EquitBank", "EquityBank", "EQUITYBANK", "EQUITY"),
                "${Telephony.Sms.Inbox.DATE} DESC"
            )
        } catch (e: SecurityException) {
            if (isPortfolioSeed) {
                logger.warning("SYNC_FLOW", "Permission denied for SMS, but proceeding with portfolio seeding only.")
                null
            } else {
                logger.error("SYNC_FLOW", "Permission denied for reading SMS", e)
                throw Exception("Permission denied: READ_SMS is required for Equity Bank sync", e)
            }
        }

        val transactions = mutableListOf<Transaction>()
        var latestBalance: BigDecimal? = null

        if (cursor != null) {
            cursor.use {
                val bodyIndex = it.getColumnIndex(Telephony.Sms.Inbox.BODY)
                val dateIndex = it.getColumnIndex(Telephony.Sms.Inbox.DATE)
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

                    val parsedTransaction = EquityParser.parse(body, accountId, smsInstant, rules)
                    if (parsedTransaction != null) {
                        // Use the ID from the parser if it's already a fixed UUID
                        val finalTransaction = if (parsedTransaction.categoryId != "pending" && !parsedTransaction.categoryId.startsWith("custom_")) {
                            parsedTransaction
                        } else {
                            // Fallback to name-based lookup for custom categories or failures
                            val categoryName = parsedTransaction.category
                            val isExpense = !parsedTransaction.isIncome
                            
                            val categoryId = categories.find { 
                                it.name.equals(categoryName, ignoreCase = true) && it.isExpense == isExpense 
                            }?.id
                            
                            val finalCategoryId = categoryId ?: categories.find { 
                                it.name.equals(categoryName, ignoreCase = true)
                            }?.id ?: categories.find { 
                                it.name.equals("Transfer", ignoreCase = true) && it.isExpense == isExpense 
                            }?.id ?: categories.find {
                                it.name.contains("Other", ignoreCase = true) && it.isExpense == isExpense
                            }?.id ?: categories.find {
                                it.name.contains("Misc", ignoreCase = true) && it.isExpense == isExpense
                            }?.id ?: categories.firstOrNull { it.isExpense == isExpense }?.id 
                            ?: categories.firstOrNull()?.id ?: "pending"

                            parsedTransaction.copy(categoryId = finalCategoryId)
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
                        }
                    }
                    
                    loggedCount++
                    if (loggedCount % 100 == 0) {
                        logger.info("SYNC_FLOW", "Scanning Equity SMS: $loggedCount/$totalMessages processed...")
                        onProgress(0.05f + (loggedCount.toFloat() / totalMessages) * 0.25f)
                    }
                }
            }
        } else if (!isPortfolioSeed) {
            logger.warning("SYNC_FLOW", "SMS cursor is null. This may be due to missing permissions or OS restrictions.")
            throw Exception("Unable to access SMS. Please check app permissions.")
        }

        logger.info("SYNC_FLOW", "Scanning complete. Found ${transactions.size} valid Equity transactions to upload.")

        if (isPortfolioSeed) {
            logger.info("SYNC_FLOW", "Portfolio Seeding: Injecting dummy transactions for app exploration...")
            val dummyTransactions = portfolioSeeder.generateDummyTransactions(accountId, categories)
            transactions.addAll(dummyTransactions)
            logger.info("SYNC_FLOW", "Portfolio Seeding: Added ${dummyTransactions.size} dummy transactions. Total now: ${transactions.size}")
        }

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

        // Update account state (last synced time and balance for seeding)
        val accountResult = accountRepository.getAccountById(accountId)
        if (accountResult is Result.Success) {
            val account = accountResult.data
            val now = Clock.System.now()
            
            // For seeding, set a realistic balance. For normal sync, keep existing.
            val newBalance = if (isPortfolioSeed) BigDecimal.fromInt(145800) else account.balance
            
            logger.info("SYNC_FLOW", "Updating Equity account state. Balance: $newBalance, Synced: $now")
            accountRepository.addOrUpdateAccount(account.copy(
                balance = newBalance,
                lastSyncedAt = now
            ))
        }

        onProgress(1.0f)
    }
}
