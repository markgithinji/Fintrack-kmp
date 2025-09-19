package com.fintrack.shared.feature.auth.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponseDto(
    val token: String
)