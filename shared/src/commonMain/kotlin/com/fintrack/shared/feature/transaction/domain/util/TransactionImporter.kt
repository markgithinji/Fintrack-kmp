package com.fintrack.shared.feature.transaction.domain.util

import com.fintrack.shared.feature.account.domain.repository.AccountRepository
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository

interface TransactionImporter {
    suspend fun importHistory()
}

expect fun createTransactionImporter(
    transactionRepository: TransactionRepository,
    accountRepository: AccountRepository
): TransactionImporter
