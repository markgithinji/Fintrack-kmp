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
    val api = SummaryApi()

    suspend fun getHighlightsSummary(): com.fintrack.shared.feature.core.Result<StatisticsSummary> = try {
        com.fintrack.shared.feature.core.Result.Success(api.getHighlightsSummary().toDomain())
    } catch (e: Exception) {
        com.fintrack.shared.feature.core.Result.Error(e)
    }

    suspend fun getDistributionSummary(
        weekOrMonthCode: String,
        type: String? = null,
        start: String? = null,
        end: String? = null
    ): com.fintrack.shared.feature.core.Result<DistributionSummary> = try {
        com.fintrack.shared.feature.core.Result.Success(api.getDistributionSummary(weekOrMonthCode, type, start, end).toDomain())
    } catch (e: Exception) {
        com.fintrack.shared.feature.core.Result.Error(e)
    }

    suspend fun getAvailableWeeks(): com.fintrack.shared.feature.core.Result<AvailableWeeks> = try {
        val dto = api.getAvailableWeeks()
        com.fintrack.shared.feature.core.Result.Success(dto.toDomain())
    } catch (e: Exception) {
        com.fintrack.shared.feature.core.Result.Error(e)
    }

    suspend fun getAvailableMonths(): com.fintrack.shared.feature.core.Result<AvailableMonths> = try {
        com.fintrack.shared.feature.core.Result.Success(api.getAvailableMonths().toDomain())
    } catch (e: Exception) {
        com.fintrack.shared.feature.core.Result.Error(e)
    }

    suspend fun getAvailableYears(): com.fintrack.shared.feature.core.Result<AvailableYears> = try {
        com.fintrack.shared.feature.core.Result.Success(api.getAvailableYears().toDomain())
    } catch (e: Exception) {
        com.fintrack.shared.feature.core.Result.Error(e)
    }

    suspend fun getOverviewSummary(): com.fintrack.shared.feature.core.Result<OverviewSummary> = try {
        com.fintrack.shared.feature.core.Result.Success(api.getOverviewSummary().toDomain())
    } catch (e: Exception) {
        com.fintrack.shared.feature.core.Result.Error(e)
    }

    suspend fun getCategoryComparisons(): com.fintrack.shared.feature.core.Result<List<CategoryComparison>> = try {
        com.fintrack.shared.feature.core.Result.Success(api.getCategoryComparisons().map { it.toDomain() })
    } catch (e: Exception) {
        Result.Error(e)
    }
}