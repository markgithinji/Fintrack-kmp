package com.fintrack.shared.feature.budget.domain

import com.fintrack.shared.feature.transaction.model.Category
import kotlinx.datetime.LocalDate

data class Budget(
    val id: Int? = null,
    val accountId: Int,
    val name: String,
    val categories: List<Category>,
    val limit: Double,
    val isExpense: Boolean,
    val startDate: LocalDate,
    val endDate: LocalDate
)
