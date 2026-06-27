package com.fintrack.shared.feature.user.domain.usecase

import com.fintrack.shared.feature.user.domain.model.User
import com.fintrack.shared.feature.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class GetUserProfileUseCase(private val repository: UserRepository) {
    operator fun invoke(): Flow<User?> = repository.getUserProfile()
    suspend fun refresh() = repository.refreshProfile()
}
