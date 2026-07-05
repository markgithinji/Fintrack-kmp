package com.fintrack.shared.feature.summary.domain.model

data class CategoryComparisonSummary(
    val period: String,
    val isCurrent: Boolean,
    val data: List<CategoryComparison>
)
