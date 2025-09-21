package com.fintrack.shared.feature.summary.data.repository

import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.summary.data.model.toDomain
import com.fintrack.shared.feature.summary.data.network.SummaryApi
import com.fintrack.shared.feature.summary.domain.AvailableMonths
import com.fintrack.shared.feature.summary.domain.AvailableWeeks
import com.fintrack.shared.feature.summary.domain.AvailableYears
import com.fintrack.shared.feature.summary.domain.CategoryComparison
import com.fintrack.shared.feature.summary.domain.DistributionSummary
import com.fintrack.shared.feature.summary.domain.OverviewSummary
import com.fintrack.shared.feature.summary.domain.StatisticsSummary

class SummaryRepository {
    private val api = SummaryApi()

    suspend fun getHighlightsSummary(accountId: Int? = null): Result<StatisticsSummary> = try {
        Result.Success(api.getHighlightsSummary(accountId).toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getDistributionSummary(
        weekOrMonthCode: String,
        type: String? = null,
        start: String? = null,
        end: String? = null,
        accountId: Int? = null
    ): Result<DistributionSummary> = try {
        Result.Success(
            api.getDistributionSummary(weekOrMonthCode, type, start, end, accountId).toDomain()
        )
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getAvailableWeeks(accountId: Int? = null): Result<AvailableWeeks> = try {
        val dto = api.getAvailableWeeks(accountId)
        Result.Success(dto.toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getAvailableMonths(accountId: Int? = null): Result<AvailableMonths> = try {
        Result.Success(api.getAvailableMonths(accountId).toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getAvailableYears(accountId: Int? = null): Result<AvailableYears> = try {
        Result.Success(api.getAvailableYears(accountId).toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getOverviewSummary(accountId: Int? = null): Result<OverviewSummary> = try {
        Result.Success(api.getOverviewSummary(accountId).toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getCategoryComparisons(accountId: Int? = null): Result<List<CategoryComparison>> =
        try {
            Result.Success(api.getCategoryComparisons(accountId).map { it.toDomain() })
        } catch (e: Exception) {
            Result.Error(e)
        }
}
