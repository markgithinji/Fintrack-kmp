package com.fintrack.shared.feature.summary.data.repository

import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.core.safeApiCall
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
    private val api: SummaryApi
) : SummaryRepository {

    override suspend fun getHighlightsSummary(accountId: Int?): Result<StatisticsSummary> =
        safeApiCall {
            api.getHighlightsSummary(accountId).toDomain()
        }

    override suspend fun getDistributionSummary(
        weekOrMonthCode: String,
        type: String?,
        start: String?,
        end: String?,
        accountId: Int?
    ): Result<DistributionSummary> =
        safeApiCall {
            api.getDistributionSummary(weekOrMonthCode, type, start, end, accountId).toDomain()
        }

    override suspend fun getAvailableWeeks(accountId: Int?): Result<AvailableWeeks> =
        safeApiCall {
            api.getAvailableWeeks(accountId).toDomain()
        }

    override suspend fun getAvailableMonths(accountId: Int?): Result<AvailableMonths> =
        safeApiCall {
            api.getAvailableMonths(accountId).toDomain()
        }

    override suspend fun getAvailableYears(accountId: Int?): Result<AvailableYears> =
        safeApiCall {
            api.getAvailableYears(accountId).toDomain()
        }

    override suspend fun getOverviewSummary(accountId: Int?): Result<OverviewSummary> =
        safeApiCall {
            api.getOverviewSummary(accountId).toDomain()
        }

    override suspend fun getCategoryComparisons(accountId: Int?): Result<List<CategoryComparison>> =
        safeApiCall {
            api.getCategoryComparisons(accountId).map { it.toDomain() }
        }

    override suspend fun getTransactionCounts(accountId: Int): Result<TransactionCountSummary> =
        safeApiCall {
            api.getTransactionCounts(accountId).toDomain()
        }
}