package com.fintrack.shared.feature.transaction.model

import kotlinx.datetime.LocalDate

data class Budget(
    val id: Int? = null,
    val name: String,
    val categories: List<Category>,
    val limit: Double,
    val isExpense: Boolean,
    val startDate: LocalDate,
    val endDate: LocalDate
)
