package com.fintrack.shared.feature.transaction.data

import com.fintrack.shared.feature.transaction.model.Transaction
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class TransactionDto(
    val id: Int? = null,
    val isIncome: Boolean,
    val amount: Double,
    val category: String,
    val dateTime: String,
    val description: String? = null
)
