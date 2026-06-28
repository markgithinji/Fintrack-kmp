package com.fintrack.shared.feature.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import platform.UserNotifications.*

@Composable
actual fun NotificationPermissionLauncher(
    trigger: Boolean,
    onResult: (Boolean) -> Unit,
    onDismissTrigger: () -> Unit
) {
    LaunchedEffect(trigger) {
        if (trigger) {
            val center = UNUserNotificationCenter.currentNotificationCenter()
            val options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
            
            center.requestAuthorizationWithOptions(options) { granted, _ ->
                onResult(granted)
            }
            onDismissTrigger()
        }
    }
}
