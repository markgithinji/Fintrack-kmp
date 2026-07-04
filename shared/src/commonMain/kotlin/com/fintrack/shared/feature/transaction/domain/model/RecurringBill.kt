package com.fintrack.shared.feature.transaction.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class RecurringBill(
    val id: String,
    val name: String,
    val amount: Double,
    val category: String,
    val frequency: String, // "Monthly", "Weekly", etc.
    val nextDueDate: LocalDate,
    val isActive: Boolean = true
)
