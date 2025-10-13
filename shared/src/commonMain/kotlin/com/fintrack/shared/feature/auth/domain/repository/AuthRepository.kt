package com.fintrack.shared.feature.auth.domain.repository

import com.fintrack.shared.feature.auth.domain.model.AuthResponse
import com.fintrack.shared.feature.core.util.Result

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthResponse>
    suspend fun register(name: String, email: String, password: String): Result<AuthResponse>
    suspend fun getUserById(userId: String, token: String): Result<AuthResponse>
}