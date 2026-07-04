package com.fintrack.shared.feature.settings.domain.util

import android.content.Context
import com.fintrack.shared.feature.settings.domain.datasource.SettingsDataSource

private lateinit var appContext: Context

fun initNotificationService(context: Context) {
    appContext = context.applicationContext
}

actual fun createNotificationService(settingsDataSource: SettingsDataSource): NotificationService {
    return AndroidNotificationService(appContext, settingsDataSource)
}
