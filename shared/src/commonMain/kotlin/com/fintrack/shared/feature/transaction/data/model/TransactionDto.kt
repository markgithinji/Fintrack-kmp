package com.fintrack.shared.feature.transaction.data.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class TransactionDto(
    val id: String? = null,
    val accountId: String,
    val isIncome: Boolean,
    val amount: Double,
    val transactionCost: Double = 0.0,
    val category: String,
    val categoryId: String? = null,
    val dateTime: Instant,
    val description: String? = null,
    val externalId: String? = null,
    val balance: Double? = null
)