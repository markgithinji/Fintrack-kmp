package com.fintrack.shared.feature.settings.data.local

import com.fintrack.shared.feature.settings.domain.datasource.SettingsDataSource
import com.fintrack.shared.feature.settings.domain.model.Currency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.Foundation.NSUserDefaults

class IOSSettingsDataSource : SettingsDataSource {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val _currencyFlow = MutableStateFlow(Currency.KES)
    private val _biometricFlow = MutableStateFlow(false)
    private val _balanceHiddenFlow = MutableStateFlow(false)

    init {
        val currencyCode = userDefaults.stringForKey("currency_code") ?: Currency.KES.code
        _currencyFlow.value = Currency.fromCode(currencyCode)
        
        val biometricEnabled = userDefaults.boolForKey("biometric_enabled")
        _biometricFlow.value = biometricEnabled

        val balanceHidden = userDefaults.boolForKey("balance_hidden")
        _balanceHiddenFlow.value = balanceHidden
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
}

actual fun createSettingsDataSource(): SettingsDataSource {
    return IOSSettingsDataSource()
}