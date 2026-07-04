package com.fintrack.shared.feature.settings.domain.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fintrack.shared.feature.settings.domain.datasource.SettingsDataSource
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import com.fintrack.shared.feature.core.util.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ReminderReceiver : BroadcastReceiver(), KoinComponent {
    private val notificationService: NotificationService by inject()
    private val settingsDataSource: SettingsDataSource by inject()
    private val transactionRepository: TransactionRepository by inject()

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.Default).launch {
            try {
                when (action) {
                    "com.fintrack.shared.ACTION_SHOW_REMINDER" -> {
                        val isEnabled = settingsDataSource.isReminderEnabled.first()
                        if (isEnabled) {
                            notificationService.showReminderNotification()
                            val time = settingsDataSource.reminderTime.first()
                            notificationService.scheduleDailyReminder(time)
                        }
                    }
                    "com.fintrack.shared.ACTION_SHOW_BILL_REMINDER" -> {
                        val billName = intent.getStringExtra("billName") ?: "Bill"
                        val amount = intent.getDoubleExtra("amount", 0.0)
                        notificationService.showBillReminderNotification(billName, amount)
                    }
                    "com.fintrack.shared.ACTION_SHOW_SUMMARY" -> {
                        handleSummaryNotification()
                    }
                    Intent.ACTION_BOOT_COMPLETED -> {
                        // Restore alarms after reboot
                        val dailyEnabled = settingsDataSource.isReminderEnabled.first()
                        if (dailyEnabled) {
                            notificationService.scheduleDailyReminder(settingsDataSource.reminderTime.first())
                        }
                        
                        val summaryEnabled = settingsDataSource.isDailySummaryEnabled.first() || 
                                          settingsDataSource.isWeeklySummaryEnabled.first()
                        if (summaryEnabled) {
                            notificationService.scheduleSummaryNotification(settingsDataSource.summaryNotificationTime.first())
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleSummaryNotification() {
        val dailyEnabled = settingsDataSource.isDailySummaryEnabled.first()
        val weeklyEnabled = settingsDataSource.isWeeklySummaryEnabled.first()
        val time = settingsDataSource.summaryNotificationTime.first()

        if (dailyEnabled) {
            val yesterdaySpending = getSpendingForYesterday()
            notificationService.showSummaryNotification(
                title = "Daily Spending Summary",
                content = "You spent Ksh ${String.format(java.util.Locale.US, "%,.2f", yesterdaySpending)} yesterday."
            )
        }

        // Only show weekly on Sundays
        if (weeklyEnabled && java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SUNDAY) {
            val weeklySpending = getSpendingForLastWeek()
            notificationService.showSummaryNotification(
                title = "Weekly Spending Summary",
                content = "You spent Ksh ${String.format(java.util.Locale.US, "%,.2f", weeklySpending)} this past week."
            )
        }

        // Re-schedule
        if (dailyEnabled || weeklyEnabled) {
            notificationService.scheduleSummaryNotification(time)
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun getSpendingForYesterday(): Double {
        val timeZone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(timeZone).date
        val yesterday = today.minus(1, DateTimeUnit.DAY).toString()
        
        val result = transactionRepository.getTransactions(
            limit = 100,
            sortBy = "date",
            order = "DESC",
            startDate = yesterday,
            endDate = yesterday,
            isIncome = false
        )
        
        return if (result is Result.Success) {
            result.data.first.sumOf { it.amount }
        } else 0.0
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun getSpendingForLastWeek(): Double {
        val timeZone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(timeZone).date
        val lastWeekStart = today.minus(7, DateTimeUnit.DAY).toString()
        val yesterday = today.minus(1, DateTimeUnit.DAY).toString()
        
        val result = transactionRepository.getTransactions(
            limit = 500,
            sortBy = "date",
            order = "DESC",
            startDate = lastWeekStart,
            endDate = yesterday,
            isIncome = false
        )
        
        return if (result is Result.Success) {
            result.data.first.sumOf { it.amount }
        } else 0.0
    }
}
