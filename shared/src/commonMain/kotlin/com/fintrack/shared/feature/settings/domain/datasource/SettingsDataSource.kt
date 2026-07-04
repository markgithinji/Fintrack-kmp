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

    val mpesaSimSlot: Flow<Int?>
    suspend fun setMpesaSimSlot(slot: Int?)

    val mpesaAccountId: Flow<String?>
    suspend fun setMpesaAccountId(accountId: String?)

    val isMpesaListenerEnabled: Flow<Boolean>
    suspend fun setMpesaListenerEnabled(enabled: Boolean)

    val budgetAlertsEnabled: Flow<Boolean>
    suspend fun setBudgetAlertsEnabled(enabled: Boolean)

    val budgetAlertThresholds: Flow<Set<Int>>
    suspend fun setBudgetAlertThresholds(thresholds: Set<Int>)

    val alertBudgetId: Flow<String?>
    suspend fun setAlertBudgetId(budgetId: String?)

    val isBillReminderEnabled: Flow<Boolean>
    suspend fun setBillReminderEnabled(enabled: Boolean)

    val billReminderDaysBefore: Flow<Int>
    suspend fun setBillReminderDaysBefore(days: Int)

    val isDailySummaryEnabled: Flow<Boolean>
    suspend fun setDailySummaryEnabled(enabled: Boolean)

    val isWeeklySummaryEnabled: Flow<Boolean>
    suspend fun setWeeklySummaryEnabled(enabled: Boolean)

    val summaryNotificationTime: Flow<LocalTime>
    suspend fun setSummaryNotificationTime(time: LocalTime)
}