package com.fintrack.shared.feature.summary.data.network

import com.fintrack.shared.feature.core.data.domain.ApiResponse
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
import io.ktor.client.request.get
import io.ktor.client.request.parameter


class SummaryApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    // --- Highlights ---
    suspend fun getHighlightsSummary(accountId: String? = null): HighlightsSummaryDto {
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
        accountId: String? = null
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
    suspend fun getAvailableWeeks(accountId: String? = null): AvailableWeeksDto {
        val response: ApiResponse<AvailableWeeksDto> =
            client.get("$baseUrl/transactions/summary/available-weeks") {
                accountId?.let { parameter("accountId", it) }
            }.body()
        return response.result
    }

    suspend fun getAvailableMonths(accountId: String? = null): AvailableMonthsDto {
        val response: ApiResponse<AvailableMonthsDto> =
            client.get("$baseUrl/transactions/summary/available-months") {
                accountId?.let { parameter("accountId", it) }
            }.body()
        return response.result
    }

    suspend fun getAvailableYears(accountId: String? = null): AvailableYearsDto {
        val response: ApiResponse<AvailableYearsDto> =
            client.get("$baseUrl/transactions/summary/available-years") {
                accountId?.let { parameter("accountId", it) }
            }.body()
        return response.result
    }

    // --- Overview ---
    suspend fun getOverviewSummary(accountId: String? = null): OverviewSummaryDto {
        val response: ApiResponse<OverviewSummaryDto> =
            client.get("$baseUrl/transactions/summary/overview") {
                accountId?.let { parameter("accountId", it) }
            }.body()
        return response.result
    }

    suspend fun getCategoryComparisons(accountId: String? = null): List<CategoryComparisonDto> {
        val response: ApiResponse<List<CategoryComparisonDto>> =
            client.get("$baseUrl/transactions/summary/category-comparison") {
                accountId?.let { parameter("accountId", it) }
            }.body()
        return response.result
    }

    suspend fun getTransactionCounts(
        accountId: String,
        isIncome: Boolean? = null
    ): TransactionCountSummaryDto {
        val response: ApiResponse<TransactionCountSummaryDto> =
            client.get("$baseUrl/transactions/summary/counts") {
                parameter("accountId", accountId)
                isIncome?.let { parameter("isIncome", it) }
            }.body()
        return response.result
    }
}