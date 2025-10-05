package com.fintrack.shared.feature.auth.data.repository

import com.fintrack.shared.feature.auth.data.remote.AuthApi
import com.fintrack.shared.feature.auth.domain.model.AuthResponse
import com.fintrack.shared.feature.auth.data.model.LoginRequestDto
import com.fintrack.shared.feature.auth.data.model.RegisterRequestDto
import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.auth.data.model.toDomain
import com.fintrack.shared.feature.auth.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val api: AuthApi = AuthApi()
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthResponse> =
        try {
            val authResponse = api.login(LoginRequestDto(email, password))
            Result.Success(authResponse.toDomain())
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun register(name: String, email: String, password: String): Result<AuthResponse> =
        try {
            val authResponse = api.register(RegisterRequestDto(name, email, password))
            Result.Success(authResponse.toDomain())
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun getUserById(userId: String, token: String): Result<AuthResponse> =
        try {
            val authResponse = api.getUserById(userId, token)
            Result.Success(authResponse.toDomain())
        } catch (e: Exception) {
            Result.Error(e)
        }
}