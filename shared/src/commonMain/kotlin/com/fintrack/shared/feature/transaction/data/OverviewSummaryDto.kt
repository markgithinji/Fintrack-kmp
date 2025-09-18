package com.fintrack.shared.feature.transaction.data

import kotlinx.serialization.Serializable

@Serializable
data class OverviewSummaryDto(
    val weeklyOverview: List<DaySummaryDto>,
    val monthlyOverview: List<DaySummaryDto>
)

@Serializable
data class DaySummaryDto(
    val date: String,
    val income: Double,
    val expense: Double
)

// Domain models
data class OverviewSummary(
    val weeklyOverview: List<DaySummary>,
    val monthlyOverview: List<DaySummary>
)

data class DaySummary(
    val date: String,
    val income: Double,
    val expense: Double
)

// Mapper
fun OverviewSummaryDto.toDomain() = OverviewSummary(
    weeklyOverview = weeklyOverview.map { it.toDomain() },
    monthlyOverview = monthlyOverview.map { it.toDomain() }
)

fun DaySummaryDto.toDomain() = DaySummary(
    date = date,
    income = income,
    expense = expense
)