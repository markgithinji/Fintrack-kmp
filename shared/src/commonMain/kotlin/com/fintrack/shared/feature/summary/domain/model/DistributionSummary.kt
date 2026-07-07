package com.fintrack.shared.feature.summary.domain.model

data class DistributionSummary(
    val period: String, // e.g. "2025-W37" or "2025-09"
    val totalTransactionCost: Double = 0.0,
    val incomeCategories: List<CategorySummary>,
    val expenseCategories: List<CategorySummary>,
    val othersInsightSummary: String? = null
)

data class CategorySummary(
    val category: String,
    val total: Double,
    val percentage: Double,
    val transactionCount: Int = 0,
    val averageTransactionCount: Double? = null,
    val momentumTrend: String? = null,
    val topDescriptionInsights: List<String>? = null
)
