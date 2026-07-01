package com.fintrack.shared.feature.user.domain.usecase

import com.fintrack.shared.feature.auth.domain.repository.AuthRepository
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.user.domain.repository.UserRepository

class DeleteAccountUseCase(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        val deleteResult = userRepository.deleteAccount()
        if (deleteResult is Result.Error) return deleteResult

        // After deleting on backend, we must clear local session
        return authRepository.logout()
    }
}
