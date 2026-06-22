package com.fintrack.shared.feature.core.data.domain

sealed class ApiException(open val details: String) : Exception(details) {
    data class Network(override val details: String) : ApiException(details)
    data class SerializationFailure(override val details: String) : ApiException(details)
    data class InvalidState(override val details: String) : ApiException(details)

    // HTTP status code exceptions
    data class Unauthorized(override val details: String) : ApiException(details)
    data class Forbidden(override val details: String) : ApiException(details)
    data class NotFound(override val details: String) : ApiException(details)
    data class ClientError(override val details: String, val statusCode: Int) : ApiException(details)
    data class ServerError(override val details: String, val statusCode: Int) : ApiException(details)

    // Domain specific exceptions
    data class Validation(override val details: String) : ApiException(details)
    data class Auth(override val details: String, val type: AuthErrorType) : ApiException(details)

    data class Unknown(override val details: String) : ApiException(details)
}

enum class AuthErrorType {
    INVALID_CREDENTIALS,
    USER_ALREADY_EXISTS,
    WEAK_PASSWORD,
    SESSION_EXPIRED,
    UNAUTHORIZED,
    UNKNOWN
}

fun ApiException.getUserFriendlyMessage(): String = when (this) {
    is ApiException.Auth -> when (type) {
        AuthErrorType.INVALID_CREDENTIALS -> "The email or password you entered is incorrect."
        AuthErrorType.USER_ALREADY_EXISTS -> "An account with this email already exists."
        AuthErrorType.WEAK_PASSWORD -> "Your password is too weak. Try a stronger one."
        AuthErrorType.SESSION_EXPIRED -> "Your session has expired. Please log in again."
        AuthErrorType.UNAUTHORIZED -> "You are not authorized to perform this action."
        AuthErrorType.UNKNOWN -> "An authentication error occurred. Please try again."
    }
    is ApiException.Network -> "Connection failed. Please check your internet and try again."
    is ApiException.Validation -> details
    is ApiException.NotFound -> "The requested information could not be found."
    is ApiException.ServerError -> "Something went wrong on our end. We're working on it!"
    is ApiException.Unauthorized -> "Authentication required. Please log in again."
    is ApiException.Forbidden -> "You don't have permission to do this."
    is ApiException.ClientError -> details
    is ApiException.SerializationFailure -> "Data format error. Please try again later."
    is ApiException.Unknown -> "An unexpected error occurred: $details"
    else -> "An unexpected error occurred. Please try again."
}
