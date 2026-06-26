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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BudgetRepositoryImpl(
    private val api: BudgetApi
) : BudgetRepository {

    private val _budgets = MutableStateFlow<Result<List<BudgetWithStatus>>>(Result.Loading)
    override val budgets: Flow<Result<List<BudgetWithStatus>>> = _budgets.asStateFlow()

    override suspend fun getBudgets(forceRefresh: Boolean): Result<List<BudgetWithStatus>> {
        if (!forceRefresh && _budgets.value is Result.Success) {
            return _budgets.value
        }
        
        val result = safeApiCall {
            val budgetsWithStatusDto = api.getBudgets()
            budgetsWithStatusDto.map { it.toDomain() }
        }
        _budgets.value = result
        return result
    }

    override suspend fun getBudgetById(id: String): Result<BudgetWithStatus> =
        safeApiCall {
            val budgetWithStatusDto = api.getBudgetById(id)
            budgetWithStatusDto.toDomain()
        }

    override suspend fun addOrUpdateBudget(budget: Budget): Result<Budget> {
        val result = safeApiCall {
            if (budget.id == null) {
                val createRequest = budget.toCreateRequest()
                val dto = api.addBudget(createRequest)
                dto.budget.toDomain()
            } else {
                val updateRequest = budget.toUpdateRequest()
                val dto = api.updateBudget(budget.id, updateRequest)
                dto.budget.toDomain()
            }
        }
        if (result is Result.Success) {
            getBudgets(forceRefresh = true)
        }
        return result
    }

    override suspend fun deleteBudget(id: String): Result<Unit> {
        val result = safeApiCall {
            api.deleteBudget(id)
        }
        if (result is Result.Success) {
            getBudgets(forceRefresh = true)
        }
        return result
    }
}