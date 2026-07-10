package com.fintrack.shared.feature.transaction.domain.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Transaction(
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
) {
    /**
     * The final amount that impacts the account balance.
     * For expenses, it's the amount plus the transaction cost.
     * For income, it's the amount minus the transaction cost (net income).
     */
    val totalAmount: Double
        get() = if (isIncome) amount - transactionCost else amount + transactionCost
}
