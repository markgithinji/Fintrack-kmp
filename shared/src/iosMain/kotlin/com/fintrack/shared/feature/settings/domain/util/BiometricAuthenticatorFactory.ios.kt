package com.fintrack.shared.feature.settings.domain.util

actual fun createBiometricAuthenticator(): BiometricAuthenticator {
    return IOSBiometricAuthenticator()
}
