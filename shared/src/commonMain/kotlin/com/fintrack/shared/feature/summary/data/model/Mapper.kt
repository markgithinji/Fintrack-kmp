package com.fintrack.shared.feature.summary.data.model

import com.fintrack.shared.feature.summary.domain.model.AvailableMonths
import com.fintrack.shared.feature.summary.domain.model.AvailableWeeks
import com.fintrack.shared.feature.summary.domain.model.AvailableYears
import com.fintrack.shared.feature.summary.domain.model.CategoryComparison
import com.fintrack.shared.feature.summary.domain.model.CategorySummary
import com.fintrack.shared.feature.summary.domain.model.DaySummary
import com.fintrack.shared.feature.summary.domain.model.DistributionSummary
import com.fintrack.shared.feature.summary.domain.model.Highlight
import com.fintrack.shared.feature.summary.domain.model.Highlights
import com.fintrack.shared.feature.summary.domain.model.OverviewSummary
import com.fintrack.shared.feature.summary.domain.model.StatisticsSummary
import com.fintrack.shared.feature.summary.domain.model.TransactionCountSummary

// --- Highlight Summary ---
fun HighlightDto.toDomain(): Highlight =
    Highlight(label.ifEmpty { "-" }, value.ifEmpty { "-" }, amount)

fun HighlightsDto.toDomain(): Highlights =
    Highlights(
        highestMonth = highestMonth?.toDomain(),
        highestCategory = highestCategory?.toDomain(),
        highestDay = highestDay?.toDomain(),
        averagePerDay = averagePerDay
    )

fun HighlightsSummaryDto.toDomain(): StatisticsSummary =
    StatisticsSummary(
        incomeHighlights = incomeHighlights.toDomain(),
        expenseHighlights = expenseHighlights.toDomain()
    )

// --- Distribution Summary ---
fun CategorySummaryDto.toDomain(): CategorySummary =
    CategorySummary(category, total, percentage)

fun DistributionSummaryDto.toDomain(): DistributionSummary =
    DistributionSummary(
        period = period,
        incomeCategories = incomeCategories.map { it.toDomain() },
        expenseCategories = expenseCategories.map { it.toDomain() }
    )

// Overview Summary
fun OverviewSummaryDto.toDomain() = OverviewSummary(
    weeklyOverview = weeklyOverview.map { it.toDomain() },
    monthlyOverview = monthlyOverview.map { it.toDomain() }
)

fun DaySummaryDto.toDomain() = DaySummary(
    date = date,
    income = income,
    expense = expense
)

// Category comparison
fun CategoryComparisonDto.toDomain() = CategoryComparison(
    period = period,
    category = category,
    currentTotal = currentTotal,
    previousTotal = previousTotal,
    changePercentage = changePercentage
)

// Available Weeks
fun AvailableWeeksDto.toDomain() = AvailableWeeks(weeks = weeks)

// Available Months
fun AvailableMonthsDto.toDomain() = AvailableMonths(months = this.months)

// Available Years
fun AvailableYearsDto.toDomain() = AvailableYears(years)

// Transaction Count Summary
fun TransactionCountSummaryDto.toDomain() = TransactionCountSummary(
    totalIncomeTransactions = totalIncomeTransactions,
    totalExpenseTransactions = totalExpenseTransactions,
    totalTransactions = totalTransactions
)

