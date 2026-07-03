package com.fintrack.shared.feature.core.util

import com.fintrack.shared.feature.core.data.domain.ApiException
import com.fintrack.shared.feature.core.data.domain.AuthErrorType
import com.fintrack.shared.feature.core.data.remote.model.ErrorResponse
import com.fintrack.shared.feature.core.logger.KMPLogger
import com.fintrack.shared.feature.core.logger.LogTags
import io.ktor.client.call.body
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
        Result.Success(result)
    } catch (e: Exception) {
        val domainException = convertToDomainException(e)
        logger.error(LogTags.API, "API call failed: ${domainException.details}", e)
        Result.Error(domainException)
    }
}

private suspend fun convertToDomainException(e: Exception): ApiException = when (e) {
    is ApiException -> {
        e
    }

    is RedirectResponseException -> handleRedirectException(e)
    is ClientRequestException -> handleClientException(e)
    is ServerResponseException -> handleServerException(e)

    is SerializationException -> {
        logger.error(LogTags.ERROR, "Serialization failure", e)
        ApiException.SerializationFailure("Failed to parse response: ${e.message}")
    }

    is IOException -> {
        val cleanMessage = e.message.cleanKtorMessage()
        logger.warning(LogTags.NETWORK, "Network connection failed: $cleanMessage")
        ApiException.Network(cleanMessage.ifEmpty { "Network connection failed" })
    }

    is HttpRequestTimeoutException -> {
        val cleanMessage = e.message.cleanKtorMessage()
        logger.warning(LogTags.NETWORK, "Request timeout: $cleanMessage")
        ApiException.Network(cleanMessage.ifEmpty { "Request timeout" })
    }

    is IllegalStateException -> {
        logger.error(LogTags.ERROR, "Invalid app state", e)
        ApiException.InvalidState("Invalid app state: ${e.message}")
    }

    is CancellationException -> {
        throw e
    }

    else -> {
        logger.error(LogTags.ERROR, "Unknown exception type: ${e::class.simpleName}", e)
        handleUnknownException(e)
    }
}

private fun handleRedirectException(e: RedirectResponseException): ApiException {
    val statusCode = e.response.status.value
    val cleanMessage = e.message.cleanKtorMessage()
    logger.warning(LogTags.ERROR, "Redirect response: $statusCode - $cleanMessage")
    return when (statusCode) {
        401 -> ApiException.Unauthorized("Authentication required")
        403 -> ApiException.Forbidden("Access denied")
        else -> ApiException.Network("Redirect error: $cleanMessage")
    }
}

private suspend fun handleClientException(e: ClientRequestException): ApiException {
    val statusCode = e.response.status.value
    val errorResponse = try {
        e.response.body<ErrorResponse>()
    } catch (parseException: Exception) {
        null
    }

    val message = errorResponse?.message ?: "Client error occurred"
    val errorCode = errorResponse?.errorCode

    if (errorResponse == null) {
        val rawBody = try { e.response.body<String>() } catch (ex: Exception) { "unavailable" }
        logger.warning(LogTags.ERROR, "Client error $statusCode: Failed to parse ErrorResponse. Raw body: $rawBody")
    } else {
        logger.warning(LogTags.ERROR, "Client error: $statusCode - $message (Code: $errorCode)")
    }

    return when (statusCode) {
        400 -> {
            if (errorCode != null) {
                mapToAuthException(message, errorCode) ?: ApiException.Validation(message)
            } else {
                ApiException.Validation(message)
            }
        }
        401 -> {
            if (errorCode != null) {
                mapToAuthException(message, errorCode) ?: ApiException.Auth(message, AuthErrorType.UNAUTHORIZED)
            } else {
                // If no specific error code, default to a generic auth error or credentials error
                ApiException.Auth(message, AuthErrorType.INVALID_CREDENTIALS)
            }
        }
        403 -> ApiException.Forbidden("Access denied: $message")
        404 -> ApiException.NotFound("Resource not found: $message")
        409 -> {
             if (errorCode != null) {
                mapToAuthException(message, errorCode) ?: ApiException.ClientError(message, statusCode)
            } else {
                ApiException.ClientError(message, statusCode)
            }
        }
        422 -> ApiException.Validation(message)
        in 400..499 -> ApiException.ClientError(message, statusCode)
        else -> ApiException.Unknown("Unexpected client error: $message")
    }
}

private fun mapToAuthException(message: String, errorCode: String): ApiException.Auth? {
    val type = when (errorCode) {
        "INVALID_CREDENTIALS" -> AuthErrorType.INVALID_CREDENTIALS
        "USER_NOT_FOUND" -> AuthErrorType.USER_NOT_FOUND
        "INVALID_PASSWORD" -> AuthErrorType.INVALID_PASSWORD
        "USER_ALREADY_EXISTS" -> AuthErrorType.USER_ALREADY_EXISTS
        "WEAK_PASSWORD" -> AuthErrorType.WEAK_PASSWORD
        "SESSION_EXPIRED" -> AuthErrorType.SESSION_EXPIRED
        "UNAUTHORIZED" -> AuthErrorType.UNAUTHORIZED
        else -> return null
    }
    return ApiException.Auth(message, type)
}

private fun handleServerException(e: ServerResponseException): ApiException {
    val statusCode = e.response.status.value
    val cleanMessage = e.message.cleanKtorMessage()
    logger.error(LogTags.ERROR, "Server error: $statusCode - $cleanMessage")

    return when (statusCode) {
        500 -> ApiException.ServerError("Internal server error", statusCode)
        503 -> ApiException.ServerError("Service unavailable", statusCode)
        else -> ApiException.ServerError("Server error $statusCode: $cleanMessage", statusCode)
    }
}

private fun handleUnknownException(e: Exception): ApiException {
    val message = e.message.cleanKtorMessage()
    return ApiException.Unknown(message.ifEmpty { "Unknown error occurred" })
}

private fun String?.cleanKtorMessage(): String {
    if (this == null) return ""
    // Remove Ktor-style metadata like [url=..., connect_timeout=...]
    return this.substringBefore("[").trim()
}
