package com.fintrack.shared.feature.budget.domain.model

import com.fintrack.shared.feature.transaction.domain.model.Category
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
