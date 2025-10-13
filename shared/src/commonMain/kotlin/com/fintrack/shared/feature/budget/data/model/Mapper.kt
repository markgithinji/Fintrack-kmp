package com.fintrack.shared.feature.budget.data.model

import com.fintrack.shared.feature.budget.domain.model.Budget
import com.fintrack.shared.feature.budget.domain.model.BudgetStatus
import com.fintrack.shared.feature.budget.domain.model.BudgetWithStatus
import com.fintrack.shared.feature.transaction.domain.model.Category

fun BudgetDto.toDomain(): Budget =
    Budget(
        id = id,
        accountId = accountId,
        name = name,
        categories = categories.map { n -> Category.fromName(n, isExpense) },
        limit = limit,
        isExpense = isExpense,
        startDate = startDate,
        endDate = endDate
    )

fun Budget.toCreateRequest(): CreateBudgetRequest = CreateBudgetRequest(
    accountId = this.accountId,
    name = this.name,
    categories = this.categories.map { it.name },
    limit = this.limit,
    isExpense = this.isExpense,
    startDate = this.startDate,
    endDate = this.endDate
)

fun Budget.toUpdateRequest(): UpdateBudgetRequest = UpdateBudgetRequest(
    accountId = this.accountId,
    name = this.name,
    categories = this.categories.map { it.name },
    limit = this.limit,
    isExpense = this.isExpense,
    startDate = this.startDate,
    endDate = this.endDate
)

fun BudgetWithStatusDto.toDomain(): BudgetWithStatus {
    return BudgetWithStatus(
        budget = budget.toDomain(),
        status = BudgetStatus(
            limit = budget.limit,
            spent = status.spent,
            remaining = status.remaining,
            percentageUsed = status.percentageUsed,
            isExceeded = status.isExceeded
        )
    )
}