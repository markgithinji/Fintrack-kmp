package com.fintrack.shared.feature.core.ui.biometric

import androidx.compose.runtime.Composable

interface BiometricAuthenticatorProvider {
    fun getAuthenticator(): BiometricAuthenticator
}

@Composable
expect fun rememberBiometricAuthenticator(): BiometricAuthenticator

expect fun createBiometricAuthenticator(): BiometricAuthenticator
