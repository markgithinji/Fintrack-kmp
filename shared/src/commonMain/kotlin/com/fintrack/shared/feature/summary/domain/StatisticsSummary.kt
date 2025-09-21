package com.fintrack.shared.feature.summary.domain

data class StatisticsSummary(
    val incomeHighlights: Highlights,
    val expenseHighlights: Highlights
)

data class Highlights(
    val highestMonth: Highlight?,
    val highestCategory: Highlight?,
    val highestDay: Highlight?,
    val averagePerDay: Double
)

data class Highlight(
    val label: String,
    val value: String,
    val amount: Double
)