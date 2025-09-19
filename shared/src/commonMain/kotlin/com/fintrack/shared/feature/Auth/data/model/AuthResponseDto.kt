package com.fintrack.shared.feature.Auth.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponseDto(
    val token: String
)