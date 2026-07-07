package com.fintrack.shared.feature.summary.domain.model

data class StatisticsSummary(
    val period: String = "",
    val isCurrent: Boolean = true,
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val balance: Double = 0.0,
    val totalTransactionCost: Double = 0.0,
    val incomeHighlights: Highlights,
    val expenseHighlights: Highlights
)

data class Highlights(
    val highestMonth: Highlight?,
    val highestCategory: Highlight?,
    val highestDay: Highlight?,
    val averagePerDay: Double,
    val ytdChangePercentage: Double? = null,
    val projectedTotal: Double? = null,
    val savingsRate: Double? = null,
    val essentialSpendRatio: Double? = null,
    val projectedExceedMonth: String? = null,
    val correlations: List<Correlation>? = null
)

data class Correlation(
    val source: String,
    val target: String,
    val insight: String
)

data class Highlight(
    val label: String,
    val value: String,
    val amount: Double,
    val volatilityPercentage: Double? = null
)
