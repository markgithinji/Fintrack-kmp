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
import com.fintrack.shared.feature.core.domain.usecase.ClearAllUserDataUseCase
import com.fintrack.shared.feature.transaction.domain.usecase.ExportTransactionsUseCase
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.auth.domain.usecase.ChangePasswordUseCase
import com.fintrack.shared.feature.auth.domain.usecase.ChangePasswordValidationUseCase
import com.fintrack.shared.feature.core.domain.SaveState
import com.fintrack.shared.feature.core.domain.ValidationResult
import com.fintrack.shared.feature.user.domain.usecase.DeleteAccountUseCase
import com.fintrack.shared.feature.user.domain.repository.UserRepository
import com.fintrack.shared.feature.transaction.domain.repository.CategoryRepository
import com.fintrack.shared.feature.budget.domain.repository.BudgetRepository
import com.fintrack.shared.feature.budget.domain.model.BudgetWithStatus
import com.fintrack.shared.feature.transaction.domain.model.Category
import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.account.domain.repository.AccountRepository
import com.fintrack.shared.feature.core.util.GlobalRefreshManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime

class SettingsViewModel(
    private val settingsDataSource: SettingsDataSource,
    private val clearAllUserDataUseCase: ClearAllUserDataUseCase,
    private val exportTransactionsUseCase: ExportTransactionsUseCase,
    private val biometricAuthenticator: BiometricAuthenticator,
    private val notificationService: NotificationService,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val validationUseCase: ChangePasswordValidationUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val budgetRepository: BudgetRepository,
    private val globalRefreshManager: GlobalRefreshManager,
) : ViewModel() {

    init {
        viewModelScope.launch {
            try {
                categoryRepository.refreshCategories()
            } catch (e: Exception) {
                // Silently fail or log, handled by UI flow
            }
        }
        viewModelScope.launch {
            val result = accountRepository.getAccounts()
            if (result is Result.Success) {
                _accounts.value = result.data
            }
        }
        viewModelScope.launch {
            budgetRepository.getBudgets(forceRefresh = false)
        }
    }

    val budgets: StateFlow<Result<List<BudgetWithStatus>>> = budgetRepository.budgets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = budgetRepository.budgets.value
        )

    val theme: StateFlow<AppTheme> = settingsDataSource.theme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = settingsDataSource.theme.value
        )

    val timeFormat: StateFlow<TimeFormat> = settingsDataSource.timeFormat
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = settingsDataSource.timeFormat.value
        )

    val currency: StateFlow<Currency> = settingsDataSource.currency
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = settingsDataSource.currency.value
        )

    val isBalanceHidden: StateFlow<Boolean> = settingsDataSource.isBalanceHidden
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = settingsDataSource.isBalanceHidden.value
        )

    val isReminderEnabled: StateFlow<Boolean> = settingsDataSource.isReminderEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = settingsDataSource.isReminderEnabled.value
        )

    val isBiometricEnabled: StateFlow<Boolean> = settingsDataSource.isBiometricEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = settingsDataSource.isBiometricEnabled.value
        )

    val reminderTime: StateFlow<LocalTime> = settingsDataSource.reminderTime
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = settingsDataSource.reminderTime.value
        )

    val mpesaSimSlot: StateFlow<Int?> = settingsDataSource.mpesaSimSlot
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = settingsDataSource.mpesaSimSlot.value
        )

    val mpesaAccountId: StateFlow<String?> = settingsDataSource.mpesaAccountId
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = settingsDataSource.mpesaAccountId.value
        )

    val isMpesaListenerEnabled: StateFlow<Boolean> = settingsDataSource.isMpesaListenerEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = settingsDataSource.isMpesaListenerEnabled.value
        )

    val budgetAlertsEnabled: StateFlow<Boolean> = settingsDataSource.budgetAlertsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = settingsDataSource.budgetAlertsEnabled.value
        )

    val budgetAlertThresholds: StateFlow<Set<Int>> = settingsDataSource.budgetAlertThresholds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = settingsDataSource.budgetAlertThresholds.value
        )

    val alertBudgetId: StateFlow<String?> = settingsDataSource.alertBudgetId
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = settingsDataSource.alertBudgetId.value
        )

    val isBillReminderEnabled: StateFlow<Boolean> = settingsDataSource.isBillReminderEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = settingsDataSource.isBillReminderEnabled.value
        )

    val billReminderDaysBefore: StateFlow<Int> = settingsDataSource.billReminderDaysBefore
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = settingsDataSource.billReminderDaysBefore.value
        )

    val isDailySummaryEnabled: StateFlow<Boolean> = settingsDataSource.isDailySummaryEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = settingsDataSource.isDailySummaryEnabled.value
        )

    val isWeeklySummaryEnabled: StateFlow<Boolean> = settingsDataSource.isWeeklySummaryEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = settingsDataSource.isWeeklySummaryEnabled.value
        )

    val summaryNotificationTime: StateFlow<LocalTime> = settingsDataSource.summaryNotificationTime
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = settingsDataSource.summaryNotificationTime.value
        )

    val showDecimals: StateFlow<Boolean> = settingsDataSource.showDecimals
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = settingsDataSource.showDecimals.value
        )

    val trackedCategories: StateFlow<List<String>> = userRepository.getUserProfile()
        .map { it?.trackedCategories ?: emptyList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = userRepository.getUserProfile().value?.trackedCategories ?: emptyList()
        )

    val allCategories: StateFlow<List<Category>> = categoryRepository.getCategories()
        .map { categories ->
            val expenseCategories = categories.filter { it.isExpense && it.id != "transaction_cost" }
                .distinctBy { it.name }
                .sortedBy { it.name }
            
            // Add Transaction Cost as a selectable metric at the TOP
            val txCostVirtual = Category.TransactionCost
            
            listOf(txCostVirtual) + expenseCategories
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = categoryRepository.getCategories().value.let { categories ->
                val expenseCategories = categories.filter { it.isExpense && it.id != "transaction_cost" }
                    .distinctBy { it.name }
                    .sortedBy { it.name }
                listOf(Category.TransactionCost) + expenseCategories
            }
        )

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

    private val _selectedAccountIdsForReset = MutableStateFlow<Set<String>>(emptySet())
    val selectedAccountIdsForReset: StateFlow<Set<String>> = _selectedAccountIdsForReset.asStateFlow()

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
                notificationService.scheduleDailyReminder(time)
            }
        }
    }

    fun setMpesaSimSlot(slot: Int?) {
        viewModelScope.launch {
            settingsDataSource.setMpesaSimSlot(slot)
        }
    }

    fun setMpesaAccountId(accountId: String?) {
        viewModelScope.launch {
            settingsDataSource.setMpesaAccountId(accountId)
        }
    }

    fun setMpesaListenerEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataSource.setMpesaListenerEnabled(enabled)
        }
    }

    fun setBudgetAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataSource.setBudgetAlertsEnabled(enabled)
        }
    }

    fun setBudgetAlertThresholds(thresholds: Set<Int>) {
        viewModelScope.launch {
            settingsDataSource.setBudgetAlertThresholds(thresholds)
        }
    }

    fun setAlertBudgetId(budgetId: String?) {
        viewModelScope.launch {
            settingsDataSource.setAlertBudgetId(budgetId)
        }
    }

    fun setBillReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataSource.setBillReminderEnabled(enabled)
        }
    }

    fun setBillReminderDaysBefore(days: Int) {
        viewModelScope.launch {
            settingsDataSource.setBillReminderDaysBefore(days)
        }
    }

    fun setDailySummaryEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataSource.setDailySummaryEnabled(enabled)
            updateSummaryScheduling()
        }
    }

    fun setWeeklySummaryEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataSource.setWeeklySummaryEnabled(enabled)
            updateSummaryScheduling()
        }
    }

    fun setSummaryNotificationTime(time: LocalTime) {
        viewModelScope.launch {
            settingsDataSource.setSummaryNotificationTime(time)
            updateSummaryScheduling()
        }
    }

    fun setShowDecimals(show: Boolean) {
        viewModelScope.launch {
            settingsDataSource.setShowDecimals(show)
        }
    }

    private suspend fun updateSummaryScheduling() {
        val daily = settingsDataSource.isDailySummaryEnabled.first()
        val weekly = settingsDataSource.isWeeklySummaryEnabled.first()
        val time = settingsDataSource.summaryNotificationTime.first()

        if (daily || weekly) {
            notificationService.scheduleSummaryNotification(time)
        } else {
            notificationService.cancelSummaryNotification()
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _showPermissionRequest.value = false
        if (granted) {
            viewModelScope.launch {
                settingsDataSource.setReminderEnabled(true)
                notificationService.scheduleDailyReminder(reminderTime.value)
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

    private val _clearDataState = MutableStateFlow<SaveState<Unit>>(SaveState.Idle)
    val clearDataState: StateFlow<SaveState<Unit>> = _clearDataState.asStateFlow()

    fun toggleAccountSelectionForReset(accountId: String) {
        _selectedAccountIdsForReset.value = if (_selectedAccountIdsForReset.value.contains(accountId)) {
            _selectedAccountIdsForReset.value - accountId
        } else {
            _selectedAccountIdsForReset.value + accountId
        }
    }

    fun selectAllAccountsForReset() {
        _selectedAccountIdsForReset.value = _accounts.value.map { it.id }.toSet()
    }

    fun clearAccountSelectionForReset() {
        _selectedAccountIdsForReset.value = emptySet()
    }

    fun clearTransactions() {
        viewModelScope.launch {
            val authResult = biometricAuthenticator.authenticate(
                title = "Clear Data",
                subtitle = "Confirm your identity to delete transactions for selected accounts"
            )
            
            when (authResult) {
                is BiometricResult.Success -> {
                    performClear()
                }
                is BiometricResult.Error -> {
                    _error.value = authResult.message
                }
                BiometricResult.NotAvailable -> {
                    performClear()
                }
            }
        }
    }

    private suspend fun performClear() {
        _clearDataState.value = SaveState.Loading
        val selectedIds = _selectedAccountIdsForReset.value
        val accountIds: List<String>? = if (selectedIds.isEmpty()) null else selectedIds.toList()
        val result = clearAllUserDataUseCase(accountIds)
        if (result is Result.Error) {
            _clearDataState.value = SaveState.Error(result.exception)
            _error.value = "Failed to clear data"
        } else {
            _clearDataState.value = SaveState.Success(Unit)
            _selectedAccountIdsForReset.value = emptySet()
        }
    }

    fun resetClearDataState() {
        _clearDataState.value = SaveState.Idle
    }

    private val _deleteAccountState = MutableStateFlow<SaveState<Unit>>(SaveState.Idle)
    val deleteAccountState: StateFlow<SaveState<Unit>> = _deleteAccountState.asStateFlow()

    fun deleteAccount() {
        viewModelScope.launch {
            val authResult = biometricAuthenticator.authenticate(
                title = "Delete Account",
                subtitle = "This will permanently delete your account and all data"
            )

            when (authResult) {
                is BiometricResult.Success -> {
                    _deleteAccountState.value = SaveState.Loading
                    val result = deleteAccountUseCase()
                    if (result is Result.Error) {
                        _deleteAccountState.value = SaveState.Error(result.exception)
                        _error.value = "Failed to delete account"
                    } else {
                        _deleteAccountState.value = SaveState.Success(Unit)
                    }
                }
                is BiometricResult.Error -> {
                    _error.value = authResult.message
                }
                BiometricResult.NotAvailable -> {
                    _deleteAccountState.value = SaveState.Loading
                    val result = deleteAccountUseCase()
                    if (result is Result.Error) {
                        _deleteAccountState.value = SaveState.Error(result.exception)
                    } else {
                        _deleteAccountState.value = SaveState.Success(Unit)
                    }
                }
            }
        }
    }

    fun resetDeleteAccountState() {
        _deleteAccountState.value = SaveState.Idle
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

    fun updateTrackedCategories(categories: List<String>) {
        viewModelScope.launch {
            try {
                userRepository.updateTrackedCategories(categories)
                globalRefreshManager.triggerRefresh()
            } catch (e: Exception) {
                _error.value = "Failed to update tracked categories: ${e.message}"
            }
        }
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
