package com.fintrack.shared.feature.auth.data.model

import com.fintrack.shared.feature.auth.domain.model.AuthResponse

fun AuthResponseDto.toDomain(): AuthResponse {
    return AuthResponse(
        token = token
    )
}