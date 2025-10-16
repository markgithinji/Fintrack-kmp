package com.fintrack.shared.feature.auth.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.fintrack.shared.feature.auth.domain.repository.TokenDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import androidx.core.content.edit

class AndroidTokenDataSource(
    private val context: Context
) : TokenDataSource {

    private val _tokenFlow = MutableStateFlow<String?>(null)

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
        loadTokenFromEncryptedPrefs()
    }

    override val token: Flow<String?> = _tokenFlow

    override suspend fun saveToken(token: String) {
        encryptedPrefs.edit { putString("auth_token", token) }
        _tokenFlow.update { token }
    }

    override suspend fun clearToken() {
        encryptedPrefs.edit { remove("auth_token") }
        _tokenFlow.update { null }
    }

    private fun loadTokenFromEncryptedPrefs() {
        val token = encryptedPrefs.getString("auth_token", null)
        _tokenFlow.update { token }
    }
}