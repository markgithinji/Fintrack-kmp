package com.fintrack.shared.feature.auth.domain.usecase

import com.fintrack.shared.feature.auth.domain.repository.AuthRepository
import com.fintrack.shared.feature.core.util.Result

class ChangePasswordUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(currentPassword: String, newPassword: String): Result<Unit> {
        return repository.changePassword(currentPassword, newPassword)
    }
}
