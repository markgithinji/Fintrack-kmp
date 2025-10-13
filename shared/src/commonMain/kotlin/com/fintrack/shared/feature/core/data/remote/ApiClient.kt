package com.fintrack.shared.feature.core.data.remote

import com.fintrack.shared.feature.auth.domain.repository.TokenDataSource
import com.fintrack.shared.feature.core.logger.KMPLogger
import com.fintrack.shared.feature.core.logger.LogTags
import com.fintrack.shared.feature.core.data.remote.NetworkMonitorInterceptor
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.plugin
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json

class ApiClient(
    private val tokenDataSource: TokenDataSource,
    private val logger: KMPLogger
) {
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
            defaultRequest { contentType(ContentType.Application.Json) }
        }.apply {
            NetworkMonitorInterceptor(logger).setupNetworkMonitoring(this)

            plugin(HttpSend).intercept { request ->
                logger.debug(LogTags.NETWORK, "Making request to: ${request.url}")

                val token = tokenDataSource.token.firstOrNull()
                if (!token.isNullOrEmpty()) {
                    request.headers.append("Authorization", "Bearer $token")
                    logger.debug(LogTags.AUTH, "Added auth token to request")
                } else {
                    logger.warning(LogTags.AUTH, "No auth token available")
                }

                execute(request)
            }
        }
    }
}