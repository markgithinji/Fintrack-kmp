package com.fintrack.shared.feature.budget.domain.model

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String = ""
)