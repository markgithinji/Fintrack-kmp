package com.fintrack.shared.feature.transaction.data

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

fun LoginRequest.toDto() = LoginRequestDto(email, password)

fun RegisterRequest.toDto() = RegisterRequestDto(name, email, password)
