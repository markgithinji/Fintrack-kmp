package com.fintrack.shared.feature.core.data.remote

import com.fintrack.shared.feature.auth.data.model.AuthResponseDto
import com.fintrack.shared.feature.auth.domain.datasource.TokenDataSource
import com.fintrack.shared.feature.core.logger.KMPLogger
import com.fintrack.shared.feature.core.logger.LogTags
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.plugin
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json

class ApiClient(
    private val tokenDataSource: TokenDataSource,
    private val logger: KMPLogger,
    private val baseUrl: String
) {
    val httpClient: HttpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                })
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        this@ApiClient.logger.debug(LogTags.NETWORK, message)
                    }
                }
                level = LogLevel.ALL
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 30000L
                connectTimeoutMillis = 10000L
                socketTimeoutMillis = 30000L
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        val accessToken = tokenDataSource.accessToken.firstOrNull()
                        val refreshToken = tokenDataSource.refreshToken.firstOrNull()
                        if (accessToken != null && refreshToken != null) {
                            BearerTokens(accessToken, refreshToken)
                        } else {
                            null
                        }
                    }

                    refreshTokens {
                        val refreshToken = tokenDataSource.refreshToken.firstOrNull()
                        if (refreshToken == null) return@refreshTokens null

                        try {
                            // We use a separate client or a request that bypasses Auth to avoid recursion
                            val response = client.post("$baseUrl/auth/refresh") {
                                contentType(ContentType.Application.Json)
                                setBody(mapOf("refreshToken" to refreshToken))
                                // Mark this request to bypass Auth plugin if possible, 
                                // but Ktor Auth bearer usually handles this by not adding headers if loadTokens returns null or if it's the refresh call.
                            }.body<AuthResponseDto>()

                            tokenDataSource.saveTokens(response.accessToken, response.refreshToken)
                            BearerTokens(response.accessToken, response.refreshToken)
                        } catch (e: Exception) {
                            logger.error(LogTags.AUTH, "Failed to refresh token: ${e.message}")
                            tokenDataSource.clearTokens()
                            null
                        }
                    }
                    
                    sendWithoutRequest { request ->
                        val path = request.url.build().encodedPath
                        path.contains("/auth/login") || 
                        path.contains("/auth/register") || 
                        path.contains("/auth/refresh")
                    }
                }
            }

            expectSuccess = true
            defaultRequest { contentType(ContentType.Application.Json) }
        }.apply {
            NetworkMonitorInterceptor(logger).setupNetworkMonitoring(this)
        }
    }
}