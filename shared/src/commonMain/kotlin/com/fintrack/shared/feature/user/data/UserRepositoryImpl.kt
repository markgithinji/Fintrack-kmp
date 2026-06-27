package com.fintrack.shared.feature.user.data

import com.fintrack.shared.feature.user.domain.model.User
import com.fintrack.shared.feature.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserRepositoryImpl(
    private val api: UserApi
) : UserRepository {
    private val _userProfile = MutableStateFlow<User?>(null)

    override fun getUserProfile(): Flow<User?> = _userProfile.asStateFlow()

    override suspend fun refreshProfile() {
        try {
            val dto = api.getUserProfile()
            _userProfile.value = User(name = dto.name, email = dto.email)
        } catch (e: Exception) {
            // Handle error or rethrow
        }
    }
}
