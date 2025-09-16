package com.fintrack.shared.feature.transaction.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AuthRepository {
    private val api = AuthApi()

    suspend fun login(email: String, password: String): Result<User> =
        try {
            val userDto = api.login(LoginRequestDto(email, password))
            Result.Success(userDto.toDomain())
        } catch (e: Exception) {
            Result.Error(e)
        }

    suspend fun register(name: String, email: String, password: String): Result<User> =
        try {
            val userDto = api.register(RegisterRequestDto(name, email, password))
            Result.Success(userDto.toDomain())
        } catch (e: Exception) {
            Result.Error(e)
        }

    suspend fun getUserById(userId: String, token: String): Result<User> =
        try {
            val userDto = api.getUserById(userId, token)
            Result.Success(userDto.toDomain())
        } catch (e: Exception) {
            Result.Error(e)
        }
}
