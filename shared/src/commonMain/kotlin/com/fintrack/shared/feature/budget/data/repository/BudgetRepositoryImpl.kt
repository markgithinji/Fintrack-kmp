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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BudgetRepositoryImpl(
    private val api: BudgetApi
) : BudgetRepository {

    private val _budgets = MutableStateFlow<Result<List<BudgetWithStatus>>>(Result.Loading)
    override val budgets: StateFlow<Result<List<BudgetWithStatus>>> = _budgets.asStateFlow()

    override suspend fun getBudgets(
        forceRefresh: Boolean,
        limit: Int,
        offset: Long,
        accountId: String?
    ): Result<List<BudgetWithStatus>> {
        if (!forceRefresh && _budgets.value is Result.Success && offset == 0L) {
            return _budgets.value
        }
        
        val result = safeApiCall {
            val budgetsWithStatusDto = api.getBudgets(limit = limit, offset = offset, accountId = accountId)
            budgetsWithStatusDto.map { it.toDomain() }
        }

        if (offset == 0L) {
            _budgets.value = result
        } else if (result is Result.Success) {
            val currentList = (_budgets.value as? Result.Success)?.data ?: emptyList()
            _budgets.value = Result.Success(currentList + result.data)
        }

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

    override suspend fun deleteAllBudgets(accountIds: List<String>?): Result<Unit> {
        val result = safeApiCall {
            api.deleteAllBudgets(accountIds)
        }
        if (result is Result.Success) {
            getBudgets(forceRefresh = true)
        }
        return result
    }
}