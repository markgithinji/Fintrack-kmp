package com.fintrack.shared.feature.summary.domain

data class OverviewSummary(
    val weeklyOverview: List<DaySummary>,
    val monthlyOverview: List<DaySummary>
)

data class DaySummary(
    val date: String,
    val income: Double,
    val expense: Double
)
