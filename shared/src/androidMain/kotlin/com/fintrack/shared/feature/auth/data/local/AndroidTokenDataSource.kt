package com.fintrack.shared.feature.auth.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.fintrack.shared.feature.auth.domain.datasource.TokenDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import androidx.core.content.edit

class AndroidTokenDataSource(
    private val context: Context
) : TokenDataSource {

    private val _accessTokenFlow = MutableStateFlow<String?>(null)
    private val _refreshTokenFlow = MutableStateFlow<String?>(null)

    private val encryptedPrefs by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        EncryptedSharedPreferences.create(
            "encrypted_auth_data",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    init {
        loadTokensFromEncryptedPrefs()
    }

    override val accessToken: Flow<String?> = _accessTokenFlow
    override val refreshToken: Flow<String?> = _refreshTokenFlow

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        encryptedPrefs.edit(commit = true) { 
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
        }
        _accessTokenFlow.value = accessToken
        _refreshTokenFlow.value = refreshToken
    }

    override suspend fun clearTokens() {
        encryptedPrefs.edit(commit = true) { 
            remove("access_token") 
            remove("refresh_token")
        }
        _accessTokenFlow.value = null
        _refreshTokenFlow.value = null
    }

    private fun loadTokensFromEncryptedPrefs() {
        val accessToken = encryptedPrefs.getString("access_token", null)
        val refreshToken = encryptedPrefs.getString("refresh_token", null)
        _accessTokenFlow.update { accessToken }
        _refreshTokenFlow.update { refreshToken }
    }
}