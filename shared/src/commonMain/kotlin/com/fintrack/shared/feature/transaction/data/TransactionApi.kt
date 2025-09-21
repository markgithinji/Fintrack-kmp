package com.fintrack.shared.feature.transaction.data

import com.fintrack.shared.feature.auth.data.SessionManager
import com.fintrack.shared.feature.core.ApiConfig
import com.fintrack.shared.feature.core.ApiResponse
import com.fintrack.shared.feature.core.PaginatedTransactionDto
import com.fintrack.shared.feature.summary.data.model.AvailableMonthsDto
import com.fintrack.shared.feature.summary.data.model.AvailableWeeksDto
import com.fintrack.shared.feature.summary.data.model.AvailableYearsDto
import com.fintrack.shared.feature.summary.data.model.CategoryComparisonDto
import com.fintrack.shared.feature.summary.data.model.DistributionSummaryDto
import com.fintrack.shared.feature.summary.data.model.HighlightsSummaryDto
import com.fintrack.shared.feature.summary.data.model.OverviewSummaryDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class TransactionApi(
    private val baseUrl: String = ApiConfig.BASE_URL,
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; explicitNulls = false })
        }

        // Automatically add Authorization header for every request
        defaultRequest {
            SessionManager.token?.let {
                header("Authorization", "Bearer $it")
            }
        }
    }

    // --- Transactions ---
    suspend fun getTransactions(
        limit: Int = 20,
        sortBy: String = "date",
        order: String = "DESC",
        afterDate: String? = null,
        afterId: Int? = null,
        accountId: Int? = null // 👈 new optional parameter
    ): PaginatedTransactionDto {
        val response: ApiResponse<PaginatedTransactionDto> = client.get("$baseUrl/transactions") {
            parameter("limit", limit)
            parameter("sortBy", sortBy)
            parameter("order", order)
            afterDate?.let { parameter("afterDate", it) }
            afterId?.let { parameter("afterId", it) }
            accountId?.let { parameter("accountId", it) }
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
}
