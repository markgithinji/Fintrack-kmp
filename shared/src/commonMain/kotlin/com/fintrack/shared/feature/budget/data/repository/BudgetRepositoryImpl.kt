package com.fintrack.shared.feature.budget.data.repository

import com.fintrack.shared.feature.budget.data.model.toCreateRequest
import com.fintrack.shared.feature.budget.data.model.toDomain
import com.fintrack.shared.feature.budget.data.model.toUpdateRequest
import com.fintrack.shared.feature.budget.data.remote.BudgetApi
import com.fintrack.shared.feature.budget.domain.model.Budget
import com.fintrack.shared.feature.budget.domain.model.BudgetWithStatus
import com.fintrack.shared.feature.budget.domain.repository.BudgetRepository
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.core.util.safeApiCall

class BudgetRepositoryImpl(
    private val api: BudgetApi
) : BudgetRepository {

    override suspend fun getBudgets(): Result<List<BudgetWithStatus>> =
        safeApiCall {
            val budgetsWithStatusDto = api.getBudgets()
            budgetsWithStatusDto.map { it.toDomain() }
        }

    override suspend fun getBudgetById(id: String): Result<BudgetWithStatus> =
        safeApiCall {
            val budgetWithStatusDto = api.getBudgetById(id)
            budgetWithStatusDto.toDomain()
        }

    override suspend fun addOrUpdateBudget(budget: Budget): Result<Budget> =
        safeApiCall {
            if (budget.id == null) {
                // Create new budget
                val createRequest = budget.toCreateRequest()
                val dto = api.addBudget(createRequest)
                dto.toDomain()
            } else {
                // Update existing budget
                val updateRequest = budget.toUpdateRequest()
                val dto = api.updateBudget(budget.id, updateRequest)
                dto.toDomain()
            }
        }

    override suspend fun deleteBudget(id: String): Result<Unit> =
        safeApiCall {
            api.deleteBudget(id)
        }
}