package com.fintrack.shared.feature.auth.domain.usecase

import com.fintrack.shared.feature.core.domain.ValidationResult

class ChangePasswordValidationUseCase {

    fun validateCurrentPassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Error("Current password is required")
            else -> ValidationResult.Success
        }
    }

    fun validateNewPassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Error("New password is required")
            password.length < 6 -> ValidationResult.Error("Password must be at least 6 characters")
            else -> ValidationResult.Success
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): ValidationResult {
        return when {
            confirmPassword.isBlank() -> ValidationResult.Error("Confirm password is required")
            password != confirmPassword -> ValidationResult.Error("Passwords do not match")
            else -> ValidationResult.Success
        }
    }

    fun validateForm(currentPassword: String, newPassword: String, confirmPassword: String): Boolean {
        return validateCurrentPassword(currentPassword) is ValidationResult.Success &&
                validateNewPassword(newPassword) is ValidationResult.Success &&
                validateConfirmPassword(newPassword, confirmPassword) is ValidationResult.Success
    }
}
