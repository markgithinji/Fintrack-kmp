package com.fintrack.shared.feature.budget.data.repository

import com.fintrack.shared.feature.budget.data.model.toDomain
import com.fintrack.shared.feature.budget.data.model.toDto
import com.fintrack.shared.feature.budget.data.remote.BudgetApi
import com.fintrack.shared.feature.budget.domain.model.Budget
import com.fintrack.shared.feature.budget.domain.model.BudgetWithStatus
import com.fintrack.shared.feature.budget.domain.repository.BudgetRepository
import com.fintrack.shared.feature.core.Result

class BudgetRepositoryImpl(
    private val api: BudgetApi = BudgetApi()
) : BudgetRepository {

    override suspend fun getBudgets(): Result<List<BudgetWithStatus>> = try {
        val budgetsWithStatusDto = api.getBudgets()
        val budgets = budgetsWithStatusDto.map { it.toDomain() }
        Result.Success(budgets)
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun getBudgetById(id: Int): Result<BudgetWithStatus> = try {
        val budgetWithStatusDto = api.getBudgetById(id)
        Result.Success(budgetWithStatusDto.toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun addOrUpdateBudget(budget: Budget): Result<Budget> = try {
        val dto = budget.toDto()
        val updatedDto = if (budget.id == null) {
            api.addBudget(dto)
        } else {
            api.updateBudget(budget.id, dto)
        }
        Result.Success(updatedDto.toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun deleteBudget(id: Int): Result<Unit> = try {
        api.deleteBudget(id)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }
}