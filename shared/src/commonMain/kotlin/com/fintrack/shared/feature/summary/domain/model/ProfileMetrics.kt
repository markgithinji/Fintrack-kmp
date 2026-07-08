package com.fintrack.shared.feature.summary.domain.model

data class ProfileMetrics(
    val name: String,
    val email: String,
    val netWorth: Double,
    val savingsRate: Double?,
    val essentialSpendRatio: Double?
)
