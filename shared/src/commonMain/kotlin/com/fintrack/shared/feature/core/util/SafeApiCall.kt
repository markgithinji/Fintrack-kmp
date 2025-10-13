package com.fintrack.shared.feature.core.util

import com.fintrack.shared.feature.core.data.domain.ApiException
import com.fintrack.shared.feature.core.logger.KMPLogger
import com.fintrack.shared.feature.core.logger.LogTags
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
private val logger = KMPLogger()

suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
    return try {
        val result = apiCall()
        logger.debug(LogTags.API, "API call completed successfully")
        Result.Success(result)
    } catch (e: Exception) {
        val domainException = convertToDomainException(e)
        logger.error(LogTags.API, "API call failed: ${domainException.details}", e)
        Result.Error(domainException)
    }
}

private fun convertToDomainException(e: Exception): ApiException = when (e) {
    is ApiException -> {
        logger.debug(LogTags.ERROR, "Already domain exception: ${e::class.simpleName}")
        e
    }
    is SerializationException -> {
        logger.error(LogTags.ERROR, "Serialization failure", e)
        ApiException.SerializationFailure("Failed to parse response: ${e.message}")
    }
    is IOException -> {
        logger.warning(LogTags.NETWORK, "Network connection failed")
        ApiException.Network("Network connection failed: ${e.message}")
    }
    is IllegalStateException -> {
        logger.error(LogTags.ERROR, "Invalid app state", e)
        ApiException.InvalidState("Invalid app state: ${e.message}")
    }
    is CancellationException -> {
        logger.debug(LogTags.ERROR, "Request cancelled")
        throw e
    }
    is HttpRequestTimeoutException -> {
        logger.warning(LogTags.NETWORK, "Request timeout")
        ApiException.Network("Request timeout: ${e.message}")
    }
    is RedirectResponseException -> handleRedirectException(e)
    is ClientRequestException -> handleClientException(e)
    is ServerResponseException -> handleServerException(e)
    else -> {
        logger.error(LogTags.ERROR, "Unknown exception type: ${e::class.simpleName}", e)
        handleUnknownException(e)
    }
}

private fun handleRedirectException(e: RedirectResponseException): ApiException {
    val statusCode = e.response.status.value
    logger.warning(LogTags.ERROR, "Redirect response: $statusCode")
    return when (statusCode) {
        401 -> ApiException.Unauthorized("Authentication required")
        403 -> ApiException.Forbidden("Access denied")
        else -> ApiException.Network("Redirect error: ${e.message}")
    }
}

private fun handleClientException(e: ClientRequestException): ApiException {
    val statusCode = e.response.status.value
    logger.warning(LogTags.ERROR, "Client error: $statusCode - ${e.message}")

    return when (statusCode) {
        400 -> ApiException.ClientError("Bad request: ${e.message}", statusCode)
        401 -> ApiException.Unauthorized("Authentication required: ${e.message}")
        403 -> ApiException.Forbidden("Access denied: ${e.message}")
        404 -> ApiException.NotFound("Resource not found: ${e.message}")
        409 -> ApiException.ClientError("Conflict: ${e.message}", statusCode)
        422 -> ApiException.ClientError("Validation error: ${e.message}", statusCode)
        in 400..499 -> ApiException.ClientError("Client error $statusCode: ${e.message}", statusCode)
        else -> ApiException.Unknown("Unexpected client error: ${e.message}")
    }
}

private fun handleServerException(e: ServerResponseException): ApiException {
    val statusCode = e.response.status.value
    logger.error(LogTags.ERROR, "Server error: $statusCode - ${e.message}")

    return when (statusCode) {
        500 -> ApiException.ServerError("Internal server error: ${e.message}", statusCode)
        502 -> ApiException.ServerError("Bad gateway: ${e.message}", statusCode)
        503 -> ApiException.ServerError("Service unavailable: ${e.message}", statusCode)
        504 -> ApiException.ServerError("Gateway timeout: ${e.message}", statusCode)
        in 500..599 -> ApiException.ServerError("Server error $statusCode: ${e.message}", statusCode)
        else -> ApiException.Unknown("Unexpected server error: ${e.message}")
    }
}

private fun handleUnknownException(e: Exception): ApiException {
    val message = e.message ?: "Unknown error occurred"
    logger.error(LogTags.ERROR, "Unhandled exception: $message", e)

    return when {
        message.contains("401", ignoreCase = true) -> ApiException.Unauthorized("Authentication required")
        message.contains("403", ignoreCase = true) -> ApiException.Forbidden("Access denied")
        message.contains("404", ignoreCase = true) -> ApiException.NotFound("Resource not found")
        message.contains("40", ignoreCase = true) -> ApiException.ClientError("Client error: $message", 400)
        message.contains("50", ignoreCase = true) -> ApiException.ServerError("Server error: $message", 500)
        message.contains("timeout", ignoreCase = true) -> ApiException.Network("Request timeout: $message")
        message.contains("network", ignoreCase = true) -> ApiException.Network("Network error: $message")
        message.contains("connect", ignoreCase = true) -> ApiException.Network("Connection failed: $message")
        else -> ApiException.Unknown("Unexpected error: $message")
    }
}