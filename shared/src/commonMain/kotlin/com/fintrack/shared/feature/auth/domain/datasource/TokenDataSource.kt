package com.fintrack.shared.feature.auth.domain.datasource

import kotlinx.coroutines.flow.Flow

interface TokenDataSource {
    val token: Flow<String?>
    suspend fun saveToken(token: String)
    suspend fun clearToken()
}