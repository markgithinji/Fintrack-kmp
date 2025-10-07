package com.fintrack.shared.feature.core

enum class Environment {
    DEVELOPMENT,
    STAGING,
    PRODUCTION
}

object ApiConfig {
    private var currentEnvironment: Environment = Environment.STAGING

    val BASE_URL: String
        get() = when (currentEnvironment) {
            Environment.DEVELOPMENT -> "http://172.21.111.247:8080"
            Environment.STAGING -> "https://staging-api.example.com"
            Environment.PRODUCTION -> "https://api.example.com"
        }

    fun initialize(environment: Environment) {
        currentEnvironment = environment
    }
}
//10.176.101.247
//192.168.100.96