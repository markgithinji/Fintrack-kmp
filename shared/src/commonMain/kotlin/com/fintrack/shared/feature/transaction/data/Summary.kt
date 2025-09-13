package com.fintrack.shared.feature.transaction.data

import kotlinx.serialization.Serializable

data class Summary(
    val income: Double,
    val expense: Double,
    val balance: Double,
    val incomeHighlights: Highlights,
    val expenseHighlights: Highlights,
    val incomeCategorySummary: CategorySummaries,
    val expenseCategorySummary: CategorySummaries
)

data class Highlights(
    val highestMonth: Highlight?,
    val highestCategory: Highlight?,
    val highestDay: Highlight?,
    val averagePerDay: Double
)

data class CategorySummaries(
    val weekly: Map<String, List<CategorySummary>>,
    val monthly: Map<String, List<CategorySummary>>
)

data class Highlight(
    val label: String,
    val value: String,
    val amount: Double
)

data class CategorySummary(
    val category: String,
    val total: Double,
    val percentage: Double
)
