package com.fintrack.shared.feature.user.domain.usecase

import com.fintrack.shared.feature.core.domain.ValidationResult

class ProfileValidationUseCase {

    fun validateName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error("Name cannot be empty.")
            name.length < 2 -> ValidationResult.Error("Name is too short (minimum 2 characters).")
            name.length > 50 -> ValidationResult.Error("Name is too long (maximum 50 characters).")
            else -> ValidationResult.Success
        }
    }

    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Error("Email address is required.")
            !isValidEmail(email) -> ValidationResult.Error("Please enter a valid email address.")
            email.length > 100 -> ValidationResult.Error("Email is too long.")
            else -> ValidationResult.Success
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
        return email.matches(emailRegex.toRegex())
    }
}
