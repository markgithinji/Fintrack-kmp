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

class MpesaImporter(
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
        // logger.info("SYNC_FLOW", "MpesaImporter: importHistory started for account: $targetAccountId")
        onProgress(0.05f)
        
        // Fetch categories and rules first to map inferred category names to IDs
        val categoriesResult = categoryRepository.getCategories()
        val categories = (categoriesResult as? Result.Success)?.data ?: emptyList()
        
        val rulesResult = categoryRepository.getCategoryRules()
        val rules = (rulesResult as? Result.Success)?.data ?: emptyList()
        // logger.info("SYNC_FLOW", "MpesaImporter: Fetched ${rules.size} dynamic categorization rules from backend")
        
        val accountsResult = accountRepository.getAccounts()
        val accounts = (accountsResult as? Result.Success)?.data ?: emptyList()
        val mpesaLinkedAccountIds = settingsDataSource.mpesaLinkedAccountIds.value
        val accountId = targetAccountId ?: accounts.find { mpesaLinkedAccountIds.contains(it.id) || it.type == AccountType.MPESA }?.id
            ?: accounts.find { it.name.lowercase() == "mpesa" }?.id 
            ?: "mpesa"

        logger.info("SYNC_FLOW", "Mpesa account identified as: $accountId")

        val cursor = try {
            context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                arrayOf(Telephony.Sms.Inbox.BODY, Telephony.Sms.Inbox.ADDRESS, Telephony.Sms.Inbox.DATE),
                "${Telephony.Sms.Inbox.ADDRESS} = ?",
                arrayOf("MPESA"),
                "${Telephony.Sms.Inbox.DATE} DESC"
            )
        } catch (e: SecurityException) {
            if (isPortfolioSeed) {
                logger.warning("SYNC_FLOW", "Permission denied for SMS, but proceeding with portfolio seeding only.")
                null
            } else {
                logger.error("SYNC_FLOW", "Permission denied for reading SMS", e)
                throw Exception("Permission denied: READ_SMS is required for M-Pesa sync", e)
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
                
                logger.info("SYNC_FLOW", "Found $totalMessages potential MPESA messages")

                while (it.moveToNext()) {
                    ensureActive()
                    val body = it.getString(bodyIndex)
                    val timestamp = it.getLong(dateIndex)
                    val smsInstant = Instant.fromEpochMilliseconds(timestamp)

                    // Keep the first balance we find (most recent message)
                    if (latestBalance == null) {
                        latestBalance = MpesaParser.parseBalance(body)
                    }

                    val parsed = MpesaParser.parse(body, accountId, smsInstant, rules)
                    if (parsed != null) {
                        // Use the ID from the parser if it's already a fixed UUID
                        val transaction = if (parsed.categoryId != "pending" && !parsed.categoryId.startsWith("custom_")) {
                            parsed
                        } else {
                            // Fallback to name-based lookup for custom categories or failures
                            val categoryName = parsed.category
                            val isExpense = !parsed.isIncome
                            
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

                            parsed.copy(categoryId = finalCategoryId)
                        }
                        
                        transactions.add(transaction)
                        if (loggedCount < 5) {
                            logger.debug("SYNC_FLOW", "Parsed sample: ${transaction.externalId} on ${transaction.dateTime} - Cat: ${transaction.category} (${transaction.categoryId})")
                        }
                    }
                    
                    loggedCount++
                    if (loggedCount % 500 == 0) {
                        logger.info("SYNC_FLOW", "Scanning SMS: $loggedCount/$totalMessages processed...")
                        onProgress(0.05f + (loggedCount.toFloat() / totalMessages) * 0.25f)
                    }
                }
            }
        } else if (!isPortfolioSeed) {
            logger.warning("SYNC_FLOW", "SMS cursor is null. This may be due to missing permissions or OS restrictions.")
            throw Exception("Unable to access SMS. Please check app permissions.")
        }

        logger.info("SYNC_FLOW", "Scanning complete. Found ${transactions.size} valid transactions to upload.")

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
            
            logger.info("SYNC_FLOW", "Starting upload of ${transactions.size} transactions in $totalChunks chunks")
            
            var failedBatchCount = 0
            var lastErrorMessage: String? = null

            // Chunk the import to avoid "Internal Server Error" (often caused by large payloads)
            chunks.forEachIndexed { index, chunk ->
                ensureActive()
                logger.debug("SYNC_FLOW", "Uploading chunk ${index + 1}/$totalChunks...")
                val result = transactionRepository.importMpesaTransactions(chunk)
                if (result is Result.Success) {
                    if (index < 3 || index == totalChunks - 1) {
                       logger.info("SYNC_FLOW", "Successfully uploaded chunk ${index + 1}/$totalChunks")
                    }
                } else if (result is Result.Error) {
                    failedBatchCount++
                    lastErrorMessage = result.exception.message
                    logger.error("SYNC_FLOW", "Failed to upload chunk ${index + 1}: $lastErrorMessage", result.exception)
                }
                // Update progress from 30% to 90% during upload
                onProgress(0.3f + ((index + 1).toFloat() / totalChunks) * 0.6f)
            }

            if (failedBatchCount > 0) {
                val summary = "Failed to sync $failedBatchCount out of $totalChunks batches. Last error: $lastErrorMessage"
                logger.error("SYNC_FLOW", summary, null)
                throw Exception(summary)
            }
        }
        logger.info("SYNC_FLOW", "Mpesa import process completed successfully")

        // Update account state (balance and last synced time)
        val accountResult = accountRepository.getAccountById(accountId)
        if (accountResult is Result.Success) {
            val account = accountResult.data
            val isPureMpesaAccount = account.type == AccountType.MPESA && 
                                   !account.linkedSources.contains("equity") &&
                                   !account.linkedSources.contains("bank")
            
            val currentAppBalance = account.balance ?: BigDecimal.ZERO
            val newBalance = if (isPortfolioSeed) {
                BigDecimal.fromInt(72450) // Nice rounded-ish balance for screenshots
            } else if (isPureMpesaAccount) {
                latestBalance ?: currentAppBalance
            } else {
                currentAppBalance
            }
            val now = Clock.System.now()
            
            logger.info("SYNC_FLOW", "Updating Mpesa account state. Pure Mpesa: $isPureMpesaAccount, Balance: $newBalance, Seed: $isPortfolioSeed")
            accountRepository.addOrUpdateAccount(account.copy(
                balance = newBalance,
                lastSyncedAt = now
            ))
        }
        onProgress(1.0f)
    }
}
