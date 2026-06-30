package com.fintrack.shared.feature.core.domain.usecase

import com.fintrack.shared.feature.budget.domain.repository.BudgetRepository
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository

class ClearAllUserDataUseCase(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        // Clear transactions
        val transactionResult = transactionRepository.deleteAllTransactions()
        if (transactionResult is Result.Error) return transactionResult

        // Clear budgets
        val budgetResult = budgetRepository.deleteAllBudgets()
        if (budgetResult is Result.Error) return budgetResult

        return Result.Success(Unit)
    }
}
