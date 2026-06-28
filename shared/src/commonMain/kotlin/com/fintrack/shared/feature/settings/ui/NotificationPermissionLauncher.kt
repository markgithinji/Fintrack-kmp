package com.fintrack.shared.feature.settings.ui

import androidx.compose.runtime.Composable

@Composable
expect fun NotificationPermissionLauncher(
    trigger: Boolean,
    onResult: (Boolean) -> Unit,
    onDismissTrigger: () -> Unit
)
