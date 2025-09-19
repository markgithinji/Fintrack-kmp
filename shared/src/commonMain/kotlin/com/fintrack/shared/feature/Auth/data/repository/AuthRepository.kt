package com.fintrack.shared.feature.Auth.data.repository

import com.fintrack.shared.feature.Auth.data.remote.AuthApi
import com.fintrack.shared.feature.Auth.domain.AuthResponse
import com.fintrack.shared.feature.Auth.data.model.LoginRequestDto
import com.fintrack.shared.feature.Auth.data.model.RegisterRequestDto
import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.Auth.data.model.toDomain

class AuthRepository {
    private val api = AuthApi()

    suspend fun login(email: String, password: String): Result<AuthResponse> =
        try {
            val authResponse = api.login(LoginRequestDto(email, password))
            Result.Success(authResponse.toDomain())
        } catch (e: Exception) {
            Result.Error(e)
        }

    suspend fun register(name: String, email: String, password: String): Result<AuthResponse> =
        try {
            val authResponse = api.register(RegisterRequestDto(name, email, password))
            Result.Success(authResponse.toDomain())
        } catch (e: Exception) {
            Result.Error(e)
        }

    suspend fun getUserById(userId: String, token: String): Result<AuthResponse> =
        try {
            val authResponse = api.getUserById(userId, token)
            Result.Success(authResponse.toDomain())
        } catch (e: Exception) {
            Result.Error(e)
        }
}