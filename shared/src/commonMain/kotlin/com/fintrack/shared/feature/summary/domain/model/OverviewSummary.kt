package com.fintrack.shared.feature.summary.domain.model

data class OverviewSummary(
    val period: String = "",
    val isCurrent: Boolean = true,
    val weeklyOverview: List<DaySummary>,
    val monthlyOverview: List<DaySummary>
)

data class DaySummary(
    val date: String,
    val income: Double,
    val expense: Double
)
