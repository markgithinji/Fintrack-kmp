package com.fintrack.shared.feature.summary.data.model

import kotlinx.serialization.Serializable

@Serializable
data class OverviewSummaryDto(
    val period: String = "",
    val isCurrent: Boolean = true,
    val weeklyOverview: List<DaySummaryDto>,
    val monthlyOverview: List<DaySummaryDto>
)

@Serializable
data class DaySummaryDto(
    val date: String,
    val income: Double,
    val expense: Double
)
