package com.fintrack.shared.feature.transaction.data

import kotlinx.serialization.Serializable

@Serializable
data class SummaryDto(
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val balance: Double = 0.0,
    val incomeHighlights: HighlightsDto = HighlightsDto(),
    val expenseHighlights: HighlightsDto = HighlightsDto(),
    val incomeCategorySummary: CategorySummariesDto = CategorySummariesDto(),
    val expenseCategorySummary: CategorySummariesDto = CategorySummariesDto()
)

@Serializable
data class HighlightsDto(
    val highestMonth: HighlightDto? = null,
    val highestCategory: HighlightDto? = null,
    val highestDay: HighlightDto? = null,
    val averagePerDay: Double = 0.0
)

@Serializable
data class CategorySummariesDto(
    val weekly: Map<String, List<CategorySummaryDto>> = emptyMap(),
    val monthly: Map<String, List<CategorySummaryDto>> = emptyMap()
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

// --- Domain mapping ---

fun HighlightDto.toDomain(): Highlight =
    Highlight(
        label = label.ifEmpty { "-" },
        value = value.ifEmpty { "-" },
        amount = amount
    )

fun CategorySummaryDto.toDomain(): CategorySummary =
    CategorySummary(category, total, percentage)

fun HighlightsDto.toDomain(): Highlights =
    Highlights(
        highestMonth = highestMonth?.toDomain(),
        highestCategory = highestCategory?.toDomain(),
        highestDay = highestDay?.toDomain(),
        averagePerDay = averagePerDay
    )

fun CategorySummariesDto.toDomain(): CategorySummaries =
    CategorySummaries(
        weekly = weekly.mapValues { it.value.map { cs -> cs.toDomain() } },
        monthly = monthly.mapValues { it.value.map { cs -> cs.toDomain() } }
    )

fun SummaryDto.toDomain(): Summary = Summary(
    income = income,
    expense = expense,
    balance = balance,
    incomeHighlights = incomeHighlights.toDomain(),
    expenseHighlights = expenseHighlights.toDomain(),
    incomeCategorySummary = incomeCategorySummary.toDomain(),
    expenseCategorySummary = expenseCategorySummary.toDomain()
)
