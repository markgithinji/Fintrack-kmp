package com.fintrack.shared.feature.transaction.ui

import androidx.compose.runtime.Composable

@Composable
expect fun SmsPermissionLauncher(
    trigger: Boolean,
    onResult: (Boolean) -> Unit,
    onDismissTrigger: () -> Unit
)
