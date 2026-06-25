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

    init {
        val currencyCode = prefs.getString("currency_code", Currency.KES.code)
        _currencyFlow.update { Currency.fromCode(currencyCode) }
    }

    override val currency: Flow<Currency> = _currencyFlow

    override suspend fun setCurrency(currency: Currency) {
        prefs.edit(commit = true) {
            putString("currency_code", currency.code)
        }
        _currencyFlow.value = currency
    }
}