package com.fintrack.shared.feature.settings.data.local

import android.content.Context
import com.fintrack.shared.feature.settings.domain.datasource.SettingsDataSource
import com.fintrack.shared.feature.settings.domain.model.AppTheme
import com.fintrack.shared.feature.settings.domain.model.Currency
import com.fintrack.shared.feature.settings.domain.model.ExportFormat
import com.fintrack.shared.feature.settings.domain.model.TimeFormat
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import androidx.core.content.edit
import kotlinx.datetime.LocalTime

class AndroidSettingsDataSource(
    private val context: Context
) : SettingsDataSource {

    private val prefs = context.getSharedPreferences("fintrack_settings", Context.MODE_PRIVATE)
    private val _themeFlow = MutableStateFlow(AppTheme.SYSTEM)
    private val _timeFormatFlow = MutableStateFlow(TimeFormat.TWENTY_FOUR_HOUR)
    private val _currencyFlow = MutableStateFlow(Currency.KES)
    private val _biometricFlow = MutableStateFlow(false)
    private val _balanceHiddenFlow = MutableStateFlow(false)
    private val _reminderFlow = MutableStateFlow(false)
    private val _reminderTimeFlow = MutableStateFlow(LocalTime(20, 0))
    private val _mpesaSimSlotFlow = MutableStateFlow<Int?>(null)
    private val _mpesaAccountIdFlow = MutableStateFlow<String?>(null)
    private val _mpesaListenerFlow = MutableStateFlow(true)
    private val _budgetAlertsEnabledFlow = MutableStateFlow(false)
    private val _budgetAlertThresholdsFlow = MutableStateFlow(setOf(50, 80, 100))
    private val _alertBudgetIdFlow = MutableStateFlow<String?>(null)
    private val _billReminderEnabledFlow = MutableStateFlow(false)
    private val _billReminderDaysBeforeFlow = MutableStateFlow(2)
    private val _dailySummaryEnabledFlow = MutableStateFlow(false)
    private val _weeklySummaryEnabledFlow = MutableStateFlow(false)
    private val _summaryNotificationTimeFlow = MutableStateFlow(LocalTime(8, 0))
    private val _showDecimalsFlow = MutableStateFlow(true)
    private val _defaultAccountIdFlow = MutableStateFlow<String?>(null)
    private val _exportFormatFlow = MutableStateFlow(ExportFormat.CSV)

    init {
        val themeName = prefs.getString("app_theme", AppTheme.SYSTEM.name)
        _themeFlow.update { AppTheme.fromName(themeName) }

        val timeFormatName = prefs.getString("time_format", TimeFormat.TWENTY_FOUR_HOUR.name)
        _timeFormatFlow.update { TimeFormat.fromName(timeFormatName) }

        val currencyCode = prefs.getString("currency_code", Currency.KES.code)
        _currencyFlow.update { Currency.fromCode(currencyCode) }
        
        val biometricEnabled = prefs.getBoolean("biometric_enabled", false)
        _biometricFlow.update { biometricEnabled }

        val balanceHidden = prefs.getBoolean("balance_hidden", false)
        _balanceHiddenFlow.update { balanceHidden }

        val reminderEnabled = prefs.getBoolean("reminder_enabled", false)
        _reminderFlow.update { reminderEnabled }

        val reminderTimeStr = prefs.getString("reminder_time", "20:00") ?: "20:00"
        _reminderTimeFlow.update { LocalTime.parse(reminderTimeStr) }

        val mpesaSimSlot = if (prefs.contains("mpesa_sim_slot")) prefs.getInt("mpesa_sim_slot", -1) else -1
        _mpesaSimSlotFlow.update { if (mpesaSimSlot == -1) null else mpesaSimSlot }

        val mpesaAccountId = prefs.getString("mpesa_account_id", null)
        _mpesaAccountIdFlow.update { mpesaAccountId }

        val mpesaListenerEnabled = prefs.getBoolean("mpesa_listener_enabled", true)
        _mpesaListenerFlow.update { mpesaListenerEnabled }

        val budgetAlertsEnabled = prefs.getBoolean("budget_alerts_enabled", false)
        _budgetAlertsEnabledFlow.update { budgetAlertsEnabled }

        val thresholds = prefs.getStringSet("budget_alert_thresholds", setOf("50", "80", "100"))
        _budgetAlertThresholdsFlow.update { thresholds?.mapNotNull { it.toIntOrNull() }?.toSet() ?: setOf(50, 80, 100) }

        val alertBudgetId = prefs.getString("alert_budget_id", null)
        _alertBudgetIdFlow.update { alertBudgetId }

        val billReminderEnabled = prefs.getBoolean("bill_reminder_enabled", false)
        _billReminderEnabledFlow.update { billReminderEnabled }

        val billReminderDaysBefore = prefs.getInt("bill_reminder_days_before", 2)
        _billReminderDaysBeforeFlow.update { billReminderDaysBefore }

        val dailySummaryEnabled = prefs.getBoolean("daily_summary_enabled", false)
        _dailySummaryEnabledFlow.update { dailySummaryEnabled }

        val weeklySummaryEnabled = prefs.getBoolean("weekly_summary_enabled", false)
        _weeklySummaryEnabledFlow.update { weeklySummaryEnabled }

        val summaryTimeStr = prefs.getString("summary_notification_time", "08:00") ?: "08:00"
        _summaryNotificationTimeFlow.update { LocalTime.parse(summaryTimeStr) }

        val showDecimals = prefs.getBoolean("show_decimals", true)
        _showDecimalsFlow.update { showDecimals }

        val defaultAccountId = prefs.getString("default_account_id", null)
        _defaultAccountIdFlow.update { defaultAccountId }

        val exportFormatName = prefs.getString("export_format", ExportFormat.CSV.name) ?: ExportFormat.CSV.name
        _exportFormatFlow.update { try { ExportFormat.valueOf(exportFormatName) } catch (e: Exception) { ExportFormat.CSV } }
    }

    override val theme: StateFlow<AppTheme> = _themeFlow.asStateFlow()

    override suspend fun setTheme(theme: AppTheme) {
        prefs.edit(commit = true) {
            putString("app_theme", theme.name)
        }
        _themeFlow.value = theme
    }

    override val timeFormat: StateFlow<TimeFormat> = _timeFormatFlow.asStateFlow()

    override suspend fun setTimeFormat(format: TimeFormat) {
        prefs.edit(commit = true) {
            putString("time_format", format.name)
        }
        _timeFormatFlow.value = format
    }

    override val currency: StateFlow<Currency> = _currencyFlow.asStateFlow()

    override suspend fun setCurrency(currency: Currency) {
        prefs.edit(commit = true) {
            putString("currency_code", currency.code)
        }
        _currencyFlow.value = currency
    }

    override val isBiometricEnabled: StateFlow<Boolean> = _biometricFlow.asStateFlow()

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit(commit = true) {
            putBoolean("biometric_enabled", enabled)
        }
        _biometricFlow.value = enabled
    }

    override val isBalanceHidden: StateFlow<Boolean> = _balanceHiddenFlow.asStateFlow()

    override suspend fun setBalanceHidden(hidden: Boolean) {
        prefs.edit(commit = true) {
            putBoolean("balance_hidden", hidden)
        }
        _balanceHiddenFlow.value = hidden
    }

    override val isReminderEnabled: StateFlow<Boolean> = _reminderFlow.asStateFlow()

    override suspend fun setReminderEnabled(enabled: Boolean) {
        prefs.edit(commit = true) {
            putBoolean("reminder_enabled", enabled)
        }
        _reminderFlow.value = enabled
    }

    override val reminderTime: StateFlow<LocalTime> = _reminderTimeFlow.asStateFlow()

    override suspend fun setReminderTime(time: LocalTime) {
        prefs.edit(commit = true) {
            putString("reminder_time", time.toString())
        }
        _reminderTimeFlow.value = time
    }

    override val mpesaSimSlot: StateFlow<Int?> = _mpesaSimSlotFlow.asStateFlow()

    override suspend fun setMpesaSimSlot(slot: Int?) {
        prefs.edit(commit = true) {
            if (slot == null) {
                remove("mpesa_sim_slot")
            } else {
                putInt("mpesa_sim_slot", slot)
            }
        }
        _mpesaSimSlotFlow.value = slot
    }

    override val mpesaAccountId: StateFlow<String?> = _mpesaAccountIdFlow.asStateFlow()

    override suspend fun setMpesaAccountId(accountId: String?) {
        prefs.edit(commit = true) {
            if (accountId == null) {
                remove("mpesa_account_id")
            } else {
                putString("mpesa_account_id", accountId)
            }
        }
        _mpesaAccountIdFlow.value = accountId
    }

    override val isMpesaListenerEnabled: StateFlow<Boolean> = _mpesaListenerFlow.asStateFlow()

    override suspend fun setMpesaListenerEnabled(enabled: Boolean) {
        prefs.edit(commit = true) {
            putBoolean("mpesa_listener_enabled", enabled)
        }
        _mpesaListenerFlow.value = enabled
    }

    override val budgetAlertsEnabled: StateFlow<Boolean> = _budgetAlertsEnabledFlow.asStateFlow()

    override suspend fun setBudgetAlertsEnabled(enabled: Boolean) {
        prefs.edit(commit = true) {
            putBoolean("budget_alerts_enabled", enabled)
        }
        _budgetAlertsEnabledFlow.value = enabled
    }

    override val budgetAlertThresholds: StateFlow<Set<Int>> = _budgetAlertThresholdsFlow.asStateFlow()

    override suspend fun setBudgetAlertThresholds(thresholds: Set<Int>) {
        prefs.edit(commit = true) {
            putStringSet("budget_alert_thresholds", thresholds.map { it.toString() }.toSet())
        }
        _budgetAlertThresholdsFlow.value = thresholds
    }

    override val alertBudgetId: StateFlow<String?> = _alertBudgetIdFlow.asStateFlow()

    override suspend fun setAlertBudgetId(budgetId: String?) {
        prefs.edit(commit = true) {
            if (budgetId == null) {
                remove("alert_budget_id")
            } else {
                putString("alert_budget_id", budgetId)
            }
        }
        _alertBudgetIdFlow.value = budgetId
    }

    override val isBillReminderEnabled: StateFlow<Boolean> = _billReminderEnabledFlow.asStateFlow()

    override suspend fun setBillReminderEnabled(enabled: Boolean) {
        prefs.edit(commit = true) {
            putBoolean("bill_reminder_enabled", enabled)
        }
        _billReminderEnabledFlow.value = enabled
    }

    override val billReminderDaysBefore: StateFlow<Int> = _billReminderDaysBeforeFlow.asStateFlow()

    override suspend fun setBillReminderDaysBefore(days: Int) {
        prefs.edit(commit = true) {
            putInt("bill_reminder_days_before", days)
        }
        _billReminderDaysBeforeFlow.value = days
    }

    override val isDailySummaryEnabled: StateFlow<Boolean> = _dailySummaryEnabledFlow.asStateFlow()

    override suspend fun setDailySummaryEnabled(enabled: Boolean) {
        prefs.edit(commit = true) {
            putBoolean("daily_summary_enabled", enabled)
        }
        _dailySummaryEnabledFlow.value = enabled
    }

    override val isWeeklySummaryEnabled: StateFlow<Boolean> = _weeklySummaryEnabledFlow.asStateFlow()

    override suspend fun setWeeklySummaryEnabled(enabled: Boolean) {
        prefs.edit(commit = true) {
            putBoolean("weekly_summary_enabled", enabled)
        }
        _weeklySummaryEnabledFlow.value = enabled
    }

    override val summaryNotificationTime: StateFlow<LocalTime> = _summaryNotificationTimeFlow.asStateFlow()

    override suspend fun setSummaryNotificationTime(time: LocalTime) {
        prefs.edit(commit = true) {
            putString("summary_notification_time", time.toString())
        }
        _summaryNotificationTimeFlow.value = time
    }

    override val showDecimals: StateFlow<Boolean> = _showDecimalsFlow.asStateFlow()

    override suspend fun setShowDecimals(show: Boolean) {
        prefs.edit(commit = true) {
            putBoolean("show_decimals", show)
        }
        _showDecimalsFlow.value = show
    }

    override val defaultAccountId: StateFlow<String?> = _defaultAccountIdFlow.asStateFlow()

    override suspend fun setDefaultAccountId(id: String?) {
        prefs.edit(commit = true) {
            if (id == null) {
                remove("default_account_id")
            } else {
                putString("default_account_id", id)
            }
        }
        _defaultAccountIdFlow.value = id
    }

    override val exportFormat: StateFlow<ExportFormat> = _exportFormatFlow.asStateFlow()

    override suspend fun setExportFormat(format: ExportFormat) {
        prefs.edit(commit = true) {
            putString("export_format", format.name)
        }
        _exportFormatFlow.value = format
    }
}
