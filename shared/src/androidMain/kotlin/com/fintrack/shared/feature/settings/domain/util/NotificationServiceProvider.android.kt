package com.fintrack.shared.feature.settings.domain.util

import android.content.Context

private lateinit var appContext: Context

fun initNotificationService(context: Context) {
    appContext = context.applicationContext
}

actual fun createNotificationService(): NotificationService {
    return AndroidNotificationService(appContext)
}
