package com.fintrack.shared.feature.transaction.data

data class User(
    val id: String,
    val name: String,
    val email: String,
    val token: String
)

fun UserDto.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        token = token
    )
}

fun User.toDto(): UserDto {
    return UserDto(
        id = id,
        name = name,
        email = email,
        token = token
    )
}
