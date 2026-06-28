package com.fintrack.shared.feature.user.domain.usecase

import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.user.domain.repository.UserRepository

class UpdateProfileUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(name: String, email: String): Result<Unit> {
        return try {
            repository.updateProfile(name, email)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
