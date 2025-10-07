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

            expectSuccess = true // This will throw exceptions for non-2xx responses

            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }.apply {
            plugin(HttpSend).intercept { request ->
                val token = tokenProvider.getToken()
                if (!token.isNullOrEmpty()) {
                    request.headers.append("Authorization", "Bearer $token")
                }
                execute(request)
            }
        }
    }
}