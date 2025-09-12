package com.fintrack.shared.feature.transaction.data

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
