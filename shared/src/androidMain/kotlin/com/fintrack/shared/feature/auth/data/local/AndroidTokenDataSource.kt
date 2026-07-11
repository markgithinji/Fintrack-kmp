package com.fintrack.shared.feature.auth.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.fintrack.shared.feature.auth.domain.datasource.TokenDataSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class AndroidTokenDataSource(
    private val context: Context
) : TokenDataSource {

    private val encryptedPrefs by lazy { createEncryptedPrefs() }

    private fun createEncryptedPrefs(): SharedPreferences {
        val fileName = "encrypted_auth_data"
        return try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

            EncryptedSharedPreferences.create(
                fileName,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // If creation fails (due to key corruption or other issues), clear the corrupted preferences and retry
            context.deleteSharedPreferences(fileName)

            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                fileName,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    override val accessToken: Flow<String?> = createTokenFlow("access_token")
    override val refreshToken: Flow<String?> = createTokenFlow("refresh_token")

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        encryptedPrefs.edit(commit = true) {
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
        }
    }

    override suspend fun clearTokens() {
        encryptedPrefs.edit(commit = true) {
            remove("access_token")
            remove("refresh_token")
        }
    }

    private fun createTokenFlow(key: String): Flow<String?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, changedKey ->
            if (key == changedKey) {
                trySend(prefs.getString(key, null))
            }
        }

        // Emit initial value
        send(encryptedPrefs.getString(key, null))

        encryptedPrefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            encryptedPrefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.distinctUntilChanged()
}
