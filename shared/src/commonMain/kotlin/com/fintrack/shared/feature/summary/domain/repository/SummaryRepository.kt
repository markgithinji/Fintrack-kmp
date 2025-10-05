package com.fintrack.shared.feature.summary.domain.repository

import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.summary.domain.model.AvailableMonths
import com.fintrack.shared.feature.summary.domain.model.AvailableWeeks
import com.fintrack.shared.feature.summary.domain.model.AvailableYears
import com.fintrack.shared.feature.summary.domain.model.CategoryComparison
import com.fintrack.shared.feature.summary.domain.model.DistributionSummary
import com.fintrack.shared.feature.summary.domain.model.OverviewSummary
import com.fintrack.shared.feature.summary.domain.model.StatisticsSummary
import com.fintrack.shared.feature.summary.domain.model.TransactionCountSummary

interface SummaryRepository {
    suspend fun getHighlightsSummary(accountId: Int? = null): Result<StatisticsSummary>
    suspend fun getDistributionSummary(
        weekOrMonthCode: String,
        type: String? = null,
        start: String? = null,
        end: String? = null,
        accountId: Int? = null
    ): Result<DistributionSummary>

    suspend fun getAvailableWeeks(accountId: Int? = null): Result<AvailableWeeks>
    suspend fun getAvailableMonths(accountId: Int? = null): Result<AvailableMonths>
    suspend fun getAvailableYears(accountId: Int? = null): Result<AvailableYears>
    suspend fun getOverviewSummary(accountId: Int? = null): Result<OverviewSummary>
    suspend fun getCategoryComparisons(accountId: Int? = null): Result<List<CategoryComparison>>
    suspend fun getTransactionCounts(accountId: Int): Result<TransactionCountSummary>
}