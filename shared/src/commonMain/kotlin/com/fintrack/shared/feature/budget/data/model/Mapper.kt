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

fun Budget.toDto(): BudgetDto =
    BudgetDto(
        id = id,
        accountId = accountId,
        name = name,
        categories = categories.map { it.name },
        limit = limit,
        isExpense = isExpense,
        startDate = startDate,
        endDate = endDate
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