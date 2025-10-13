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

    data class Unknown(override val details: String) : ApiException(details)
}