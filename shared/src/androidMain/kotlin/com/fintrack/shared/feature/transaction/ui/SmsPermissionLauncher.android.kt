package com.fintrack.shared.feature.transaction.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun SmsPermissionLauncher(
    trigger: Boolean,
    onResult: (Boolean) -> Unit,
    onDismissTrigger: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onResult
    )

    LaunchedEffect(trigger) {
        if (trigger) {
            launcher.launch(Manifest.permission.READ_SMS)
            onDismissTrigger()
        }
    }
}
