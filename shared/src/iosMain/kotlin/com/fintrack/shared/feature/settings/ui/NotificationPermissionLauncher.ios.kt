package com.fintrack.shared.feature.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun NotificationPermissionLauncher(
    trigger: Boolean,
    onResult: (Boolean) -> Unit,
    onDismissTrigger: () -> Unit
) {
    LaunchedEffect(trigger) {
        if (trigger) {
            // iOS permission request would go here
            onResult(true)
            onDismissTrigger()
        }
    }
}
