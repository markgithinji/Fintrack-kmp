package com.fintrack.shared.feature.transaction.domain.util

import android.content.Context
import com.fintrack.shared.feature.account.domain.model.AccountType
import com.fintrack.shared.feature.account.domain.repository.AccountRepository
import com.fintrack.shared.feature.category.domain.repository.CategoryRepository
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.core.logger.KMPLogger
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
): TransactionImporter {
    val context = importerContext ?: throw IllegalStateException("TransactionImporter not initialized. Call initTransactionImporter(context)")
    val logger = KMPLogger()
    
    return object : TransactionImporter {
        override suspend fun importHistory(targetAccountId: String?, onProgress: (Float) -> Unit) {
            logger.info("SYNC_FLOW", "AndroidTransactionImporter: importHistory started. Target: $targetAccountId")
            val accountsResult = accountRepository.getAccounts()
            val accounts = (accountsResult as? Result.Success)?.data ?: emptyList()
            
            val mpesaImporter = MpesaImporter(context, transactionRepository, accountRepository, categoryRepository)
            val equityImporter = EquityImporter(context, transactionRepository, accountRepository, categoryRepository)

            if (targetAccountId != null) {
                val targetAccount = accounts.find { it.id == targetAccountId }
                val hasMpesa = targetAccount?.linkedSources?.contains("mpesa") == true || 
                              targetAccount?.type == AccountType.MPESA || 
                              targetAccount?.name?.lowercase() == "mpesa"
                
                val hasEquity = targetAccount?.linkedSources?.contains("equity") == true || 
                               targetAccount?.type == AccountType.EQUITY || 
                               targetAccount?.name?.lowercase()?.contains("equity") == true

                when {
                    hasMpesa && hasEquity -> {
                        logger.info("SYNC_FLOW", "Importing both Mpesa and Equity for account: $targetAccountId")
                        mpesaImporter.importHistory(targetAccountId) { onProgress(it * 0.5f) }
                        equityImporter.importHistory(targetAccountId) { onProgress(0.5f + (it * 0.5f)) }
                    }
                    hasMpesa -> {
                        logger.info("SYNC_FLOW", "Importing specific Mpesa account: $targetAccountId")
                        mpesaImporter.importHistory(targetAccountId, onProgress)
                    }
                    hasEquity -> {
                        logger.info("SYNC_FLOW", "Importing specific Equity account: $targetAccountId")
                        equityImporter.importHistory(targetAccountId, onProgress)
                    }
                    else -> {
                        logger.warning("SYNC_FLOW", "Account $targetAccountId is not a linked account type")
                        onProgress(1.0f)
                    }
                }
                return
            }

            val mpesaAccount = accounts.find { 
                it.linkedSources.contains("mpesa") || it.type == AccountType.MPESA || it.name.lowercase() == "mpesa" 
            }
            val equityAccount = accounts.find { 
                it.linkedSources.contains("equity") || it.type == AccountType.EQUITY || it.name.lowercase().contains("equity") 
            }

            logger.info("SYNC_FLOW", "Found accounts for global sync - Mpesa: ${mpesaAccount?.id}, Equity: ${equityAccount?.id}")

            when {
                mpesaAccount != null && equityAccount != null -> {
                    logger.info("SYNC_FLOW", "Importing both Mpesa and Equity")
                    mpesaImporter.importHistory(null) { progress ->
                        onProgress(progress * 0.5f)
                    }
                    equityImporter.importHistory(null) { progress ->
                        onProgress(0.5f + (progress * 0.5f))
                    }
                }
                mpesaAccount != null -> {
                    logger.info("SYNC_FLOW", "Importing Mpesa only")
                    mpesaImporter.importHistory(null, onProgress)
                }
                equityAccount != null -> {
                    logger.info("SYNC_FLOW", "Importing Equity only")
                    equityImporter.importHistory(null, onProgress)
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
