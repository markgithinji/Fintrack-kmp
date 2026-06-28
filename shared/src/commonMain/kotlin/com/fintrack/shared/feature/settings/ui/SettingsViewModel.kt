package com.fintrack.shared.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.settings.domain.datasource.SettingsDataSource
import com.fintrack.shared.feature.settings.domain.model.AppTheme
import com.fintrack.shared.feature.settings.domain.model.Currency
import com.fintrack.shared.feature.settings.domain.model.TimeFormat
import com.fintrack.shared.feature.settings.domain.util.BiometricAuthenticator
import com.fintrack.shared.feature.settings.domain.util.BiometricResult
import com.fintrack.shared.feature.settings.domain.util.NotificationService
import com.fintrack.shared.feature.transaction.domain.usecase.ClearAllTransactionsUseCase
import com.fintrack.shared.feature.transaction.domain.usecase.ExportTransactionsUseCase
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.auth.domain.usecase.ChangePasswordUseCase
import com.fintrack.shared.feature.auth.domain.usecase.ChangePasswordValidationUseCase
import com.fintrack.shared.feature.core.domain.SaveState
import com.fintrack.shared.feature.core.domain.ValidationResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime

class SettingsViewModel(
    private val settingsDataSource: SettingsDataSource,
    private val clearAllTransactionsUseCase: ClearAllTransactionsUseCase,
    private val exportTransactionsUseCase: ExportTransactionsUseCase,
    private val biometricAuthenticator: BiometricAuthenticator,
    private val notificationService: NotificationService,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val validationUseCase: ChangePasswordValidationUseCase,
) : ViewModel() {

    val theme: StateFlow<AppTheme> = settingsDataSource.theme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.SYSTEM
        )

    val timeFormat: StateFlow<TimeFormat> = settingsDataSource.timeFormat
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TimeFormat.TWENTY_FOUR_HOUR
        )

    val currency: StateFlow<Currency> = settingsDataSource.currency
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Currency.KES
        )

    val isBalanceHidden: StateFlow<Boolean> = settingsDataSource.isBalanceHidden
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isReminderEnabled: StateFlow<Boolean> = settingsDataSource.isReminderEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isBiometricEnabled: StateFlow<Boolean> = settingsDataSource.isBiometricEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val reminderTime: StateFlow<LocalTime> = settingsDataSource.reminderTime
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LocalTime(20, 0)
        )

    private val _exportResult = MutableStateFlow<String?>(null)
    val exportResult: StateFlow<String?> = _exportResult.asStateFlow()

    private val _isLoading = MutableStateFlow(value = false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _showPermissionRequest = MutableStateFlow(false)
    val showPermissionRequest: StateFlow<Boolean> = _showPermissionRequest.asStateFlow()

    private val _changePasswordState = MutableStateFlow<SaveState<Unit>>(SaveState.Idle)
    val changePasswordState: StateFlow<SaveState<Unit>> = _changePasswordState.asStateFlow()

    private val _changePasswordFormState = MutableStateFlow(ChangePasswordFormState())
    val changePasswordFormState: StateFlow<ChangePasswordFormState> = _changePasswordFormState.asStateFlow()

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsDataSource.setTheme(theme)
        }
    }

    fun setTimeFormat(format: TimeFormat) {
        viewModelScope.launch {
            settingsDataSource.setTimeFormat(format)
        }
    }

    fun setCurrency(currency: Currency) {
        viewModelScope.launch {
            settingsDataSource.setCurrency(currency)
        }
    }

    fun setBalanceHidden(hidden: Boolean) {
        viewModelScope.launch {
            settingsDataSource.setBalanceHidden(hidden)
        }
    }

    fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) {
                val result = biometricAuthenticator.authenticate(
                    title = "Enable Biometric",
                    subtitle = "Confirm your identity to enable biometric lock"
                )
                if (result is BiometricResult.Success) {
                    settingsDataSource.setBiometricEnabled(true)
                } else if (result is BiometricResult.Error) {
                    _error.value = result.message
                }
            } else {
                settingsDataSource.setBiometricEnabled(false)
            }
        }
    }

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) {
                _showPermissionRequest.value = true
            } else {
                settingsDataSource.setReminderEnabled(false)
                notificationService.cancelDailyReminder()
            }
        }
    }

    fun setReminderTime(time: LocalTime) {
        viewModelScope.launch {
            settingsDataSource.setReminderTime(time)
            if (isReminderEnabled.value) {
                notificationService.scheduleDailyReminder()
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _showPermissionRequest.value = false
        if (granted) {
            viewModelScope.launch {
                settingsDataSource.setReminderEnabled(true)
                notificationService.scheduleDailyReminder()
            }
        } else {
            _error.value = "Notification permission denied"
        }
    }

    fun dismissPermissionRequest() {
        _showPermissionRequest.value = false
    }

    fun exportToCsv() {
        viewModelScope.launch {
            val authResult = biometricAuthenticator.authenticate(
                title = "Export Data",
                subtitle = "Confirm your identity to export your transactions"
            )

            if (authResult is BiometricResult.Success || authResult is BiometricResult.NotAvailable) {
                _exportResult.value = null
                _isLoading.value = true
                when (val result = exportTransactionsUseCase()) {
                    is Result.Success -> {
                        _exportResult.value = result.data
                    }
                    is Result.Error -> {
                        _error.value = "Failed to export transactions"
                    }
                    else -> {}
                }
                _isLoading.value = false
            } else if (authResult is BiometricResult.Error) {
                _error.value = authResult.message
            }
        }
    }

    fun clearAllTransactions() {
        viewModelScope.launch {
            val authResult = biometricAuthenticator.authenticate(
                title = "Clear Data",
                subtitle = "Confirm your identity to delete all transactions"
            )
            
            when (authResult) {
                is BiometricResult.Success -> {
                    _isLoading.value = true
                    val result = clearAllTransactionsUseCase()
                    if (result is Result.Error) {
                        _error.value = "Failed to clear transactions"
                    }
                    _isLoading.value = false
                }
                is BiometricResult.Error -> {
                    _error.value = authResult.message
                }
                BiometricResult.NotAvailable -> {
                    // If biometric is not available, we could fallback to PIN/Password
                    // For now, we'll allow it since they already confirmed in the dialog
                    _isLoading.value = true
                    clearAllTransactionsUseCase()
                    _isLoading.value = false
                }
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearExportResult() {
        _exportResult.value = null
    }

    // Password change methods
    fun updateCurrentPassword(password: String) {
        _changePasswordFormState.update { it.copy(currentPassword = password, currentPasswordError = null) }
    }

    fun updateNewPassword(password: String) {
        _changePasswordFormState.update { it.copy(newPassword = password, newPasswordError = null) }
    }

    fun updateConfirmPassword(password: String) {
        _changePasswordFormState.update { it.copy(confirmPassword = password, confirmPasswordError = null) }
    }

    fun changePassword() {
        val form = _changePasswordFormState.value
        val currentPasswordResult = validationUseCase.validateCurrentPassword(form.currentPassword)
        val newPasswordResult = validationUseCase.validateNewPassword(form.newPassword)
        val confirmPasswordResult = validationUseCase.validateConfirmPassword(form.newPassword, form.confirmPassword)

        val hasError = listOf(currentPasswordResult, newPasswordResult, confirmPasswordResult)
            .any { it is ValidationResult.Error }

        if (hasError) {
            _changePasswordFormState.update {
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
            when (val result = changePasswordUseCase(form.currentPassword, form.newPassword)) {
                is Result.Success -> {
                    _changePasswordState.value = SaveState.Success(Unit)
                    _changePasswordFormState.value = ChangePasswordFormState()
                }
                is Result.Error -> {
                    _changePasswordState.value = SaveState.Error(result.exception)
                }
                else -> {}
            }
        }
    }

    fun resetChangePasswordState() {
        _changePasswordState.value = SaveState.Idle
        _changePasswordFormState.value = ChangePasswordFormState()
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
