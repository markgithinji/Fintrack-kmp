package com.fintrack.shared.feature.settings.domain.util

class IOSNotificationService : NotificationService {
    override fun showReminderNotification() {}
    override fun scheduleDailyReminder() {}
    override fun cancelDailyReminder() {}
    override fun requestPermission(callback: (Boolean) -> Unit) {
        callback(true)
    }
}

actual fun createNotificationService(): NotificationService {
    return IOSNotificationService()
}
