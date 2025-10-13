package com.fintrack.shared.feature.core.data.remote

import com.fintrack.shared.feature.core.logger.KMPLogger
import com.fintrack.shared.feature.core.logger.LogTags
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin

class NetworkMonitorInterceptor(private val logger: KMPLogger) {

    fun setupNetworkMonitoring(client: HttpClient) {
        client.plugin(HttpSend.Plugin).intercept { request ->
            logger.debug(LogTags.NETWORK, "Request: ${request.method.value} ${request.url}")

            val call = execute(request)

            val status = call.response.status
            val url = call.request.url

            // Log incoming response
            logger.debug(LogTags.NETWORK, "Response: $status for $url")

            call
        }
    }
}