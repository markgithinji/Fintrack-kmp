package com.fintrack.shared.feature.settings.domain.util

import androidx.compose.runtime.Composable

interface BiometricAuthenticatorProvider {
    fun getAuthenticator(): BiometricAuthenticator
}

@Composable
expect fun rememberBiometricAuthenticator(): BiometricAuthenticator

expect fun createBiometricAuthenticator(): BiometricAuthenticator

