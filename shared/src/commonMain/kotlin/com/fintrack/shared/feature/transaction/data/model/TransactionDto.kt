package com.fintrack.shared.feature.transaction.data.model

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class TransactionDto @OptIn(ExperimentalTime::class) constructor(
    val id: String? = null,
    val accountId: String,
    val isIncome: Boolean,
    val amount: Double,
    val transactionCost: Double = 0.0,
    val category: String,
    val dateTime: Instant,
    val description: String? = null,
    val externalId: String? = null,
    val balance: Double? = null
)