package com.fintrack.shared.feature.core

import com.fintrack.shared.feature.auth.data.local.TokenProvider
import com.fintrack.shared.feature.auth.data.local.TokenProviderImpl
import com.fintrack.shared.feature.auth.data.local.createTokenDataStore
import com.fintrack.shared.feature.auth.data.repository.TokenRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.plugin
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object ApiClient {
    private val tokenRepository = TokenRepository(createTokenDataStore())
    private val tokenProvider: TokenProvider = TokenProviderImpl(tokenRepository)

    // Authenticated client for endpoints that need tokens
    val authenticatedClient: HttpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                })
            }

            install(HttpSend)
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

    // Simple client for endpoints that don't need tokens (like login/register)
    val simpleClient: HttpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                })
            }
        }
    }
}