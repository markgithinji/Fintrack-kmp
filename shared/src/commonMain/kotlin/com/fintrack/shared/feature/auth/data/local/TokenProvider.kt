package com.fintrack.shared.feature.auth.data.local

import com.fintrack.shared.feature.auth.data.repository.TokenRepository
import kotlinx.coroutines.flow.firstOrNull

interface TokenProvider {
    suspend fun getToken(): String?
}

class TokenProviderImpl(
    private val tokenRepository: TokenRepository
) : TokenProvider {
    override suspend fun getToken(): String? {
        return tokenRepository.token.firstOrNull()
    }
}
