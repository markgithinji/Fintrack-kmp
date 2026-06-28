package com.fintrack.shared.feature.settings.domain.util

import android.content.Context
import androidx.fragment.app.FragmentActivity

private var currentActivity: FragmentActivity? = null

fun initBiometricAuthenticator(activity: FragmentActivity) {
    currentActivity = activity
}

actual fun createBiometricAuthenticator(): BiometricAuthenticator {
    return AndroidBiometricAuthenticator(currentActivity!!)
}

