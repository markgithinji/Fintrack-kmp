package com.fintrack.shared.feature.settings.data.local

import com.fintrack.shared.feature.settings.domain.datasource.SettingsDataSource
import com.fintrack.shared.feature.settings.domain.model.AppTheme
import com.fintrack.shared.feature.settings.domain.model.Currency
import com.fintrack.shared.feature.settings.domain.model.ExportFormat
import com.fintrack.shared.feature.settings.domain.model.TimeFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalTime
import platform.Foundation.NSUserDefaults

class IOSSettingsDataSource : SettingsDataSource {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val _themeFlow = MutableStateFlow(AppTheme.SYSTEM)
    private val _timeFormatFlow = MutableStateFlow(TimeFormat.TWENTY_FOUR_HOUR)
    private val _currencyFlow = MutableStateFlow(Currency.KES)
    private val _biometricFlow = MutableStateFlow(false)
    private val _balanceHiddenFlow = MutableStateFlow(false)
    private val _reminderFlow = MutableStateFlow(false)
    private val _reminderTimeFlow = MutableStateFlow(LocalTime(20, 0))
    private val _mpesaSimSlotFlow = MutableStateFlow<Int?>(null)
    private val _mpesaLinkedAccountIdsFlow = MutableStateFlow<Set<String>>(emptySet())
    private val _equityLinkedAccountIdsFlow = MutableStateFlow<Set<String>>(emptySet())
    private val _mpesaListenerFlow = MutableStateFlow(false)
    private val _equityListenerFlow = MutableStateFlow(false)
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
    private val _smsRationaleHiddenFlow = MutableStateFlow(false)

    init {
        val themeName = userDefaults.stringForKey("app_theme") ?: AppTheme.SYSTEM.name
        _themeFlow.value = AppTheme.fromName(themeName)

        val timeFormatName = userDefaults.stringForKey("time_format") ?: TimeFormat.TWENTY_FOUR_HOUR.name
        _timeFormatFlow.value = TimeFormat.fromName(timeFormatName)

        val currencyCode = userDefaults.stringForKey("currency_code") ?: Currency.KES.code
        _currencyFlow.value = Currency.fromCode(currencyCode)
        
        val biometricEnabled = userDefaults.boolForKey("biometric_enabled")
        _biometricFlow.value = biometricEnabled

        val balanceHidden = userDefaults.boolForKey("balance_hidden")
        _balanceHiddenFlow.value = balanceHidden

        val reminderEnabled = userDefaults.boolForKey("reminder_enabled")
        _reminderFlow.value = reminderEnabled

        val reminderTimeStr = userDefaults.stringForKey("reminder_time") ?: "20:00"
        _reminderTimeFlow.value = LocalTime.parse(reminderTimeStr)

        val mpesaSimSlot = if (userDefaults.objectForKey("mpesa_sim_slot") != null) userDefaults.integerForKey("mpesa_sim_slot").toInt() else -1
        _mpesaSimSlotFlow.value = if (mpesaSimSlot == -1) null else mpesaSimSlot

        val mpesaLinkedAccountIds = userDefaults.stringArrayForKey("mpesa_linked_account_ids") as? List<String>
        _mpesaLinkedAccountIdsFlow.value = mpesaLinkedAccountIds?.toSet() ?: emptySet()

        val equityLinkedAccountIds = userDefaults.stringArrayForKey("equity_linked_account_ids") as? List<String>
        _equityLinkedAccountIdsFlow.value = equityLinkedAccountIds?.toSet() ?: emptySet()

        val mpesaListenerEnabled = if (userDefaults.objectForKey("mpesa_listener_enabled") != null) userDefaults.boolForKey("mpesa_listener_enabled") else false
        _mpesaListenerFlow.value = mpesaListenerEnabled

        val equityListenerEnabled = if (userDefaults.objectForKey("equity_listener_enabled") != null) userDefaults.boolForKey("equity_listener_enabled") else false
        _equityListenerFlow.value = equityListenerEnabled

        val budgetAlertsEnabled = userDefaults.boolForKey("budget_alerts_enabled")
        _budgetAlertsEnabledFlow.value = budgetAlertsEnabled

        val thresholds = userDefaults.stringArrayForKey("budget_alert_thresholds") as? List<String>
        _budgetAlertThresholdsFlow.value = thresholds?.mapNotNull { it.toIntOrNull() }?.toSet() ?: setOf(50, 80, 100)

        val alertBudgetId = userDefaults.stringForKey("alert_budget_id")
        _alertBudgetIdFlow.value = alertBudgetId

        val billReminderEnabled = userDefaults.boolForKey("bill_reminder_enabled")
        _billReminderEnabledFlow.value = billReminderEnabled

        val billReminderDaysBefore = if (userDefaults.objectForKey("bill_reminder_days_before") != null) userDefaults.integerForKey("bill_reminder_days_before").toInt() else 2
        _billReminderDaysBeforeFlow.value = billReminderDaysBefore

        val dailySummaryEnabled = userDefaults.boolForKey("daily_summary_enabled")
        _dailySummaryEnabledFlow.value = dailySummaryEnabled

        val weeklySummaryEnabled = userDefaults.boolForKey("weekly_summary_enabled")
        _weeklySummaryEnabledFlow.value = weeklySummaryEnabled

        val summaryTimeStr = userDefaults.stringForKey("summary_notification_time") ?: "08:00"
        _summaryNotificationTimeFlow.value = LocalTime.parse(summaryTimeStr)

        val showDecimals = if (userDefaults.objectForKey("show_decimals") != null) userDefaults.boolForKey("show_decimals") else true
        _showDecimalsFlow.value = showDecimals

        val defaultAccountId = userDefaults.stringForKey("default_account_id")
        _defaultAccountIdFlow.value = defaultAccountId

        val exportFormatName = userDefaults.stringForKey("export_format") ?: ExportFormat.CSV.name
        _exportFormatFlow.value = try { ExportFormat.valueOf(exportFormatName) } catch (e: Exception) { ExportFormat.CSV }

        _smsRationaleHiddenFlow.value = userDefaults.boolForKey("sms_rationale_hidden")
    }

    override val theme: StateFlow<AppTheme> = _themeFlow.asStateFlow()

    override suspend fun setTheme(theme: AppTheme) {
        userDefaults.setObject(theme.name, "app_theme")
        _themeFlow.value = theme
    }

    override val timeFormat: StateFlow<TimeFormat> = _timeFormatFlow.asStateFlow()

    override suspend fun setTimeFormat(format: TimeFormat) {
        userDefaults.setObject(format.name, "time_format")
        _timeFormatFlow.value = format
    }

    override val currency: StateFlow<Currency> = _currencyFlow.asStateFlow()

    override suspend fun setCurrency(currency: Currency) {
        userDefaults.setObject(currency.code, "currency_code")
        _currencyFlow.value = currency
    }

    override val isBiometricEnabled: StateFlow<Boolean> = _biometricFlow.asStateFlow()

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        userDefaults.setBool(enabled, "biometric_enabled")
        _biometricFlow.value = enabled
    }

    override val isBalanceHidden: StateFlow<Boolean> = _balanceHiddenFlow.asStateFlow()

    override suspend fun setBalanceHidden(hidden: Boolean) {
        userDefaults.setBool(hidden, "balance_hidden")
        _balanceHiddenFlow.value = hidden
    }

    override val isReminderEnabled: StateFlow<Boolean> = _reminderFlow.asStateFlow()

    override suspend fun setReminderEnabled(enabled: Boolean) {
        userDefaults.setBool(enabled, "reminder_enabled")
        _reminderFlow.value = enabled
    }

    override val reminderTime: StateFlow<LocalTime> = _reminderTimeFlow.asStateFlow()

    override suspend fun setReminderTime(time: LocalTime) {
        userDefaults.setObject(time.toString(), "reminder_time")
        _reminderTimeFlow.value = time
    }

    override val mpesaSimSlot: StateFlow<Int?> = _mpesaSimSlotFlow.asStateFlow()

    override suspend fun setMpesaSimSlot(slot: Int?) {
        if (slot == null) {
            userDefaults.removeObjectForKey("mpesa_sim_slot")
        } else {
            userDefaults.setInteger(slot.toLong(), "mpesa_sim_slot")
        }
        _mpesaSimSlotFlow.value = slot
    }

    override val mpesaLinkedAccountIds: StateFlow<Set<String>> = _mpesaLinkedAccountIdsFlow.asStateFlow()

    override suspend fun setMpesaLinkedAccountIds(ids: Set<String>) {
        userDefaults.setObject(ids.toList(), "mpesa_linked_account_ids")
        _mpesaLinkedAccountIdsFlow.value = ids
    }

    override val equityLinkedAccountIds: StateFlow<Set<String>> = _equityLinkedAccountIdsFlow.asStateFlow()

    override suspend fun setEquityLinkedAccountIds(ids: Set<String>) {
        userDefaults.setObject(ids.toList(), "equity_linked_account_ids")
        _equityLinkedAccountIdsFlow.value = ids
    }

    override val isMpesaListenerEnabled: StateFlow<Boolean> = _mpesaListenerFlow.asStateFlow()

    override suspend fun setMpesaListenerEnabled(enabled: Boolean) {
        userDefaults.setBool(enabled, "mpesa_listener_enabled")
        _mpesaListenerFlow.value = enabled
    }

    override val isEquityListenerEnabled: StateFlow<Boolean> = _equityListenerFlow.asStateFlow()

    override suspend fun setEquityListenerEnabled(enabled: Boolean) {
        userDefaults.setBool(enabled, "equity_listener_enabled")
        _equityListenerFlow.value = enabled
    }

    override val budgetAlertsEnabled: StateFlow<Boolean> = _budgetAlertsEnabledFlow.asStateFlow()

    override suspend fun setBudgetAlertsEnabled(enabled: Boolean) {
        userDefaults.setBool(enabled, "budget_alerts_enabled")
        _budgetAlertsEnabledFlow.value = enabled
    }

    override val budgetAlertThresholds: StateFlow<Set<Int>> = _budgetAlertThresholdsFlow.asStateFlow()

    override suspend fun setBudgetAlertThresholds(thresholds: Set<Int>) {
        userDefaults.setObject(thresholds.map { it.toString() }, "budget_alert_thresholds")
        _budgetAlertThresholdsFlow.value = thresholds
    }

    override val alertBudgetId: StateFlow<String?> = _alertBudgetIdFlow.asStateFlow()

    override suspend fun setAlertBudgetId(budgetId: String?) {
        if (budgetId == null) {
            userDefaults.removeObjectForKey("alert_budget_id")
        } else {
            userDefaults.setObject(budgetId, "alert_budget_id")
        }
        _alertBudgetIdFlow.value = budgetId
    }

    override val isBillReminderEnabled: StateFlow<Boolean> = _billReminderEnabledFlow.asStateFlow()

    override suspend fun setBillReminderEnabled(enabled: Boolean) {
        userDefaults.setBool(enabled, "bill_reminder_enabled")
        _billReminderEnabledFlow.value = enabled
    }

    override val billReminderDaysBefore: StateFlow<Int> = _billReminderDaysBeforeFlow.asStateFlow()

    override suspend fun setBillReminderDaysBefore(days: Int) {
        userDefaults.setInteger(days.toLong(), "bill_reminder_days_before")
        _billReminderDaysBeforeFlow.value = days
    }

    override val isDailySummaryEnabled: StateFlow<Boolean> = _dailySummaryEnabledFlow.asStateFlow()

    override suspend fun setDailySummaryEnabled(enabled: Boolean) {
        userDefaults.setBool(enabled, "daily_summary_enabled")
        _dailySummaryEnabledFlow.value = enabled
    }

    override val isWeeklySummaryEnabled: StateFlow<Boolean> = _weeklySummaryEnabledFlow.asStateFlow()

    override suspend fun setWeeklySummaryEnabled(enabled: Boolean) {
        userDefaults.setBool(enabled, "weekly_summary_enabled")
        _weeklySummaryEnabledFlow.value = enabled
    }

    override val summaryNotificationTime: StateFlow<LocalTime> = _summaryNotificationTimeFlow.asStateFlow()

    override suspend fun setSummaryNotificationTime(time: LocalTime) {
        userDefaults.setObject(time.toString(), "summary_notification_time")
        _summaryNotificationTimeFlow.value = time
    }

    override val showDecimals: StateFlow<Boolean> = _showDecimalsFlow.asStateFlow()

    override suspend fun setShowDecimals(show: Boolean) {
        userDefaults.setBool(show, "show_decimals")
        _showDecimalsFlow.value = show
    }

    override val defaultAccountId: StateFlow<String?> = _defaultAccountIdFlow.asStateFlow()

    override suspend fun setDefaultAccountId(id: String?) {
        if (id == null) {
            userDefaults.removeObjectForKey("default_account_id")
        } else {
            userDefaults.setObject(id, "default_account_id")
        }
        _defaultAccountIdFlow.value = id
    }

    override val exportFormat: StateFlow<ExportFormat> = _exportFormatFlow.asStateFlow()

    override suspend fun setExportFormat(format: ExportFormat) {
        userDefaults.setObject(format.name, "export_format")
        _exportFormatFlow.value = format
    }

    override val isSmsRationaleHidden: StateFlow<Boolean> = _smsRationaleHiddenFlow.asStateFlow()

    override suspend fun setSmsRationaleHidden(hidden: Boolean) {
        userDefaults.setBool(hidden, "sms_rationale_hidden")
        _smsRationaleHiddenFlow.value = hidden
    }

    override suspend fun clear() {
        val keys = userDefaults.dictionaryRepresentation().keys
        keys.forEach { key ->
            if (key is String) userDefaults.removeObjectForKey(key)
        }
        
        _themeFlow.value = AppTheme.SYSTEM
        _timeFormatFlow.value = TimeFormat.TWENTY_FOUR_HOUR
        _currencyFlow.value = Currency.KES
        _biometricFlow.value = false
        _balanceHiddenFlow.value = false
        _reminderFlow.value = false
        _reminderTimeFlow.value = LocalTime(20, 0)
        _mpesaSimSlotFlow.value = null
        _mpesaLinkedAccountIdsFlow.value = emptySet()
        _equityLinkedAccountIdsFlow.value = emptySet()
        _mpesaListenerFlow.value = false
        _equityListenerFlow.value = false
        _budgetAlertsEnabledFlow.value = false
        _budgetAlertThresholdsFlow.value = setOf(50, 80, 100)
        _alertBudgetIdFlow.value = null
        _billReminderEnabledFlow.value = false
        _billReminderDaysBeforeFlow.value = 2
        _dailySummaryEnabledFlow.value = false
        _weeklySummaryEnabledFlow.value = false
        _summaryNotificationTimeFlow.value = LocalTime(8, 0)
        _showDecimalsFlow.value = true
        _defaultAccountIdFlow.value = null
        _exportFormatFlow.value = ExportFormat.CSV
        _smsRationaleHiddenFlow.value = false
    }
}

actual fun createSettingsDataSource(): SettingsDataSource {
    return IOSSettingsDataSource()
}
