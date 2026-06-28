package com.fintrack.shared.feature.transaction.domain.usecase

import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository

class ClearAllTransactionsUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.deleteAllTransactions()
    }
}
