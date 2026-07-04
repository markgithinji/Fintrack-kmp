package com.fintrack.shared.feature.core.data.remote

import com.fintrack.shared.feature.core.logger.KMPLogger
import com.fintrack.shared.feature.core.logger.LogTags
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin

class NetworkMonitorInterceptor(private val logger: KMPLogger) {

    fun setupNetworkMonitoring(client: HttpClient) {
        client.plugin(HttpSend.Plugin).intercept { request ->
            try {
                val call = execute(request)
                call
            } catch (e: Exception) {
                // Propagate the exception so safeApiCall can catch it, 
                // but ensure the interceptor itself doesn't cause an unhandled crash
                throw e
            }
        }
    }
}