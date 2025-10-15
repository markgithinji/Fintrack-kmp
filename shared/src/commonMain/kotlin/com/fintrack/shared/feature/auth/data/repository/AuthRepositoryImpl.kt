package com.fintrack.shared.feature.auth.data.repository

import com.fintrack.shared.feature.auth.data.model.LoginRequest
import com.fintrack.shared.feature.auth.data.model.RegisterRequest
import com.fintrack.shared.feature.auth.data.model.toDomain
import com.fintrack.shared.feature.auth.data.remote.AuthApi
import com.fintrack.shared.feature.auth.domain.model.AuthResponse
import com.fintrack.shared.feature.auth.domain.repository.AuthRepository
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.core.util.safeApiCall

class AuthRepositoryImpl(
    private val api: AuthApi
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthResponse> =
        safeApiCall {
            val authResponse = api.login(LoginRequest(email, password))
            authResponse.toDomain()
        }

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<AuthResponse> =
        safeApiCall {
            val authResponse = api.register(RegisterRequest(name, email, password))
            authResponse.toDomain()
        }

    override suspend fun getUserById(userId: String, token: String): Result<AuthResponse> =
        safeApiCall {
            val authResponse = api.getUserById(userId, token)
            authResponse.toDomain()
        }

    override suspend fun validateToken(token: String): Result<Boolean> =
        safeApiCall {
            val response = api.validateToken()
            response.isValid
        }
}