package com.fintrack.shared.feature.budget.data.model

import kotlinx.datetime.LocalDate

data class CreateBudgetRequest(
    val accountId: String,
    val name: String,
    val categories: List<String>,
    val limit: Double,
    val isExpense: Boolean,
    val startDate: LocalDate,
    val endDate: LocalDate
)