package com.fintrack.shared.feature.transaction.data

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequestDto(
    val name: String,
    val email: String,
    val password: String
)

@Serializable
data class AuthResponseDto(
    val token: String
)

data class AuthResponse(
    val token: String
)

fun AuthResponseDto.toDomain(): AuthResponse {
    return AuthResponse(
       token = token
    )
}