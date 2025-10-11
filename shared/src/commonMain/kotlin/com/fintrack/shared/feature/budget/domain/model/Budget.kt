package com.fintrack.shared.feature.budget.domain.model

import com.fintrack.shared.feature.transaction.domain.model.Category
import kotlinx.datetime.LocalDate

data class Budget(
    val id: String? = null,
    val accountId: String,
    val name: String,
    val categories: List<Category>,
    val limit: Double,
    val isExpense: Boolean,
    val startDate: LocalDate,
    val endDate: LocalDate
)
