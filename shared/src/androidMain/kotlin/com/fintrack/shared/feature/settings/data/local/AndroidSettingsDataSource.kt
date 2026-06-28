package com.fintrack.shared.feature.settings.data.local

import android.content.Context
import com.fintrack.shared.feature.settings.domain.datasource.SettingsDataSource
import com.fintrack.shared.feature.settings.domain.model.Currency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import androidx.core.content.edit

class AndroidSettingsDataSource(
    private val context: Context
) : SettingsDataSource {

    private val prefs = context.getSharedPreferences("fintrack_settings", Context.MODE_PRIVATE)
    private val _currencyFlow = MutableStateFlow(Currency.KES)
    private val _biometricFlow = MutableStateFlow(false)
    private val _balanceHiddenFlow = MutableStateFlow(false)

    init {
        val currencyCode = prefs.getString("currency_code", Currency.KES.code)
        _currencyFlow.update { Currency.fromCode(currencyCode) }
        
        val biometricEnabled = prefs.getBoolean("biometric_enabled", false)
        _biometricFlow.update { biometricEnabled }

        val balanceHidden = prefs.getBoolean("balance_hidden", false)
        _balanceHiddenFlow.update { balanceHidden }
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
}