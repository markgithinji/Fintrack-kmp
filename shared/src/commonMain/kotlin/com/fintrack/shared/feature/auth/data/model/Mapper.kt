package com.fintrack.shared.feature.auth.data.model

import com.fintrack.shared.feature.auth.domain.AuthResponse

fun AuthResponseDto.toDomain(): AuthResponse {
    return AuthResponse(
        token = token
    )
}