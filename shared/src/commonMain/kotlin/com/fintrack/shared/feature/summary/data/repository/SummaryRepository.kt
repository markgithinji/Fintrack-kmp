package com.fintrack.shared.feature.summary.data.repository

import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.summary.data.model.toDomain
import com.fintrack.shared.feature.summary.data.network.SummaryApi
import com.fintrack.shared.feature.summary.domain.model.AvailableMonths
import com.fintrack.shared.feature.summary.domain.model.AvailableWeeks
import com.fintrack.shared.feature.summary.domain.model.AvailableYears
import com.fintrack.shared.feature.summary.domain.model.CategoryComparison
import com.fintrack.shared.feature.summary.domain.model.DistributionSummary
import com.fintrack.shared.feature.summary.domain.model.OverviewSummary
import com.fintrack.shared.feature.summary.domain.model.StatisticsSummary
import com.fintrack.shared.feature.summary.domain.model.TransactionCountSummary
import com.fintrack.shared.feature.summary.domain.repository.SummaryRepository

class SummaryRepositoryImpl(
    private val api: SummaryApi = SummaryApi()
) : SummaryRepository {

    override suspend fun getHighlightsSummary(accountId: Int?): Result<StatisticsSummary> = try {
        Result.Success(api.getHighlightsSummary(accountId).toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun getDistributionSummary(
        weekOrMonthCode: String,
        type: String?,
        start: String?,
        end: String?,
        accountId: Int?
    ): Result<DistributionSummary> = try {
        Result.Success(
            api.getDistributionSummary(weekOrMonthCode, type, start, end, accountId).toDomain()
        )
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun getAvailableWeeks(accountId: Int?): Result<AvailableWeeks> = try {
        val dto = api.getAvailableWeeks(accountId)
        Result.Success(dto.toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun getAvailableMonths(accountId: Int?): Result<AvailableMonths> = try {
        Result.Success(api.getAvailableMonths(accountId).toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun getAvailableYears(accountId: Int?): Result<AvailableYears> = try {
        Result.Success(api.getAvailableYears(accountId).toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun getOverviewSummary(accountId: Int?): Result<OverviewSummary> = try {
        Result.Success(api.getOverviewSummary(accountId).toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun getCategoryComparisons(accountId: Int?): Result<List<CategoryComparison>> =
        try {
            Result.Success(api.getCategoryComparisons(accountId).map { it.toDomain() })
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun getTransactionCounts(accountId: Int): Result<TransactionCountSummary> = try {
        Result.Success(api.getTransactionCounts(accountId).toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }
}