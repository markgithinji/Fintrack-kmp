package com.fintrack.shared.feature.settings.data.local

import com.fintrack.shared.feature.settings.domain.datasource.SettingsDataSource
import com.fintrack.shared.feature.settings.domain.model.AppTheme
import com.fintrack.shared.feature.settings.domain.model.Currency
import com.fintrack.shared.feature.settings.domain.model.TimeFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
    }

    override val theme: Flow<AppTheme> = _themeFlow

    override suspend fun setTheme(theme: AppTheme) {
        userDefaults.setObject(theme.name, "app_theme")
        _themeFlow.value = theme
    }

    override val timeFormat: Flow<TimeFormat> = _timeFormatFlow

    override suspend fun setTimeFormat(format: TimeFormat) {
        userDefaults.setObject(format.name, "time_format")
        _timeFormatFlow.value = format
    }

    override val currency: Flow<Currency> = _currencyFlow

    override suspend fun setCurrency(currency: Currency) {
        userDefaults.setObject(currency.code, "currency_code")
        _currencyFlow.value = currency
    }

    override val isBiometricEnabled: Flow<Boolean> = _biometricFlow

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        userDefaults.setBool(enabled, "biometric_enabled")
        _biometricFlow.value = enabled
    }

    override val isBalanceHidden: Flow<Boolean> = _balanceHiddenFlow

    override suspend fun setBalanceHidden(hidden: Boolean) {
        userDefaults.setBool(hidden, "balance_hidden")
        _balanceHiddenFlow.value = hidden
    }

    override val isReminderEnabled: Flow<Boolean> = _reminderFlow

    override suspend fun setReminderEnabled(enabled: Boolean) {
        userDefaults.setBool(enabled, "reminder_enabled")
        _reminderFlow.value = enabled
    }

    override val reminderTime: Flow<LocalTime> = _reminderTimeFlow

    override suspend fun setReminderTime(time: LocalTime) {
        userDefaults.setObject(time.toString(), "reminder_time")
        _reminderTimeFlow.value = time
    }
}

actual fun createSettingsDataSource(): SettingsDataSource {
    return IOSSettingsDataSource()
}