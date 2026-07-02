package com.fintrack.shared.feature.summary.domain.model

data class StatisticsSummary(
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
    val averagePerDay: Double
)

data class Highlight(
    val label: String,
    val value: String,
    val amount: Double
)