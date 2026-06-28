package com.fintrack.shared.feature.settings.domain.util

import kotlinx.datetime.LocalTime

interface NotificationService {
    fun showReminderNotification()
    fun scheduleDailyReminder(time: LocalTime? = null)
    fun cancelDailyReminder()
    fun requestPermission(callback: (Boolean) -> Unit)
}
