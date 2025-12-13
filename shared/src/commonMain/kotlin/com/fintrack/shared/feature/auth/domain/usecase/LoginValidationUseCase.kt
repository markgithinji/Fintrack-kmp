package com.fintrack.shared.feature.auth.domain.usecase

import com.fintrack.shared.feature.core.domain.ValidationResult

class LoginValidationUseCase {

    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Error("Email is required")
            !isValidEmail(email) -> ValidationResult.Error("Invalid email format")
            else -> ValidationResult.Success
        }
    }

    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Error("Password is required")
            else -> ValidationResult.Success
        }
    }

    fun validateForm(email: String, password: String): Boolean {
        return validateEmail(email) is ValidationResult.Success &&
                validatePassword(password) is ValidationResult.Success
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
        return email.matches(emailRegex.toRegex())
    }
}