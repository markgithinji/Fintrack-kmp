package com.fintrack.shared.feature.summary.domain


data class DistributionSummary(
    val period: String, // e.g. "2025-W37" or "2025-09"
    val incomeCategories: List<CategorySummary>,
    val expenseCategories: List<CategorySummary>
)


data class CategorySummary(
    val category: String,
    val total: Double,
    val percentage: Double
)
