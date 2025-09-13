package com.fintrack.shared.feature.transaction.data

import kotlinx.serialization.Serializable

@Serializable
data class HighlightsSummaryDto(
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val balance: Double = 0.0,
    val incomeHighlights: HighlightsDto = HighlightsDto(),
    val expenseHighlights: HighlightsDto = HighlightsDto()
)

@Serializable
data class DistributionSummaryDto(
    val period: String = "", // e.g. "2025-W37" or "2025-09"
    val incomeCategories: List<CategorySummaryDto> = emptyList(),
    val expenseCategories: List<CategorySummaryDto> = emptyList()
)

@Serializable
data class HighlightsDto(
    val highestMonth: HighlightDto? = null,
    val highestCategory: HighlightDto? = null,
    val highestDay: HighlightDto? = null,
    val averagePerDay: Double = 0.0
)

@Serializable
data class HighlightDto(
    val label: String = "",
    val value: String = "",
    val amount: Double = 0.0
)



@Serializable
data class CategorySummaryDto(
    val category: String = "",
    val total: Double = 0.0,
    val percentage: Double = 0.0
)

// --- Highlights ---
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

// --- Distribution ---
fun CategorySummaryDto.toDomain(): CategorySummary =
    CategorySummary(category, total, percentage)

fun DistributionSummaryDto.toDomain(): DistributionSummary =
    DistributionSummary(
        period = period,
        incomeCategories = incomeCategories.map { it.toDomain() },
        expenseCategories = expenseCategories.map { it.toDomain() }
    )
