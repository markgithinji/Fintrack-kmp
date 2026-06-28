package com.fintrack.shared.feature.settings.domain.util

interface NotificationService {
    fun showReminderNotification()
    fun scheduleDailyReminder()
    fun cancelDailyReminder()
    fun requestPermission(callback: (Boolean) -> Unit)
}
