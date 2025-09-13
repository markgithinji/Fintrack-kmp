package com.fintrack.shared.feature.transaction.data

import kotlinx.serialization.Serializable

@Serializable
data class SummaryDto(
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val balance: Double = 0.0,

    // Expense highlights
    val highestMonth: HighlightDto? = null,
    val highestCategory: HighlightDto? = null,
    val highestDay: HighlightDto? = null,
    val averagePerDay: Double = 0.0,

    // Income highlights
    val highestIncomeMonth: HighlightDto? = null,
    val highestIncomeCategory: HighlightDto? = null,
    val highestIncomeDay: HighlightDto? = null,
    val averageIncomePerDay: Double = 0.0,

    // Expense categories
    val weeklyCategorySummary: Map<String, List<CategorySummaryDto>> = emptyMap(),
    val monthlyCategorySummary: Map<String, List<CategorySummaryDto>> = emptyMap(),

    // Income categories
    val weeklyIncomeCategorySummary: Map<String, List<CategorySummaryDto>> = emptyMap(),
    val monthlyIncomeCategorySummary: Map<String, List<CategorySummaryDto>> = emptyMap()
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

fun SummaryDto.toDomain(): Summary = Summary(
    income = income,
    expense = expense,
    balance = balance,
    highestMonth = highestMonth?.toDomain(),
    highestCategory = highestCategory?.toDomain(),
    highestDay = highestDay?.toDomain(),
    averagePerDay = averagePerDay,
    highestIncomeMonth = highestIncomeMonth?.toDomain(),
    highestIncomeCategory = highestIncomeCategory?.toDomain(),
    highestIncomeDay = highestIncomeDay?.toDomain(),
    averageIncomePerDay = averageIncomePerDay,
    weeklyCategorySummary = weeklyCategorySummary.mapValues { it.value.map { cs -> cs.toDomain() } },
    monthlyCategorySummary = monthlyCategorySummary.mapValues { it.value.map { cs -> cs.toDomain() } },
    weeklyIncomeCategorySummary = weeklyIncomeCategorySummary.mapValues { it.value.map { cs -> cs.toDomain() } },
    monthlyIncomeCategorySummary = monthlyIncomeCategorySummary.mapValues { it.value.map { cs -> cs.toDomain() } }
)