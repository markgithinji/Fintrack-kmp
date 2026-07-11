package com.fintrack.shared.feature.settings.domain.util

import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity

private var currentActivity: FragmentActivity? = null

fun initBiometricAuthenticator(activity: FragmentActivity) {
    currentActivity = activity
}

@Composable
actual fun rememberBiometricAuthenticator(): BiometricAuthenticator {
    val context = LocalContext.current
    return remember(context) {
        val activity = context as? FragmentActivity 
            ?: (context as? ContextWrapper)?.baseContext as? FragmentActivity
            ?: error("Context must be FragmentActivity. Current context: $context")
        AndroidBiometricAuthenticator(activity)
    }
}

actual fun createBiometricAuthenticator(): BiometricAuthenticator {
    return AndroidBiometricAuthenticator(currentActivity!!)
}
