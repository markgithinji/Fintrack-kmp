package com.fintrack.shared.feature.core.data.remote

import com.fintrack.shared.feature.auth.data.model.AuthResponseDto
import com.fintrack.shared.feature.auth.domain.datasource.TokenDataSource
import com.fintrack.shared.feature.core.logger.KMPLogger
import com.fintrack.shared.feature.core.logger.LogTags
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
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
    private val baseUrl: String,
) {
    val httpClient: HttpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    }
                )
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
                        
                        if (!accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
                            logger.debug(LogTags.AUTH, "Loading tokens for request: ${accessToken.take(10)}...")
                            BearerTokens(accessToken, refreshToken)
                        } else {
                            logger.debug(LogTags.AUTH, "No tokens available for request")
                            null
                        }
                    }

                    refreshTokens {
                        val refreshToken = tokenDataSource.refreshToken.firstOrNull()
                        if (refreshToken == null) {
                            logger.warning(LogTags.AUTH, "Refresh triggered but no refresh token found in storage")
                            return@refreshTokens null
                        }

                        try {
                            logger.info(LogTags.AUTH, "Attempting to refresh token...")
                            // Note: Ktor's bearer auth automatically excludes the refresh request 
                            // from further auth interceptors to avoid infinite loops.
                            val response = client.post("$baseUrl/auth/refresh") {
                                contentType(ContentType.Application.Json)
                                setBody(mapOf("refreshToken" to refreshToken))
                            }.body<AuthResponseDto>()

                            logger.info(LogTags.AUTH, "Token refreshed successfully")
                            tokenDataSource.saveTokens(response.accessToken, response.refreshToken)
                            BearerTokens(response.accessToken, response.refreshToken)
                        } catch (e: Exception) {
                            logger.error(LogTags.AUTH, "Failed to refresh token: ${e.message}")
                            
                            val isAuthError = when (e) {
                                is ClientRequestException -> {
                                    val status = e.response.status.value
                                    status == 401 || status == 403
                                }
                                else -> e.message?.contains("401") == true || e.message?.contains("403") == true
                            }

                            if (isAuthError) {
                                logger.warning(LogTags.AUTH, "Refresh token invalid or expired (401/403). Clearing session.")
                                tokenDataSource.clearTokens()
                            }
                            null
                        }
                    }
                    
                    sendWithoutRequest { request ->
                        val path = request.url.build().encodedPath
                        // Send credentials for everything EXCEPT public auth endpoints
                        !(path.contains("/auth/login") || 
                          path.contains("/auth/register") || 
                          path.contains("/auth/refresh"))
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