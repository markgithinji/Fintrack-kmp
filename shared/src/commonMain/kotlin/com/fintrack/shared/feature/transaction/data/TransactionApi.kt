package com.fintrack.shared.feature.transaction.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class TransactionApi(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            })
        }
    }

    // --- Transactions ---
    suspend fun getTransactions(
        limit: Int = 20,
        sortBy: String = "date",
        order: String = "DESC",
        afterDate: String? = null,
        afterId: Int? = null
    ): PaginatedTransactionDto {
        val response: ApiResponse<PaginatedTransactionDto> =
            client.get("$baseUrl/transactions") {
                parameter("limit", limit)
                parameter("sortBy", sortBy)
                parameter("order", order)
                afterDate?.let { parameter("afterDate", it) }
                afterId?.let { parameter("afterId", it) }
            }.body()

        return response.result
    }

    suspend fun addTransaction(transaction: TransactionDto): TransactionDto {
        val response: ApiResponse<TransactionDto> =
            client.post("$baseUrl/transactions") {
                contentType(ContentType.Application.Json)
                setBody(transaction)
            }.body()

        return response.result
    }

    // --- Summaries ---

    // Highlights summary (overall income/expense + highlights)
    suspend fun getHighlightsSummary(): HighlightsSummaryDto {
        val response: ApiResponse<HighlightsSummaryDto> =
            client.get("$baseUrl/transactions/summary/highlights").body()
        return response.result
    }

    // Distribution summary (category breakdown for a specific period)
    suspend fun getDistributionSummary(
        period: String, // e.g. "week" | "month"
        value: String   // e.g. "2025-W37" | "2025-09"
    ): DistributionSummaryDto {
        val response: ApiResponse<DistributionSummaryDto> =
            client.get("$baseUrl/transactions/summary/distribution") {
                parameter("period", period)
                parameter("value", value)
            }.body()

        return response.result
    }
}
