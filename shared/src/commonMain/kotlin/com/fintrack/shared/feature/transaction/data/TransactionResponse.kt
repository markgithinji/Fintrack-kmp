package com.fintrack.shared.feature.transaction.data

import com.fintrack.shared.feature.transaction.model.Transaction
import kotlinx.serialization.Serializable

@Serializable
data class TransactionResponse(
    val data: List<TransactionDto>
)
