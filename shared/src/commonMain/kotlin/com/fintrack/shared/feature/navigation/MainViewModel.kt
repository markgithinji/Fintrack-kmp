package com.fintrack.shared.feature.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.budget.domain.usecase.CheckBudgetThresholdsUseCase
import com.fintrack.shared.feature.settings.domain.datasource.SettingsDataSource
import com.fintrack.shared.feature.transaction.domain.usecase.SyncRecurringBillsUseCase
import com.fintrack.shared.feature.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class MainViewModel(
    private val settingsDataSource: SettingsDataSource,
    userRepository: UserRepository,
    private val checkBudgetThresholdsUseCase: CheckBudgetThresholdsUseCase,
    private val syncRecurringBillsUseCase: SyncRecurringBillsUseCase,
) : ViewModel() {

    private val _selectedAccountId = MutableStateFlow<String?>(null)
    val selectedAccountId: StateFlow<String?> = _selectedAccountId.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0)
    val refreshTrigger: StateFlow<Int> = _refreshTrigger.asStateFlow()

    // Global states that multiple screens care about
    val isBalanceHidden = settingsDataSource.isBalanceHidden
    val currency = settingsDataSource.currency
    val theme = settingsDataSource.theme
    val showDecimals = settingsDataSource.showDecimals
    val timeFormat = settingsDataSource.timeFormat
    val userProfile = userRepository.getUserProfile()

    init {
        // Initialize selected account from default settings if not already set
        viewModelScope.launch {
            settingsDataSource.defaultAccountId.collect { id ->
                if ((_selectedAccountId.value == null) && (id != null)) {
                    _selectedAccountId.value = id
                }
            }
        }

        // Maintenance tasks
        viewModelScope.launch {
            // Initial checks
            checkBudgets()
            syncBills()

            // React to settings changes for bills
            combine(
                settingsDataSource.isBillReminderEnabled,
                settingsDataSource.billReminderDaysBefore,
            ) { enabled, days -> enabled to days }
                .distinctUntilChanged()
                .collectLatest { (enabled, _) ->
                    if (enabled) {
                        syncBills()
                    }
                }
        }
    }

    fun onAccountSelected(id: String) {
        _selectedAccountId.value = id
    }

    fun triggerGlobalRefresh() {
        viewModelScope.launch {
            _refreshTrigger.value++
            // Maintenance tasks that react to data changes
            checkBudgets()
            syncBills()
        }
    }

    private suspend fun checkBudgets() {
        try {
            checkBudgetThresholdsUseCase()
        } catch (_: Exception) {
            // Log error
        }
    }

    private suspend fun syncBills() {
        try {
            syncRecurringBillsUseCase()
        } catch (_: Exception) {
            // Log error
        }
    }
}
