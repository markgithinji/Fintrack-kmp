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
            // We'll keep this as a 'Global' sync for now, but we'll ensure
            // individual importers are robust against duplicate data.
            val accountsResult = accountRepository.getAccounts()
            val accounts = (accountsResult as? Result.Success)?.data ?: emptyList()
            
            val mpesaAccount = accounts.find { it.isMpesa || it.name.lowercase() == "mpesa" }
            val equityAccount = accounts.find { it.isEquity || it.name.lowercase().contains("equity") }

            val mpesaImporter = MpesaImporter(context, transactionRepository, accountRepository)
            val equityImporter = EquityImporter(context, transactionRepository, accountRepository)
            
            when {
                mpesaAccount != null && equityAccount != null -> {
                    mpesaImporter.importHistory { progress ->
                        onProgress(progress * 0.5f)
                    }
                    equityImporter.importHistory { progress ->
                        onProgress(0.5f + (progress * 0.5f))
                    }
                }
                mpesaAccount != null -> mpesaImporter.importHistory(onProgress)
                equityAccount != null -> equityImporter.importHistory(onProgress)
                else -> onProgress(1.0f)
            }
        }
    }
}
