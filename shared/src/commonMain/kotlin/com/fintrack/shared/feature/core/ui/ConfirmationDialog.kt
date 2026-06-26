package com.fintrack.shared.feature.core.ui

import androidx.compose.runtime.Composable

@Composable
expect fun ConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String,
    cancelLabel: String = "Cancel",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
)
