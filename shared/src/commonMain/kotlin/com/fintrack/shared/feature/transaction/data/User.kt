package com.fintrack.shared.feature.transaction.data

import kotlinx.serialization.Serializable

data class User(
    val name: String,
    val email: String,
)

@Serializable
data class UserDto(
    val name: String,
    val email: String
)


fun UserDto.toDomain(): User {
    return User(
        name = name,
        email = email,
    )
}