package com.fintrack.shared.feature.core.ui.permission

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fintrack.shared.feature.core.logger.KMPLogger
import kotlinx.coroutines.delay

@Composable
actual fun SmsPermissionLauncher(
    trigger: Boolean,
    onResult: (Boolean) -> Unit,
    onDismissTrigger: () -> Unit
) {
    val logger = remember { KMPLogger() }
    val context = LocalContext.current
    val activity = context as? Activity

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            logger.info("PERMISSION", "SmsPermissionLauncher result: $permissions")
            val readGranted = permissions[Manifest.permission.READ_SMS] == true
            logger.info("PERMISSION", "READ_SMS granted: $readGranted")
            onResult(readGranted)
        }
    )

    LaunchedEffect(trigger) {
        if (trigger) {
            logger.info("PERMISSION", "SmsPermissionLauncher triggered. Checking current state...")
            
            val currentReadStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS)
            val shouldShowRationale = activity?.let { 
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.READ_SMS) 
            } ?: false
            
            logger.info("PERMISSION", "Current READ_SMS status: $currentReadStatus (0=GRANTED, -1=DENIED)")
            logger.info("PERMISSION", "Should show system rationale: $shouldShowRationale")

            if (currentReadStatus == PackageManager.PERMISSION_GRANTED) {
                logger.info("PERMISSION", "Permission already granted, triggering success immediately")
                onResult(true)
                return@LaunchedEffect
            }

            // Small delay to ensure any preceding dialog dismissals or window transitions are settled
            delay(300)
            
            try {
                logger.info("PERMISSION", "Launching system permission request...")
                launcher.launch(
                    arrayOf(
                        Manifest.permission.READ_SMS,
                        Manifest.permission.RECEIVE_SMS
                    )
                )
            } catch (e: Exception) {
                logger.error("PERMISSION", "Failed to launch SMS permission request", e)
                onDismissTrigger()
            }
        }
    }
}
