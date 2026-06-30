package com.fintrack.shared.feature.transaction.domain.util

import android.content.Context
import com.fintrack.shared.feature.account.domain.repository.AccountRepository
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import com.fintrack.shared.feature.transaction.util.MpesaImporter

private var importerContext: Context? = null

fun initTransactionImporter(context: Context) {
    importerContext = context.applicationContext
}

actual fun createTransactionImporter(
    transactionRepository: TransactionRepository,
    accountRepository: AccountRepository
): TransactionImporter {
    val context = importerContext ?: throw IllegalStateException("TransactionImporter not initialized. Call initTransactionImporter(context)")
    return MpesaImporter(context, transactionRepository, accountRepository)
}
