package com.fintrack.shared.feature.user.domain.usecase

import com.fintrack.shared.feature.core.domain.ValidationResult

class ProfileValidationUseCase {

    fun validateName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error("Name is required")
            name.length < 2 -> ValidationResult.Error("Name must be at least 2 characters")
            name.length > 50 -> ValidationResult.Error("Name must be less than 50 characters")
            else -> ValidationResult.Success
        }
    }

    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Error("Email is required")
            !isValidEmail(email) -> ValidationResult.Error("Invalid email format")
            email.length > 100 -> ValidationResult.Error("Email must be less than 100 characters")
            else -> ValidationResult.Success
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
        return email.matches(emailRegex.toRegex())
    }
}
