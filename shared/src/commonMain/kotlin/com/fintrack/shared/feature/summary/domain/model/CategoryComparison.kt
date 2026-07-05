package com.fintrack.shared.feature.summary.domain.model

data class CategoryComparison(
    val category: String,
    val currentTotal: Double,
    val previousTotal: Double,
    val changePercentage: Double,
    val isIncome: Boolean,
    val period: String? = null,
    val weeklyChangePercentage: Double? = null,
    val weeklyCurrentTotal: Double? = null
)
