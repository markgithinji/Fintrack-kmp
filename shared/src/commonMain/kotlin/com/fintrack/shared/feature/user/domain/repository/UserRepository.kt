package com.fintrack.shared.feature.user.domain.repository

import com.fintrack.shared.feature.user.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    fun getUserProfile(): StateFlow<User?>
    suspend fun refreshProfile()
    suspend fun updateProfile(name: String, email: String)
    suspend fun updateTrackedCategories(categories: List<String>)
    suspend fun deleteAccount(): com.fintrack.shared.feature.core.util.Result<Unit>
}
