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
import kotlin.coroutines.coroutineContext

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

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
        onProgress(0.05f)
        
        val categoriesResult = categoryRepository.getCategories()
        val categories = (categoriesResult as? Result.Success)?.data ?: emptyList()
        
        val rulesResult = categoryRepository.getCategoryRules()
        val rules = (rulesResult as? Result.Success)?.data ?: emptyList()
        
        val accountsResult = accountRepository.getAccounts()
        val accounts = (accountsResult as? Result.Success)?.data ?: emptyList()
        
        val accountId = targetAccountId ?: accounts.find { it.linkedSources.contains("mpesa") }?.id

        if (accountId == null) {
            logger.warning("SYNC_FLOW", "MpesaImporter: No destination account ID provided or found in linked sources.")
            if (isPortfolioSeed) {
                 val fallbackId = accounts.firstOrNull()?.id ?: "mpesa"
                 logger.info("SYNC_FLOW", "Portfolio Seeding: Using fallback account ID: $fallbackId")
                 processImport(fallbackId, categories, rules, isPortfolioSeed, onProgress)
            } else {
                throw Exception("No account is configured for M-Pesa sync.")
            }
            return@withContext
        }

        processImport(accountId, categories, rules, isPortfolioSeed, onProgress)
    }

    private suspend fun processImport(
        accountId: String,
        categories: List<com.fintrack.shared.feature.category.domain.model.Category>,
        rules: List<com.fintrack.shared.feature.category.domain.model.CategoryRule>,
        isPortfolioSeed: Boolean,
        onProgress: (Float) -> Unit
    ) {
        val permissionStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS)
        logger.info("SYNC_DEBUG", "MpesaImporter: Permission check for READ_SMS: ${if (permissionStatus == PackageManager.PERMISSION_GRANTED) "GRANTED" else "DENIED"}")
        
        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
            if (!isPortfolioSeed) {
                logger.error("SYNC_DEBUG", "MpesaImporter: Permission denied, throwing exception.")
                throw Exception("Permission denied: READ_SMS is required for M-Pesa sync")
            }
        }

        val cursor = try {
            context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                arrayOf(Telephony.Sms.Inbox.BODY, Telephony.Sms.Inbox.ADDRESS, Telephony.Sms.Inbox.DATE),
                "${Telephony.Sms.Inbox.ADDRESS} = ?",
                arrayOf("MPESA"),
                "${Telephony.Sms.Inbox.DATE} DESC"
            )
        } catch (e: SecurityException) {
            if (isPortfolioSeed) null else throw Exception("Permission denied: READ_SMS is required for M-Pesa sync", e)
        }

        val transactions = mutableListOf<Transaction>()
        var latestBalance: BigDecimal? = null

        if (cursor != null) {
            cursor.use {
                val bodyIndex = it.getColumnIndex(Telephony.Sms.Inbox.BODY)
                val dateIndex = it.getColumnIndex(Telephony.Sms.Inbox.DATE)
                var loggedCount = 0
                val totalMessages = it.count

                while (it.moveToNext()) {
                    coroutineContext.ensureActive()
                    val body = it.getString(bodyIndex)
                    val timestamp = it.getLong(dateIndex)
                    val smsInstant = Instant.fromEpochMilliseconds(timestamp)

                    if (latestBalance == null) {
                        latestBalance = MpesaParser.parseBalance(body)
                    }

                    val parsed = MpesaParser.parse(body, accountId, smsInstant, rules)
                    if (parsed != null) {
                        val categoryName = parsed.category
                        val isExpense = !parsed.isIncome
                        val finalCategoryId = categories.find { it.name.equals(categoryName, ignoreCase = true) && it.isExpense == isExpense }?.id
                            ?: categories.find { it.name.contains("Other", ignoreCase = true) && it.isExpense == isExpense }?.id
                            ?: "pending"

                        transactions.add(parsed.copy(categoryId = finalCategoryId))
                    }
                    
                    loggedCount++
                    if (loggedCount % 500 == 0) onProgress(0.05f + (loggedCount.toFloat() / totalMessages) * 0.25f)
                }
            }
        }

        if (isPortfolioSeed) {
            val dummyTransactions = portfolioSeeder.generateDummyTransactions(accountId, categories)
            transactions.addAll(dummyTransactions)
        }

        if (transactions.isNotEmpty()) {
            onProgress(0.3f)
            val chunks = transactions.chunked(250)
            chunks.forEachIndexed { index, chunk ->
                coroutineContext.ensureActive()
                val result = transactionRepository.importMpesaTransactions(chunk)
                if (result is Result.Error) {
                    logger.error("SYNC_FLOW", "MpesaImporter: Failed to import chunk $index: ${result.exception.message}")
                    throw result.exception
                }
                onProgress(0.3f + ((index + 1).toFloat() / chunks.size) * 0.6f)
            }
        }

        val accountResult = accountRepository.getAccountById(accountId)
        if (accountResult is Result.Success) {
            val account = accountResult.data
            val isPureMpesaAccount = account.linkedSources.contains("mpesa") && account.linkedSources.size == 1
            val newBalance = if (isPortfolioSeed) BigDecimal.fromInt(72450) else if (isPureMpesaAccount) latestBalance ?: account.balance else account.balance
            val updateResult = accountRepository.addOrUpdateAccount(account.copy(balance = newBalance, lastSyncedAt = Clock.System.now()))
            if (updateResult is Result.Error) {
                logger.error("SYNC_FLOW", "MpesaImporter: Failed to update account balance: ${updateResult.exception.message}")
                throw updateResult.exception
            }
        }
        onProgress(1.0f)
    }
}
