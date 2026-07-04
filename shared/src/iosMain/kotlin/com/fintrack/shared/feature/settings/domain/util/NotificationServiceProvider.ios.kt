package com.fintrack.shared.feature.settings.domain.util

import com.fintrack.shared.feature.transaction.domain.model.Transaction
import kotlinx.datetime.LocalTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
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

    override fun showTransactionNotification(transaction: Transaction) {
        val content = UNMutableNotificationContent().apply {
            setTitle("New Transaction Detected")
            val emoji = if (transaction.isIncome) "💰" else "💸"
            val type = if (transaction.isIncome) "received" else "spent"
            setBody("$emoji Ksh ${transaction.amount} $type for ${transaction.category}. Tap to change.")
            setSound(UNNotificationSound.defaultSound)
            setUserInfo(mapOf("transactionId" to transaction.id))
        }

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "transaction_${transaction.id}",
            content = content,
            trigger = null
        )

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
            if (error != null) {
                println("Error showing transaction notification: ${error.localizedDescription}")
            }
        }
    }

    override fun showBudgetAlertNotification(budgetName: String, threshold: Int) {
        val content = UNMutableNotificationContent().apply {
            setTitle("Budget Alert: $budgetName")
            setBody("You've reached $threshold% of your budget limit.")
            setSound(UNNotificationSound.defaultSound)
        }

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "budget_alert_${budgetName}_$threshold",
            content = content,
            trigger = null
        )

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
            if (error != null) {
                println("Error showing budget alert notification: ${error.localizedDescription}")
            }
        }
    }

    override fun showBillReminderNotification(billName: String, amount: Double) {
        val content = UNMutableNotificationContent().apply {
            setTitle("Upcoming Bill: $billName")
            setBody("Your bill of Ksh $amount is due soon.")
            setSound(UNNotificationSound.defaultSound)
        }

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "bill_reminder_$billName",
            content = content,
            trigger = null
        )

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
            if (error != null) {
                println("Error showing bill reminder notification: ${error.localizedDescription}")
            }
        }
    }

    override fun scheduleBillReminder(billName: String, amount: Double, dueDate: LocalDate, daysBefore: Int) {
        val reminderDate = dueDate.minus(daysBefore, DateTimeUnit.DAY)
        
        val content = UNMutableNotificationContent().apply {
            setTitle("Upcoming Bill: $billName")
            setBody("Your bill of Ksh $amount is due soon.")
            setSound(UNNotificationSound.defaultSound)
        }

        val dateComponents = NSDateComponents().apply {
            setYear(reminderDate.year.toLong())
            setMonth((reminderDate.month.ordinal + 1).toLong())
            setDay(reminderDate.dayOfMonth.toLong())
            setHour(9)
            setMinute(0)
        }

        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            dateComponents = dateComponents,
            repeats = false
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "bill_reminder_$billName",
            content = content,
            trigger = trigger
        )

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
            if (error != null) {
                println("Error scheduling bill reminder: ${error.localizedDescription}")
            }
        }
    }

    override fun showSummaryNotification(title: String, content: String) {
        val notificationContent = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(content)
            setSound(UNNotificationSound.defaultSound)
        }

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "spending_summary",
            content = notificationContent,
            trigger = null
        )

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
            if (error != null) {
                println("Error showing summary notification: ${error.localizedDescription}")
            }
        }
    }

    override fun scheduleSummaryNotification(time: LocalTime) {
        val content = UNMutableNotificationContent().apply {
            setTitle("Spending Summary")
            setBody("Check out your spending summary!")
            setSound(UNNotificationSound.defaultSound)
        }

        val dateComponents = NSDateComponents().apply {
            setHour(time.hour.toLong())
            setMinute(time.minute.toLong())
        }

        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            dateComponents = dateComponents,
            repeats = true
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "scheduled_summary",
            content = content,
            trigger = trigger
        )

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
            if (error != null) {
                println("Error scheduling summary notification: ${error.localizedDescription}")
            }
        }
    }

    override fun cancelSummaryNotification() {
        UNUserNotificationCenter.currentNotificationCenter().removePendingNotificationRequestsWithIdentifiers(
            listOf("scheduled_summary")
        )
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
