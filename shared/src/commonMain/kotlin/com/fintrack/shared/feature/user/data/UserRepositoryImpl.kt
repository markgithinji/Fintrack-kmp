package com.fintrack.shared.feature.user.data

import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.core.util.safeApiCall
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
        when (val result = safeApiCall { api.getUserProfile() }) {
            is Result.Success -> {
                val dto = result.data
                _userProfile.value = User(name = dto.name, email = dto.email)
            }
            is Result.Error -> throw result.exception
            is Result.Loading -> {}
        }
    }

    override suspend fun updateProfile(name: String, email: String) {
        when (val result = safeApiCall { api.updateProfile(name, email) }) {
            is Result.Success -> {
                val dto = result.data
                _userProfile.value = User(name = dto.name, email = dto.email)
            }
            is Result.Error -> throw result.exception
            is Result.Loading -> {}
        }
    }
}
