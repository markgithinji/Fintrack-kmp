package com.fintrack.shared.feature.summary.data.network

import com.fintrack.shared.feature.auth.data.local.TokenProvider
import com.fintrack.shared.feature.auth.data.local.TokenProviderImpl
import com.fintrack.shared.feature.auth.data.repository.TokenRepository
import com.fintrack.shared.feature.auth.data.local.createTokenDataStore
import com.fintrack.shared.feature.core.ApiClient
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
import com.fintrack.shared.feature.summary.data.model.TransactionCountSummaryDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.plugin
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json


class SummaryApi(
    private val client: HttpClient = ApiClient.authenticatedClient,
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    // --- Highlights ---
    suspend fun getHighlightsSummary(accountId: Int? = null): HighlightsSummaryDto {
        val response: ApiResponse<HighlightsSummaryDto> =
            client.get("$baseUrl/transactions/summary/highlights") {
                accountId?.let { parameter("accountId", it) }
            }.body()
        return response.result
    }

    // --- Distribution ---
    suspend fun getDistributionSummary(
        period: String,
        type: String? = null,
        start: String? = null,
        end: String? = null,
        accountId: Int? = null
    ): DistributionSummaryDto {
        val response: ApiResponse<DistributionSummaryDto> =
            client.get("$baseUrl/transactions/summary/distribution") {
                parameter("period", period)
                type?.let { parameter("type", it) }
                start?.let { parameter("start", it) }
                end?.let { parameter("end", it) }
                accountId?.let { parameter("accountId", it) }
            }.body()
        return response.result
    }

    // --- Available ranges ---
    suspend fun getAvailableWeeks(accountId: Int? = null): AvailableWeeksDto {
        val response: ApiResponse<AvailableWeeksDto> =
            client.get("$baseUrl/transactions/summary/available-weeks") {
                accountId?.let { parameter("accountId", it) }
            }.body()
        return response.result
    }

    suspend fun getAvailableMonths(accountId: Int? = null): AvailableMonthsDto {
        val response: ApiResponse<AvailableMonthsDto> =
            client.get("$baseUrl/transactions/summary/available-months") {
                accountId?.let { parameter("accountId", it) }
            }.body()
        return response.result
    }

    suspend fun getAvailableYears(accountId: Int? = null): AvailableYearsDto {
        val response: ApiResponse<AvailableYearsDto> =
            client.get("$baseUrl/transactions/summary/available-years") {
                accountId?.let { parameter("accountId", it) }
            }.body()
        return response.result
    }

    // --- Overview ---
    suspend fun getOverviewSummary(accountId: Int? = null): OverviewSummaryDto {
        val response: ApiResponse<OverviewSummaryDto> =
            client.get("$baseUrl/transactions/summary/overview") {
                accountId?.let { parameter("accountId", it) }
            }.body()
        return response.result
    }

    suspend fun getCategoryComparisons(accountId: Int? = null): List<CategoryComparisonDto> {
        val response: ApiResponse<List<CategoryComparisonDto>> =
            client.get("$baseUrl/transactions/summary/category-comparison") {
                accountId?.let { parameter("accountId", it) }
            }.body()
        return response.result
    }

    suspend fun getTransactionCounts(accountId: Int): TransactionCountSummaryDto {
        val response: ApiResponse<TransactionCountSummaryDto> =
            client.get("$baseUrl/transactions/summary/counts") {
                parameter("accountId", accountId)
            }.body()
        return response.result
    }
}