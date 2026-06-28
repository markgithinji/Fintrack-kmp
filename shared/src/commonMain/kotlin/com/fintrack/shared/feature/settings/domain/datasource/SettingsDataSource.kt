package com.fintrack.shared.feature.settings.domain.datasource

import com.fintrack.shared.feature.settings.domain.model.AppTheme
import com.fintrack.shared.feature.settings.domain.model.Currency
import com.fintrack.shared.feature.settings.domain.model.TimeFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalTime

interface SettingsDataSource {
    val theme: Flow<AppTheme>
    suspend fun setTheme(theme: AppTheme)

    val timeFormat: Flow<TimeFormat>
    suspend fun setTimeFormat(format: TimeFormat)

    val currency: Flow<Currency>
    suspend fun setCurrency(currency: Currency)

    val isBiometricEnabled: Flow<Boolean>
    suspend fun setBiometricEnabled(enabled: Boolean)

    val isBalanceHidden: Flow<Boolean>
    suspend fun setBalanceHidden(hidden: Boolean)

    val isReminderEnabled: Flow<Boolean>
    suspend fun setReminderEnabled(enabled: Boolean)

    val reminderTime: Flow<LocalTime>
    suspend fun setReminderTime(time: LocalTime)
}