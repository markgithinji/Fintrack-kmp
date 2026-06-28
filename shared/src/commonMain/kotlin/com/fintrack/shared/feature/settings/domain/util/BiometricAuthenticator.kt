package com.fintrack.shared.feature.settings.domain.util

interface BiometricAuthenticator {
    suspend fun authenticate(
        title: String,
        subtitle: String
    ): BiometricResult
}

sealed class BiometricResult {
    object Success : BiometricResult()
    data class Error(val message: String) : BiometricResult()
    object NotAvailable : BiometricResult()
}
