package com.fintrack.shared.feature.transaction.data

import kotlinx.serialization.Serializable

data class HighlightsSummary(
    val income: Double,
    val expense: Double,
    val balance: Double,
    val incomeHighlights: Highlights,
    val expenseHighlights: Highlights
)

data class DistributionSummary(
    val period: String, // e.g. "2025-W37" or "2025-09"
    val incomeCategories: List<CategorySummary>,
    val expenseCategories: List<CategorySummary>
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

data class CategorySummary(
    val category: String,
    val total: Double,
    val percentage: Double
)
