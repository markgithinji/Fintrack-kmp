package com.fintrack.shared.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.settings.domain.datasource.SettingsDataSource
import com.fintrack.shared.feature.settings.domain.model.AppTheme
import com.fintrack.shared.feature.settings.domain.model.Currency
import com.fintrack.shared.feature.settings.domain.util.BiometricAuthenticator
import com.fintrack.shared.feature.settings.domain.util.BiometricResult
import com.fintrack.shared.feature.transaction.domain.usecase.ClearAllTransactionsUseCase
import com.fintrack.shared.feature.transaction.domain.usecase.ExportTransactionsUseCase
import com.fintrack.shared.feature.core.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsDataSource: SettingsDataSource,
    private val clearAllTransactionsUseCase: ClearAllTransactionsUseCase,
    private val exportTransactionsUseCase: ExportTransactionsUseCase,
    private val biometricAuthenticator: BiometricAuthenticator,
) : ViewModel() {

    val theme: StateFlow<AppTheme> = settingsDataSource.theme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.SYSTEM
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

    private val _exportResult = MutableStateFlow<String?>(null)
    val exportResult: StateFlow<String?> = _exportResult.asStateFlow()

    private val _isLoading = MutableStateFlow(value = false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsDataSource.setTheme(theme)
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
}
