package com.fintrack.shared.feature.budget.data.repository

import com.fintrack.shared.feature.budget.data.model.toDomain
import com.fintrack.shared.feature.budget.data.model.toDto
import com.fintrack.shared.feature.budget.data.remote.BudgetApi
import com.fintrack.shared.feature.budget.domain.Budget
import com.fintrack.shared.feature.budget.domain.BudgetWithStatus
import com.fintrack.shared.feature.core.Result

class BudgetRepository {

    private val api: BudgetApi = BudgetApi()

    suspend fun getBudgets(): Result<List<BudgetWithStatus>> = try {
        val budgetsWithStatusDto = api.getBudgets()
        val budgets = budgetsWithStatusDto.map { it.toDomain() }
        Result.Success(budgets)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getBudgetById(id: Int): Result<BudgetWithStatus> = try {
        val budgetWithStatusDto = api.getBudgetById(id)
        Result.Success(budgetWithStatusDto.toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun addOrUpdateBudget(budget: Budget): Result<Budget> = try {
        val dto = budget.toDto()
        val updatedDto = if (budget.id == null) {
            // No id → create new budget
            api.addBudget(dto)
        } else {
            // Existing id → update budget
            api.updateBudget(budget.id, dto)
        }
        Result.Success(updatedDto.toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun deleteBudget(id: Int): Result<Unit> = try {
        api.deleteBudget(id)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }
}
