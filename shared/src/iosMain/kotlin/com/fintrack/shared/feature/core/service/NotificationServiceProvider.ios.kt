package com.fintrack.shared.feature.core.service

import com.fintrack.shared.feature.core.domain.service.NotificationService
import com.fintrack.shared.feature.core.util.formatToAmount
import com.fintrack.shared.feature.settings.domain.datasource.SettingsDataSource
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import platform.UserNotifications.*
import platform.Foundation.*

class IOSNotificationService(
    private val settingsDataSource: SettingsDataSource
) : NotificationService {
    
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

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { _ ->
            // Handle error silently
        }
    }

    override fun showTransactionNotification(transaction: Transaction) {
        val showDecimals = runBlocking { settingsDataSource.showDecimals.first() }
        val amountStr = transaction.amount.formatToAmount(showDecimals = showDecimals)

        val content = UNMutableNotificationContent().apply {
            setTitle("New Transaction Detected")
            val emoji = if (transaction.isIncome) "💰" else "💸"
            val type = if (transaction.isIncome) "received" else "spent"
            setBody("$emoji Ksh $amountStr $type for ${transaction.category}. Tap to change.")
            setSound(UNNotificationSound.defaultSound)
            setUserInfo(mapOf("transactionId" to transaction.id))
        }

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "transaction_${transaction.id}",
            content = content,
            trigger = null
        )

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { _ ->
            // Handle error silently
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

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { _ ->
            // Handle error silently
        }
    }

    override fun showBillReminderNotification(billName: String, amount: BigDecimal) {
        val showDecimals = runBlocking { settingsDataSource.showDecimals.first() }
        val amountStr = amount.formatToAmount(showDecimals = showDecimals)
        
        val content = UNMutableNotificationContent().apply {
            setTitle("Upcoming Bill: $billName")
            setBody("Your bill of Ksh $amountStr is due soon.")
            setSound(UNNotificationSound.defaultSound)
        }

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "bill_reminder_$billName",
            content = content,
            trigger = null
        )

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { _ ->
            // Handle error silently
        }
    }

    override fun scheduleBillReminder(billName: String, amount: BigDecimal, dueDate: LocalDate, daysBefore: Int) {
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

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { _ ->
            // Handle error silently
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

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { _ ->
            // Handle error silently
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

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { _ ->
            // Handle error silently
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

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { _ ->
            // Handle error silently
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
        
        center.requestAuthorizationWithOptions(options) { granted, _ ->
            callback(granted)
        }
    }
}

actual fun createNotificationService(settingsDataSource: SettingsDataSource): NotificationService {
    return IOSNotificationService(settingsDataSource)
}
