package com.fintrack.shared.feature.budget.data.model

import com.fintrack.shared.feature.budget.domain.Budget
import com.fintrack.shared.feature.transaction.model.Category

fun BudgetDto.toDomain(): Budget =
    Budget(
        id = id,
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
        name = name,
        categories = categories.map { it.name },
        limit = limit,
        isExpense = isExpense,
        startDate = startDate,
        endDate = endDate
    )