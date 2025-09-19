package com.fintrack.shared.feature.Auth.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    val name: String,
    val email: String,
    val password: String
)
