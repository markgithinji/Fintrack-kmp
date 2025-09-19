package com.fintrack.shared.feature.Auth.data

object SessionManager {
    var token: String? = null
        private set

    fun saveToken(newToken: String) {
        token = newToken
        println("Token saved: $token")
    }

    fun clearToken() {
        token = null
        println("Token cleared")
    }
}