package com.fintrack.shared.feature.settings.data.local

import android.content.Context
import com.fintrack.shared.feature.settings.domain.datasource.SettingsDataSource
import com.fintrack.shared.feature.settings.domain.model.AppTheme
import com.fintrack.shared.feature.settings.domain.model.Currency
import com.fintrack.shared.feature.settings.domain.model.TimeFormat
import kotlinx.coroutines.flow.Flow
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
    }

    override val theme: Flow<AppTheme> = _themeFlow

    override suspend fun setTheme(theme: AppTheme) {
        prefs.edit(commit = true) {
            putString("app_theme", theme.name)
        }
        _themeFlow.value = theme
    }

    override val timeFormat: Flow<TimeFormat> = _timeFormatFlow

    override suspend fun setTimeFormat(format: TimeFormat) {
        prefs.edit(commit = true) {
            putString("time_format", format.name)
        }
        _timeFormatFlow.value = format
    }

    override val currency: Flow<Currency> = _currencyFlow

    override suspend fun setCurrency(currency: Currency) {
        prefs.edit(commit = true) {
            putString("currency_code", currency.code)
        }
        _currencyFlow.value = currency
    }

    override val isBiometricEnabled: Flow<Boolean> = _biometricFlow

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit(commit = true) {
            putBoolean("biometric_enabled", enabled)
        }
        _biometricFlow.value = enabled
    }

    override val isBalanceHidden: Flow<Boolean> = _balanceHiddenFlow

    override suspend fun setBalanceHidden(hidden: Boolean) {
        prefs.edit(commit = true) {
            putBoolean("balance_hidden", hidden)
        }
        _balanceHiddenFlow.value = hidden
    }

    override val isReminderEnabled: Flow<Boolean> = _reminderFlow

    override suspend fun setReminderEnabled(enabled: Boolean) {
        prefs.edit(commit = true) {
            putBoolean("reminder_enabled", enabled)
        }
        _reminderFlow.value = enabled
    }

    override val reminderTime: Flow<LocalTime> = _reminderTimeFlow

    override suspend fun setReminderTime(time: LocalTime) {
        prefs.edit(commit = true) {
            putString("reminder_time", time.toString())
        }
        _reminderTimeFlow.value = time
    }

    override val mpesaSimSlot: Flow<Int?> = _mpesaSimSlotFlow

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

    override val mpesaAccountId: Flow<String?> = _mpesaAccountIdFlow

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
}