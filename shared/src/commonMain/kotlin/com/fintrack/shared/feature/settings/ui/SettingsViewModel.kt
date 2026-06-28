package com.fintrack.shared.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.settings.domain.datasource.SettingsDataSource
import com.fintrack.shared.feature.settings.domain.model.Currency
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
) : ViewModel() {

    val currency: StateFlow<Currency> = settingsDataSource.currency
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Currency.KES
        )

    private val _exportResult = MutableStateFlow<String?>(null)
    val exportResult: StateFlow<String?> = _exportResult.asStateFlow()

    private val _isLoading = MutableStateFlow(value = false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun setCurrency(currency: Currency) {
        viewModelScope.launch {
            settingsDataSource.setCurrency(currency)
        }
    }

    fun exportToCsv() {
        viewModelScope.launch {
            _exportResult.value = null
            _isLoading.value = true
            when (val result = exportTransactionsUseCase()) {
                is Result.Success -> {
                    _exportResult.value = result.data
                }
                is Result.Error -> {
                    // We could add an error state flow here if needed
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun clearAllTransactions() {
        viewModelScope.launch {
            _isLoading.value = true
            clearAllTransactionsUseCase()
            _isLoading.value = false
        }
    }

    fun clearExportResult() {
        _exportResult.value = null
    }
}
