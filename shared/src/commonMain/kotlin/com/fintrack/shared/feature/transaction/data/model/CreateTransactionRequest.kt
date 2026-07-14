package com.fintrack.shared.feature.transaction.data.model

import com.fintrack.shared.feature.core.data.serialization.BigDecimalSerializer
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class CreateTransactionRequest(
    val accountId: String,
    val isIncome: Boolean,
    @Serializable(with = BigDecimalSerializer::class)
    val amount: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val transactionCost: BigDecimal = BigDecimal.ZERO,
    val category: String,
    val categoryId: String? = null,
    val dateTime: Instant,
    val description: String? = "",
    val externalId: String? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val balance: BigDecimal? = null
)
