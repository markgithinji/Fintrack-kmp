package com.fintrack.shared.feature.settings.domain.util

import com.fintrack.shared.feature.transaction.domain.model.Transaction
import kotlinx.datetime.LocalTime

interface NotificationService {
    fun showReminderNotification()
    fun scheduleDailyReminder(time: LocalTime? = null)
    fun cancelDailyReminder()
    fun requestPermission(callback: (Boolean) -> Unit)
    fun showTransactionNotification(transaction: Transaction)
    fun showBudgetAlertNotification(budgetName: String, threshold: Int)
}
