package com.fintrack.shared.feature.budget.domain.repository

import com.fintrack.shared.feature.budget.domain.model.Budget
import com.fintrack.shared.feature.budget.domain.model.BudgetWithStatus
import com.fintrack.shared.feature.core.util.Result
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    val budgets: Flow<Result<List<BudgetWithStatus>>>
    suspend fun getBudgets(forceRefresh: Boolean = false): Result<List<BudgetWithStatus>>
    suspend fun getBudgetById(id: String): Result<BudgetWithStatus>
    suspend fun addOrUpdateBudget(budget: Budget): Result<Budget>
    suspend fun deleteBudget(id: String): Result<Unit>
}