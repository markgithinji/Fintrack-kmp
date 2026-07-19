package com.fintrack.shared.feature.transaction.domain.util

import android.content.Context
import com.fintrack.shared.feature.account.domain.repository.AccountRepository
import com.fintrack.shared.feature.category.domain.repository.CategoryRepository
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.core.logger.KMPLogger
import com.fintrack.shared.feature.settings.domain.datasource.SettingsDataSource
import com.fintrack.shared.feature.transaction.util.EquityImporter
import com.fintrack.shared.feature.transaction.util.MpesaImporter

private var importerContext: Context? = null

fun initTransactionImporter(context: Context) {
    importerContext = context.applicationContext
}

actual fun createTransactionImporter(
    transactionRepository: TransactionRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository,
    settingsDataSource: SettingsDataSource
): TransactionImporter {
    val context = importerContext ?: throw IllegalStateException("TransactionImporter not initialized. Call initTransactionImporter(context)")
    val logger = KMPLogger()
    
    return object : TransactionImporter {
        override suspend fun importHistory(
            targetAccountId: String?,
            isPortfolioSeed: Boolean,
            onProgress: (Float) -> Unit
        ) {
            logger.info("SYNC_DEBUG", "AndroidTransactionImporter: importHistory started. Target: $targetAccountId, PortfolioSeed: $isPortfolioSeed")
            
            val accountsResult = accountRepository.getAccounts()
            if (accountsResult is Result.Error && !isPortfolioSeed) {
                logger.error("SYNC_DEBUG", "Importer: Failed to load accounts from server. Cannot verify linkage.")
                throw accountsResult.exception
            }

            val accounts = (accountsResult as? Result.Success)?.data ?: emptyList()
            
            val mpesaImporter = MpesaImporter(context, transactionRepository, accountRepository, categoryRepository, settingsDataSource)
            val equityImporter = EquityImporter(context, transactionRepository, accountRepository, categoryRepository, settingsDataSource)

            if (targetAccountId != null) {
                val targetAccount = accounts.find { it.id == targetAccountId }
                val hasMpesa = targetAccount?.linkedSources?.contains("mpesa") == true
                val hasEquity = targetAccount?.linkedSources?.contains("equity") == true
                
                logger.info("SYNC_DEBUG", "Importer: Processing target account $targetAccountId. hasMpesa: $hasMpesa, hasEquity: $hasEquity")

                when {
                    isPortfolioSeed -> {
                        logger.info("SYNC_DEBUG", "Importer: Branch -> Portfolio Seeding")
                        mpesaImporter.importHistory(targetAccountId, isPortfolioSeed, onProgress)
                    }
                    hasMpesa && hasEquity -> {
                        logger.info("SYNC_DEBUG", "Importer: Branch -> Both Mpesa & Equity")
                        mpesaImporter.importHistory(targetAccountId, isPortfolioSeed) { onProgress(it * 0.5f) }
                        equityImporter.importHistory(targetAccountId, isPortfolioSeed) { onProgress(0.5f + (it * 0.5f)) }
                    }
                    hasMpesa -> {
                        logger.info("SYNC_DEBUG", "Importer: Branch -> Mpesa only")
                        mpesaImporter.importHistory(targetAccountId, isPortfolioSeed, onProgress)
                    }
                    hasEquity -> {
                        logger.info("SYNC_DEBUG", "Importer: Branch -> Equity only")
                        equityImporter.importHistory(targetAccountId, isPortfolioSeed, onProgress)
                    }
                    else -> {
                        logger.warning("SYNC_DEBUG", "Importer: Branch -> SKIPPING. Account not linked to active sources.")
                        onProgress(1.0f)
                    }
                }
                return
            }

            // Global sync
            val mpesaAccounts = accounts.filter { it.linkedSources.contains("mpesa") }
            val equityAccounts = accounts.filter { it.linkedSources.contains("equity") }

            logger.info("SYNC_DEBUG", "Importer: Global sync start. Mpesa count: ${mpesaAccounts.size}, Equity count: ${equityAccounts.size}")

            if (mpesaAccounts.isEmpty() && equityAccounts.isEmpty()) {
                if (isPortfolioSeed) {
                    val fallback = accounts.firstOrNull()
                    logger.info("SYNC_FLOW", "Seeding dummy data to fallback account: ${fallback?.id}")
                    mpesaImporter.importHistory(fallback?.id, true, onProgress)
                } else {
                    logger.info("SYNC_FLOW", "No linked accounts found for global sync.")
                    onProgress(1.0f)
                }
                return
            }

            // Perform sync for each account
            val totalSteps = mpesaAccounts.size + equityAccounts.size
            var currentStep = 0

            mpesaAccounts.forEach { account ->
                logger.info("SYNC_FLOW", "Global Sync: Processing Mpesa for account ${account.id}")
                mpesaImporter.importHistory(account.id, isPortfolioSeed) { 
                    onProgress((currentStep + it) / totalSteps)
                }
                currentStep++
            }

            equityAccounts.forEach { account ->
                logger.info("SYNC_FLOW", "Global Sync: Processing Equity for account ${account.id}")
                equityImporter.importHistory(account.id, isPortfolioSeed) {
                    onProgress((currentStep + it) / totalSteps)
                }
                currentStep++
            }

            logger.info("SYNC_FLOW", "AndroidTransactionImporter: importHistory finished")
        }
    }
}
