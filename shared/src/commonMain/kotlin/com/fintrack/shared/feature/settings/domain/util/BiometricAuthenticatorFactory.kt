package com.fintrack.shared.feature.settings.domain.util

interface BiometricAuthenticatorProvider {
    fun getAuthenticator(): BiometricAuthenticator
}

expect fun createBiometricAuthenticator(): BiometricAuthenticator
