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
            json(Json { ignoreUnknownKeys = true; explicitNulls = false })
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
        val response: ApiResponse<PaginatedTransactionDto> = client.get("$baseUrl/transactions") {
            parameter("limit", limit)
            parameter("sortBy", sortBy)
            parameter("order", order)
            afterDate?.let { parameter("afterDate", it) }
            afterId?.let { parameter("afterId", it) }
        }.body()
        return response.result
    }

    suspend fun addTransaction(transaction: TransactionDto): TransactionDto {
        val response: ApiResponse<TransactionDto> = client.post("$baseUrl/transactions") {
            contentType(ContentType.Application.Json)
            setBody(transaction)
        }.body()
        return response.result
    }

    // --- Summaries ---

    suspend fun getHighlightsSummary(): HighlightsSummaryDto {
        val response: ApiResponse<HighlightsSummaryDto> =
            client.get("$baseUrl/transactions/summary/highlights").body()
        return response.result
    }

    suspend fun getDistributionSummary(
        weekOrMonthCode: String,          // e.g., "2025-W37" or "2025-09"
        type: String? = null,             // "income" | "expense"
        start: String? = null,            // optional start date "YYYY-MM-DD"
        end: String? = null               // optional end date "YYYY-MM-DD"
    ): DistributionSummaryDto {
        val response: ApiResponse<DistributionSummaryDto> =
            client.get("$baseUrl/transactions/summary/distribution") {
                parameter("period", weekOrMonthCode)
                type?.let { parameter("type", it) }
                start?.let { parameter("start", it) }
                end?.let { parameter("end", it) }
            }.body()

        return response.result
    }

    // --- Available Weeks ---
    suspend fun getAvailableWeeks(): AvailableWeeksDto {
        val response: ApiResponse<AvailableWeeksDto> =
            client.get("$baseUrl/transactions/available-weeks").body()
        return response.result
    }

    // --- Available Months ---
    suspend fun getAvailableMonths(): AvailableMonthsDto {
        val response: ApiResponse<AvailableMonthsDto> =
            client.get("$baseUrl/transactions/available-months").body()
        return response.result
    }

    suspend fun getAvailableYears(): AvailableYearsDto {
        val response: ApiResponse<AvailableYearsDto> =
            client.get("$baseUrl/transactions/available-years").body()
        return response.result
    }

}
