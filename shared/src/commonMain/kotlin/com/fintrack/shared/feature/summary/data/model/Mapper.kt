package com.fintrack.shared.feature.summary.data.model

import com.fintrack.shared.feature.summary.domain.AvailableMonths
import com.fintrack.shared.feature.summary.domain.AvailableWeeks
import com.fintrack.shared.feature.summary.domain.AvailableYears
import com.fintrack.shared.feature.summary.domain.CategoryComparison
import com.fintrack.shared.feature.summary.domain.CategorySummary
import com.fintrack.shared.feature.summary.domain.DaySummary
import com.fintrack.shared.feature.summary.domain.DistributionSummary
import com.fintrack.shared.feature.summary.domain.Highlight
import com.fintrack.shared.feature.summary.domain.Highlights
import com.fintrack.shared.feature.summary.domain.HighlightsSummary
import com.fintrack.shared.feature.summary.domain.OverviewSummary

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

fun HighlightsSummaryDto.toDomain(): HighlightsSummary =
    HighlightsSummary(
        income = income,
        expense = expense,
        balance = balance,
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

//
fun AvailableWeeksDto.toDomain() = AvailableWeeks(weeks = weeks)

// Available Months
fun AvailableMonthsDto.toDomain() = AvailableMonths(months = this.months)

// Available Years
fun AvailableYearsDto.toDomain() = AvailableYears(years)
