package com.fintrack.shared.feature.core

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
    return try {
        Result.Success(apiCall())
    } catch (e: Exception) {
        Result.Error(convertToDomainException(e))
    }
}

private fun convertToDomainException(e: Exception): ApiException = when (e) {
    is ApiException -> e
    is SerializationException -> ApiException.SerializationFailure("Failed to parse response: ${e.message}")
    is IOException -> ApiException.Network("Network connection failed: ${e.message}")
    is IllegalStateException -> ApiException.InvalidState("Invalid app state: ${e.message}")
    is kotlinx.coroutines.CancellationException -> throw e // Re-throw cancellation exceptions

    // Handle Ktor HTTP response exceptions
    is HttpRequestTimeoutException -> ApiException.Network("Request timeout: ${e.message}")
    is RedirectResponseException -> handleRedirectException(e)
    is ClientRequestException -> handleClientException(e)
    is ServerResponseException -> handleServerException(e)

    else -> handleUnknownException(e)
}

private fun handleRedirectException(e: RedirectResponseException): ApiException {
    return when (e.response.status.value) {
        401 -> ApiException.Unauthorized("Authentication required")
        403 -> ApiException.Forbidden("Access denied")
        else -> ApiException.Network("Redirect error: ${e.message}")
    }
}

private fun handleClientException(e: ClientRequestException): ApiException {
    val statusCode = e.response.status.value
    val message = e.message

    return when (statusCode) {
        400 -> ApiException.ClientError("Bad request: $message", statusCode)
        401 -> ApiException.Unauthorized("Authentication required: $message")
        403 -> ApiException.Forbidden("Access denied: $message")
        404 -> ApiException.NotFound("Resource not found: $message")
        409 -> ApiException.ClientError("Conflict: $message", statusCode)
        422 -> ApiException.ClientError("Validation error: $message", statusCode)
        in 400..499 -> ApiException.ClientError("Client error $statusCode: $message", statusCode)
        else -> ApiException.Unknown("Unexpected client error: $message")
    }
}

private fun handleServerException(e: ServerResponseException): ApiException {
    val statusCode = e.response.status.value
    val message = e.message

    return when (statusCode) {
        500 -> ApiException.ServerError("Internal server error: $message", statusCode)
        502 -> ApiException.ServerError("Bad gateway: $message", statusCode)
        503 -> ApiException.ServerError("Service unavailable: $message", statusCode)
        504 -> ApiException.ServerError("Gateway timeout: $message", statusCode)
        in 500..599 -> ApiException.ServerError("Server error $statusCode: $message", statusCode)
        else -> ApiException.Unknown("Unexpected server error: $message")
    }
}

private fun handleUnknownException(e: Exception): ApiException {
    val message = e.message ?: "Unknown error occurred"

    // Fallback to message parsing for any unhandled exceptions
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