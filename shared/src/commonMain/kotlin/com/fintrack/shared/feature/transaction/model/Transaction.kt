package com.fintrack.shared.feature.transaction.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: Int? = null,
    val isIncome: Boolean,
    val amount: Double,
    val category: String,
    @Contextual val dateTime: LocalDateTime,
    val description: String? = null
)
