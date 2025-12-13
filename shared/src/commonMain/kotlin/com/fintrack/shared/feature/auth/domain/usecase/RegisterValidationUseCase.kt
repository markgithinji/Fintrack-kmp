package com.fintrack.shared.feature.auth.domain.usecase

import com.fintrack.shared.feature.auth.domain.model.ValidationResult

class RegisterValidationUseCase {

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

    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Error("Password is required")
            password.length < 8 -> ValidationResult.Error("Password must be at least 8 characters")
            password.length > 50 -> ValidationResult.Error("Password must be less than 50 characters")
            !password.any { it.isDigit() } -> ValidationResult.Error("Password must contain at least one digit")
            !password.any { it.isLetter() } -> ValidationResult.Error("Password must contain at least one letter")
            else -> ValidationResult.Success
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): ValidationResult {
        return when {
            confirmPassword.isBlank() -> ValidationResult.Error("Please confirm your password")
            password != confirmPassword -> ValidationResult.Error("Passwords do not match")
            else -> ValidationResult.Success
        }
    }

    fun validateForm(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return validateName(name) is ValidationResult.Success &&
                validateEmail(email) is ValidationResult.Success &&
                validatePassword(password) is ValidationResult.Success &&
                validateConfirmPassword(password, confirmPassword) is ValidationResult.Success
    }

    fun calculatePasswordStrength(password: String): String {
        if (password.isEmpty()) return "None"

        val hasMinLength = password.length >= 8
        val hasDigit = password.any { it.isDigit() }
        val hasLetter = password.any { it.isLetter() }
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        return when {
            hasMinLength && hasDigit && hasLetter && hasUpperCase && hasLowerCase && hasSpecialChar && password.length >= 12 -> "Strong"
            hasMinLength && hasDigit && hasLetter && (hasUpperCase || hasLowerCase) -> "Medium"
            else -> "Weak"
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
        return email.matches(emailRegex.toRegex())
    }
}