package com.fintrack.shared.feature.core

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin

class NetworkMonitorInterceptor(private val logger: KMPLogger) {

    fun setupNetworkMonitoring(client: HttpClient) {
        client.plugin(HttpSend).intercept { request ->
            logger.debug("NetworkMonitor", "Request: ${request.method.value} ${request.url}")

            val call = execute(request)

            val status = call.response.status
            val url = call.request.url

            // Log incoming response
            logger.debug("NetworkMonitor", "Response: $status for $url")

            call
        }
    }
}
