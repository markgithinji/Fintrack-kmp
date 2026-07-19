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
            logger.info("SYNC_FLOW", "AndroidTransactionImporter: importHistory started. Target: $targetAccountId, PortfolioSeed: $isPortfolioSeed")
            
            val accountsResult = accountRepository.getAccounts()
            val accounts = (accountsResult as? Result.Success)?.data ?: emptyList()
            logger.info("SYNC_FLOW", "Fetched ${accounts.size} accounts for identification")
            
            val mpesaImporter = MpesaImporter(context, transactionRepository, accountRepository, categoryRepository, settingsDataSource)
            val equityImporter = EquityImporter(context, transactionRepository, accountRepository, categoryRepository, settingsDataSource)

            if (targetAccountId != null) {
                val mpesaLinkedAccountIds = settingsDataSource.mpesaLinkedAccountIds.value
                val equityLinkedAccountIds = settingsDataSource.equityLinkedAccountIds.value
                
                val targetAccount = accounts.find { it.id == targetAccountId }
                logger.info("SYNC_FLOW", "Target account found in list: ${targetAccount?.name} (Type: ${targetAccount?.type}, Sources: ${targetAccount?.linkedSources})")
                
                val nameLower = targetAccount?.name?.lowercase() ?: ""
                val hasMpesa = mpesaLinkedAccountIds.contains(targetAccountId) || 
                              targetAccount?.type == com.fintrack.shared.feature.account.domain.model.AccountType.MPESA ||
                              targetAccount?.linkedSources?.contains("mpesa") == true ||
                              nameLower.contains("mpesa")
                
                val hasEquity = equityLinkedAccountIds.contains(targetAccountId) ||
                               targetAccount?.type == com.fintrack.shared.feature.account.domain.model.AccountType.EQUITY ||
                               targetAccount?.linkedSources?.contains("equity") == true ||
                               nameLower.contains("equity")

                logger.info("SYNC_FLOW", "Identification result - hasMpesa: $hasMpesa, hasEquity: $hasEquity")

                when {
                    isPortfolioSeed -> {
                        logger.info("SYNC_FLOW", "Portfolio Seeding: Force seeding dummy data to account: $targetAccountId")
                        // Default to Mpesa-style seeding for demo purposes
                        mpesaImporter.importHistory(targetAccountId, isPortfolioSeed, onProgress)
                    }
                    hasMpesa && hasEquity -> {
                        logger.info("SYNC_FLOW", "Importing both Mpesa and Equity for account: $targetAccountId")
                        mpesaImporter.importHistory(targetAccountId, isPortfolioSeed) { onProgress(it * 0.5f) }
                        equityImporter.importHistory(targetAccountId, isPortfolioSeed) { onProgress(0.5f + (it * 0.5f)) }
                    }
                    hasMpesa -> {
                        logger.info("SYNC_FLOW", "Importing specific Mpesa account: $targetAccountId")
                        mpesaImporter.importHistory(targetAccountId, isPortfolioSeed, onProgress)
                    }
                    hasEquity -> {
                        logger.info("SYNC_FLOW", "Importing specific Equity account: $targetAccountId")
                        equityImporter.importHistory(targetAccountId, isPortfolioSeed, onProgress)
                    }
                    else -> {
                        val msg = "Account $targetAccountId is not configured for automatic SMS sync."
                        logger.warning("SYNC_FLOW", msg)
                        throw Exception(msg)
                    }
                }
                return
            }

            val mpesaLinkedAccountIds = settingsDataSource.mpesaLinkedAccountIds.value
            val equityLinkedAccountIds = settingsDataSource.equityLinkedAccountIds.value
            
            val mpesaAccount = accounts.find { 
                it.id in mpesaLinkedAccountIds || 
                it.type == com.fintrack.shared.feature.account.domain.model.AccountType.MPESA ||
                it.linkedSources.contains("mpesa") ||
                it.name.lowercase().contains("mpesa")
            }
            val equityAccount = accounts.find { 
                it.id in equityLinkedAccountIds || 
                it.type == com.fintrack.shared.feature.account.domain.model.AccountType.EQUITY ||
                it.linkedSources.contains("equity") ||
                it.name.lowercase().contains("equity")
            }

            logger.info("SYNC_FLOW", "Found accounts for global sync (Local Settings) - Mpesa: ${mpesaAccount?.id}, Equity: ${equityAccount?.id}")

            when {
                mpesaAccount != null && equityAccount != null -> {
                    logger.info("SYNC_FLOW", "Importing both Mpesa and Equity")
                    mpesaImporter.importHistory(null, isPortfolioSeed) { progress ->
                        onProgress(progress * 0.5f)
                    }
                    equityImporter.importHistory(null, isPortfolioSeed) { progress ->
                        onProgress(0.5f + (progress * 0.5f))
                    }
                }
                mpesaAccount != null -> {
                    logger.info("SYNC_FLOW", "Importing Mpesa only")
                    mpesaImporter.importHistory(null, isPortfolioSeed, onProgress)
                }
                equityAccount != null -> {
                    logger.info("SYNC_FLOW", "Importing Equity only")
                    equityImporter.importHistory(null, isPortfolioSeed, onProgress)
                }
                isPortfolioSeed -> {
                    logger.info("SYNC_FLOW", "Portfolio Seeding (Global): No linked accounts found, seeding to first available account")
                    val fallbackAccount = accounts.find { it.name.lowercase().contains("mpesa") } ?: accounts.firstOrNull()
                    mpesaImporter.importHistory(fallbackAccount?.id, isPortfolioSeed, onProgress)
                }
                else -> {
                    logger.warning("SYNC_FLOW", "No suitable accounts found for import")
                    onProgress(1.0f)
                }
            }
            logger.info("SYNC_FLOW", "AndroidTransactionImporter: importHistory finished")
        }
    }
}
