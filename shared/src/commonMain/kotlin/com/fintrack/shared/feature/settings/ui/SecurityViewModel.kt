package com.fintrack.shared.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.auth.domain.usecase.ChangePasswordUseCase
import com.fintrack.shared.feature.auth.domain.usecase.ChangePasswordValidationUseCase
import com.fintrack.shared.feature.core.domain.SaveState
import com.fintrack.shared.feature.core.domain.ValidationResult
import com.fintrack.shared.feature.core.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SecurityViewModel(
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val validationUseCase: ChangePasswordValidationUseCase,
) : ViewModel() {

    private val _changePasswordState = MutableStateFlow<SaveState<Unit>>(SaveState.Idle)
    val changePasswordState: StateFlow<SaveState<Unit>> = _changePasswordState.asStateFlow()

    private val _formState = MutableStateFlow(ChangePasswordFormState())
    val formState: StateFlow<ChangePasswordFormState> = _formState.asStateFlow()

    fun updateCurrentPassword(password: String) {
        _formState.update { it.copy(currentPassword = password, currentPasswordError = null) }
    }

    fun updateNewPassword(password: String) {
        _formState.update { it.copy(newPassword = password, newPasswordError = null) }
    }

    fun updateConfirmPassword(password: String) {
        _formState.update { it.copy(confirmPassword = password, confirmPasswordError = null) }
    }

    fun changePassword() {
        val currentPassword = _formState.value.currentPassword
        val newPassword = _formState.value.newPassword
        val confirmPassword = _formState.value.confirmPassword

        val currentPasswordResult = validationUseCase.validateCurrentPassword(currentPassword)
        val newPasswordResult = validationUseCase.validateNewPassword(newPassword)
        val confirmPasswordResult = validationUseCase.validateConfirmPassword(newPassword, confirmPassword)

        val hasError = listOf(currentPasswordResult, newPasswordResult, confirmPasswordResult)
            .any { it is ValidationResult.Error }

        if (hasError) {
            _formState.update {
                it.copy(
                    currentPasswordError = (currentPasswordResult as? ValidationResult.Error)?.message,
                    newPasswordError = (newPasswordResult as? ValidationResult.Error)?.message,
                    confirmPasswordError = (confirmPasswordResult as? ValidationResult.Error)?.message
                )
            }
            return
        }

        viewModelScope.launch {
            _changePasswordState.value = SaveState.Loading
            when (val result = changePasswordUseCase(currentPassword, newPassword)) {
                is Result.Success -> {
                    _changePasswordState.value = SaveState.Success(Unit)
                    resetForm()
                }
                is Result.Error -> {
                    _changePasswordState.value = SaveState.Error(result.exception)
                }
                else -> { /* No-op for loading state if already set */ }
            }
        }
    }

    fun resetState() {
        _changePasswordState.value = SaveState.Idle
    }

    private fun resetForm() {
        _formState.value = ChangePasswordFormState()
    }
}

data class ChangePasswordFormState(
    val currentPassword: String = "",
    val currentPasswordError: String? = null,
    val newPassword: String = "",
    val newPasswordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null
)
