package com.fintrack.shared.feature.core.data.domain

import com.fintrack.shared.feature.transaction.data.model.TransactionDto
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val result: T
)

@Serializable
data class PaginatedTransactionDto(
    val data: List<TransactionDto>,
    val nextCursor: String? = null
)
