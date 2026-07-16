package com.fintrack.shared.feature.transaction.domain.util

import com.fintrack.shared.feature.account.domain.repository.AccountRepository
import com.fintrack.shared.feature.category.domain.repository.CategoryRepository
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository

class IosTransactionImporter : TransactionImporter {
    override suspend fun importHistory(targetAccountId: String?, onProgress: (Float) -> Unit) {
        onProgress(1.0f)
    }
}

actual fun createTransactionImporter(
    transactionRepository: TransactionRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository
): TransactionImporter {
    return IosTransactionImporter()
}
