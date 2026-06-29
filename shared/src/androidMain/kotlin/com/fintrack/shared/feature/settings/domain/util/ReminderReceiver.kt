package com.fintrack.shared.feature.settings.domain.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fintrack.shared.feature.settings.domain.datasource.SettingsDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ReminderReceiver : BroadcastReceiver(), KoinComponent {
    private val notificationService: NotificationService by inject()
    private val settingsDataSource: SettingsDataSource by inject()

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val isEnabled = settingsDataSource.isReminderEnabled.first()
                if (!isEnabled) return@launch

                val time = settingsDataSource.reminderTime.first()

                when (action) {
                    "com.fintrack.shared.ACTION_SHOW_REMINDER" -> {
                        notificationService.showReminderNotification()
                        // Re-schedule for next day
                        notificationService.scheduleDailyReminder(time)
                    }
                    Intent.ACTION_BOOT_COMPLETED -> {
                        // Restore alarm after reboot
                        notificationService.scheduleDailyReminder(time)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
