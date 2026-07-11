package com.fintrack.shared.feature.transaction.domain.util

import android.content.Context
import com.fintrack.shared.feature.account.domain.repository.AccountRepository
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
): TransactionImporter {
    val context = importerContext ?: throw IllegalStateException("TransactionImporter not initialized. Call initTransactionImporter(context)")
    val logger = KMPLogger()
    
    return object : TransactionImporter {
        override suspend fun importHistory(onProgress: (Float) -> Unit) {
            logger.info("SYNC_FLOW", "AndroidTransactionImporter: importHistory started")
            val accountsResult = accountRepository.getAccounts()
            val accounts = (accountsResult as? Result.Success)?.data ?: emptyList()
            
            val mpesaAccount = accounts.find { (it.type == AccountType.MPESA) || (it.name.lowercase() == "mpesa") }
            val equityAccount = accounts.find { (it.type == AccountType.EQUITY) || (it.name.lowercase().contains("equity")) }

            logger.info("SYNC_FLOW", "Found accounts - Mpesa: ${mpesaAccount?.id}, Equity: ${equityAccount?.id}")

            val mpesaImporter = MpesaImporter(context, transactionRepository, accountRepository)
            val equityImporter = EquityImporter(context, transactionRepository, accountRepository)
            
            when {
                mpesaAccount != null && equityAccount != null -> {
                    logger.info("SYNC_FLOW", "Importing both Mpesa and Equity")
                    mpesaImporter.importHistory { progress ->
                        onProgress(progress * 0.5f)
                    }
                    equityImporter.importHistory { progress ->
                        onProgress(0.5f + (progress * 0.5f))
                    }
                }
                mpesaAccount != null -> {
                    logger.info("SYNC_FLOW", "Importing Mpesa only")
                    mpesaImporter.importHistory(onProgress)
                }
                equityAccount != null -> {
                    logger.info("SYNC_FLOW", "Importing Equity only")
                    equityImporter.importHistory(onProgress)
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
