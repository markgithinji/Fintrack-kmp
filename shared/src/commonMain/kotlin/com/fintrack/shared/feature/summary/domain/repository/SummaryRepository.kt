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
    suspend fun getHighlightsSummary(accountId: String?): Result<StatisticsSummary>
    suspend fun getDistributionSummary(
        weekOrMonthCode: String,
        type: String?,
        start: String?,
        end: String?,
        accountId: String?
    ): Result<DistributionSummary>
    suspend fun getAvailableWeeks(accountId: String?): Result<AvailableWeeks>
    suspend fun getAvailableMonths(accountId: String?): Result<AvailableMonths>
    suspend fun getAvailableYears(accountId: String?): Result<AvailableYears>
    suspend fun getOverviewSummary(accountId: String?): Result<OverviewSummary>
    suspend fun getCategoryComparisons(accountId: String?): Result<List<CategoryComparison>>
    suspend fun getTransactionCounts(accountId: String): Result<TransactionCountSummary>
}