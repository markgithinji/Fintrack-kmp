package com.fintrack.shared.feature.budget.data.repository

import com.fintrack.shared.feature.budget.data.model.toDomain
import com.fintrack.shared.feature.budget.data.model.toDto
import com.fintrack.shared.feature.budget.data.remote.BudgetApi
import com.fintrack.shared.feature.budget.domain.model.Budget
import com.fintrack.shared.feature.budget.domain.model.BudgetWithStatus
import com.fintrack.shared.feature.budget.domain.repository.BudgetRepository
import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.core.safeApiCall

class BudgetRepositoryImpl(
    private val api: BudgetApi
) : BudgetRepository {

    override suspend fun getBudgets(): Result<List<BudgetWithStatus>> =
        safeApiCall {
            val budgetsWithStatusDto = api.getBudgets()
            budgetsWithStatusDto.map { it.toDomain() }
        }

    override suspend fun getBudgetById(id: Int): Result<BudgetWithStatus> =
        safeApiCall {
            val budgetWithStatusDto = api.getBudgetById(id)
            budgetWithStatusDto.toDomain()
        }

    override suspend fun addOrUpdateBudget(budget: Budget): Result<Budget> =
        safeApiCall {
            val dto = budget.toDto()
            val updatedDto = if (budget.id == null) {
                api.addBudget(dto)
            } else {
                api.updateBudget(budget.id, dto)
            }
            updatedDto.toDomain()
        }

    override suspend fun deleteBudget(id: Int): Result<Unit> =
        safeApiCall {
            api.deleteBudget(id)
        }
}