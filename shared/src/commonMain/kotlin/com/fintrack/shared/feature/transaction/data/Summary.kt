package com.fintrack.shared.feature.transaction.data

import kotlinx.serialization.Serializable

data class Summary(
    val income: Double,
    val expense: Double,
    val balance: Double,

    // Expense highlights
    val highestMonth: Highlight?,
    val highestCategory: Highlight?,
    val highestDay: Highlight?,
    val averagePerDay: Double,

    // Income highlights
    val highestIncomeMonth: Highlight?,
    val highestIncomeCategory: Highlight?,
    val highestIncomeDay: Highlight?,
    val averageIncomePerDay: Double,

    val weeklyCategorySummary: Map<String, List<CategorySummary>>,
    val monthlyCategorySummary: Map<String, List<CategorySummary>>
)

data class Highlight(
    val label: String,
    val value: String,
    val amount: Double
)

@Serializable
data class CategorySummary(
    val category: String,
    val total: Double,
    val percentage: Double
)

