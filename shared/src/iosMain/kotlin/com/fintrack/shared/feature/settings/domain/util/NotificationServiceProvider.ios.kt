package com.fintrack.shared.feature.settings.domain.util

import kotlinx.datetime.LocalTime
import platform.UserNotifications.*
import platform.Foundation.*

class IOSNotificationService : NotificationService {
    
    override fun showReminderNotification() {
        val content = UNMutableNotificationContent().apply {
            setTitle("Fintrack Reminder")
            setBody("Don't forget to log your transactions today!")
            setSound(UNNotificationSound.defaultSound)
        }

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "immediate_reminder",
            content = content,
            trigger = null
        )

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
            if (error != null) {
                println("Error showing immediate notification: ${error.localizedDescription}")
            }
        }
    }

    override fun scheduleDailyReminder(time: LocalTime?) {
        val reminderTime = time ?: LocalTime(20, 0)
        
        val content = UNMutableNotificationContent().apply {
            setTitle("Fintrack Reminder")
            setBody("Don't forget to log your transactions today!")
            setSound(UNNotificationSound.defaultSound)
        }

        val dateComponents = NSDateComponents().apply {
            hour = reminderTime.hour.toLong()
            minute = reminderTime.minute.toLong()
        }

        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            dateComponents = dateComponents,
            repeats = true
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "daily_transaction_reminder",
            content = content,
            trigger = trigger
        )

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
            if (error != null) {
                println("Error scheduling daily reminder: ${error.localizedDescription}")
            }
        }
    }

    override fun cancelDailyReminder() {
        UNUserNotificationCenter.currentNotificationCenter().removePendingNotificationRequestsWithIdentifiers(
            listOf("daily_transaction_reminder")
        )
    }

    override fun requestPermission(callback: (Boolean) -> Unit) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        
        center.requestAuthorizationWithOptions(options) { granted, error ->
            callback(granted)
            if (error != null) {
                println("Error requesting notification permission: ${error.localizedDescription}")
            }
        }
    }
}

actual fun createNotificationService(): NotificationService {
    return IOSNotificationService()
}
