package com.fintrack.shared.feature.user.domain.repository

import com.fintrack.shared.feature.user.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserProfile(): Flow<User?>
    suspend fun refreshProfile()
}
