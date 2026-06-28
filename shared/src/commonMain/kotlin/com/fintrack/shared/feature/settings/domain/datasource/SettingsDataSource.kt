package com.fintrack.shared.feature.settings.domain.datasource

import com.fintrack.shared.feature.settings.domain.model.AppTheme
import com.fintrack.shared.feature.settings.domain.model.Currency
import kotlinx.coroutines.flow.Flow

interface SettingsDataSource {
    val theme: Flow<AppTheme>
    suspend fun setTheme(theme: AppTheme)

    val currency: Flow<Currency>
    suspend fun setCurrency(currency: Currency)

    val isBiometricEnabled: Flow<Boolean>
    suspend fun setBiometricEnabled(enabled: Boolean)

    val isBalanceHidden: Flow<Boolean>
    suspend fun setBalanceHidden(hidden: Boolean)
}