package com.fintrack.shared.feature.transaction.domain.util

import android.content.Context
import com.fintrack.shared.feature.account.domain.repository.AccountRepository
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.settings.domain.datasource.SettingsDataSource
import com.fintrack.shared.feature.transaction.util.EquityImporter
import com.fintrack.shared.feature.transaction.util.MpesaImporter
import kotlinx.coroutines.flow.first

private var importerContext: Context? = null

fun initTransactionImporter(context: Context) {
    importerContext = context.applicationContext
}

actual fun createTransactionImporter(
    transactionRepository: TransactionRepository,
    accountRepository: AccountRepository
): TransactionImporter {
    val context = importerContext ?: throw IllegalStateException("TransactionImporter not initialized. Call initTransactionImporter(context)")
    
    return object : TransactionImporter {
        override suspend fun importHistory(onProgress: (Float) -> Unit) {
            val accountsResult = accountRepository.getAccounts()
            val accounts = (accountsResult as? Result.Success)?.data ?: emptyList()
            
            val hasMpesaAccount = accounts.any { it.isMpesa || it.name.lowercase() == "mpesa" }
            val hasEquityAccount = accounts.any { it.isEquity || it.name.lowercase().contains("equity") }

            val mpesaImporter = MpesaImporter(context, transactionRepository, accountRepository)
            val equityImporter = EquityImporter(context, transactionRepository, accountRepository)
            
            when {
                hasMpesaAccount && hasEquityAccount -> {
                    // Run M-Pesa import
                    mpesaImporter.importHistory { progress ->
                        onProgress(progress * 0.5f) // Map to 0-50%
                    }
                    
                    // Run Equity import
                    equityImporter.importHistory { progress ->
                        onProgress(0.5f + (progress * 0.5f)) // Map to 50-100%
                    }
                }
                hasMpesaAccount -> {
                    mpesaImporter.importHistory(onProgress)
                }
                hasEquityAccount -> {
                    equityImporter.importHistory(onProgress)
                }
                else -> {
                    onProgress(1.0f)
                }
            }
        }
    }
}
