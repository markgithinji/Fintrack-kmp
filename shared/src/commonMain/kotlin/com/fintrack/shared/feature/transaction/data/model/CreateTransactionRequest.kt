package com.fintrack.shared.feature.transaction.data.model

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class CreateTransactionRequest @OptIn(ExperimentalTime::class) constructor(
    val accountId: String,
    val isIncome: Boolean,
    val amount: Double,
    val transactionCost: Double = 0.0,
    val category: String,
    val dateTime: Instant,
    val description: String? = ""
)
