package com.fintrack.shared.feature.settings.domain.util

import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference

private var currentActivityRef: WeakReference<FragmentActivity>? = null

fun initBiometricAuthenticator(activity: FragmentActivity) {
    currentActivityRef = WeakReference(activity)
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
    return object : BiometricAuthenticator {
        override suspend fun authenticate(title: String, subtitle: String): BiometricResult {
            val activity = currentActivityRef?.get() 
                ?: return BiometricResult.Error("Biometric authentication failed: Activity context is missing.")
            
            return AndroidBiometricAuthenticator(activity).authenticate(title, subtitle)
        }
    }
}
