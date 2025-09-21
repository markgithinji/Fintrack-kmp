package com.fintrack.shared.feature.summary.data.network

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

class SummaryApi(
    private val baseUrl: String = ApiConfig.BASE_URL,
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; explicitNulls = false })
        }
        defaultRequest {
            SessionManager.token?.let { header("Authorization", "Bearer $it") }
        }
    }

    // --- Highlights ---
    suspend fun getHighlightsSummary(): HighlightsSummaryDto {
        val response: ApiResponse<HighlightsSummaryDto> =
            client.get("$baseUrl/transactions/summary/highlights").body()
        return response.result
    }

    // --- Distribution ---
    suspend fun getDistributionSummary(
        period: String,
        type: String? = null,
        start: String? = null,
        end: String? = null
    ): DistributionSummaryDto {
        val response: ApiResponse<DistributionSummaryDto> =
            client.get("$baseUrl/transactions/summary/distribution") {
                parameter("period", period)
                type?.let { parameter("type", it) }
                start?.let { parameter("start", it) }
                end?.let { parameter("end", it) }
            }.body()
        return response.result
    }

    // --- Available ranges ---
    suspend fun getAvailableWeeks(): AvailableWeeksDto {
        val response: ApiResponse<AvailableWeeksDto> =
            client.get("$baseUrl/transactions/summary/available-weeks").body()
        return response.result
    }

    suspend fun getAvailableMonths(): AvailableMonthsDto {
        val response: ApiResponse<AvailableMonthsDto> =
            client.get("$baseUrl/transactions/summary/available-months").body()
        return response.result
    }

    suspend fun getAvailableYears(): AvailableYearsDto {
        val response: ApiResponse<AvailableYearsDto> =
            client.get("$baseUrl/transactions/summary/available-years").body()
        return response.result
    }

    // --- Overview ---
    suspend fun getOverviewSummary(): OverviewSummaryDto {
        val response: ApiResponse<OverviewSummaryDto> =
            client.get("$baseUrl/transactions/summary/overview").body()
        return response.result
    }

    suspend fun getCategoryComparisons(): List<CategoryComparisonDto> {
        val response: ApiResponse<List<CategoryComparisonDto>> =
            client.get("$baseUrl/transactions/summary/category-comparison").body()
        return response.result
    }
}
