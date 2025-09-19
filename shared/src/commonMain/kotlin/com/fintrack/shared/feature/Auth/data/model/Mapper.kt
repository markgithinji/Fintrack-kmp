package com.fintrack.shared.feature.Auth.data.model

import com.fintrack.shared.feature.Auth.domain.AuthResponse

fun AuthResponseDto.toDomain(): AuthResponse {
    return AuthResponse(
        token = token
    )
}