package com.fintrack.shared.feature.auth.domain.model

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}