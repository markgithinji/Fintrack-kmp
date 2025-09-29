package com.fintrack.shared.feature.budget.domain

data class BudgetWithStatus(
    val budget: Budget,
    val status: BudgetStatus
)