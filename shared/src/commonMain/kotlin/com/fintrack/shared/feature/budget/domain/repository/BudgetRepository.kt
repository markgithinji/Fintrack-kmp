package com.fintrack.shared.feature.budget.domain.repository

import com.fintrack.shared.feature.budget.domain.model.Budget
import com.fintrack.shared.feature.budget.domain.model.BudgetWithStatus
import com.fintrack.shared.feature.core.Result

interface BudgetRepository {
    suspend fun getBudgets(): Result<List<BudgetWithStatus>>
    suspend fun getBudgetById(id: Int): Result<BudgetWithStatus>
    suspend fun addOrUpdateBudget(budget: Budget): Result<Budget>
    suspend fun deleteBudget(id: Int): Result<Unit>
}