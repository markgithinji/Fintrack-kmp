package com.fintrack.shared.feature.transaction.data.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class CreateTransactionRequest(
    val accountId: String,
    val isIncome: Boolean,
    val amount: Double,
    val transactionCost: Double = 0.0,
    val category: String,
    val dateTime: Instant,
    val description: String? = "",
    val externalId: String? = null,
    val balance: Double? = null
)
