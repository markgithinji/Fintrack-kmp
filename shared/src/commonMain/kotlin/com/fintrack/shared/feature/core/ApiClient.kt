package com.fintrack.shared.feature.core

import com.fintrack.shared.feature.auth.data.local.TokenProvider
import com.fintrack.shared.feature.auth.data.local.TokenProviderImpl
import com.fintrack.shared.feature.auth.data.local.createTokenDataStore
import com.fintrack.shared.feature.auth.data.repository.TokenRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.plugin
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object ApiClient {
    private val logger = KMPLogger()
    private val tokenRepository = TokenRepository(createTokenDataStore())
    private val tokenProvider: TokenProvider = TokenProviderImpl(tokenRepository)

    val httpClient: HttpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                })
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 30000L
                connectTimeoutMillis = 10000L
                socketTimeoutMillis = 30000L
            }

            expectSuccess = true

            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }.apply {
            // Add network monitoring
            NetworkMonitorInterceptor(logger).setupNetworkMonitoring(this)

            plugin(HttpSend).intercept { request ->
                logger.debug("ApiClient", "Making request to: ${request.url}")

                val token = tokenProvider.getToken()
                if (!token.isNullOrEmpty()) {
                    request.headers.append("Authorization", "Bearer $token")
                    logger.debug("ApiClient", "Added auth token to request")
                } else {
                    logger.warning("ApiClient", "No auth token available")
                }

                execute(request)
            }
        }
    }
}