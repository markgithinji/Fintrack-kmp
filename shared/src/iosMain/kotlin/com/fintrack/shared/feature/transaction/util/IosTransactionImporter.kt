package com.fintrack.shared.feature.transaction.domain.util

import com.fintrack.shared.feature.account.domain.repository.AccountRepository
import com.fintrack.shared.feature.category.domain.repository.CategoryRepository
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository

class IosTransactionImporter : TransactionImporter {
    override suspend fun importHistory(onProgress: (Float) -> Unit) {
        // No-op on iOS for now as SMS access is restricted
    }
}

actual fun createTransactionImporter(
    transactionRepository: TransactionRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository
): TransactionImporter {
    return IosTransactionImporter()
}
