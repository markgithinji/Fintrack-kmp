package com.fintrack.shared.feature.auth.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.fintrack.shared.feature.auth.data.local.TokenPreferencesKeys
import com.fintrack.shared.feature.auth.domain.repository.TokenDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class TokenDataSourceImpl(
    private val dataStore: DataStore<Preferences>
) : TokenDataSource {

    override val token: Flow<String?> = dataStore.data
        .map { prefs ->
            prefs[TokenPreferencesKeys.TOKEN]
        }.distinctUntilChanged()

    override suspend fun saveToken(token: String) {
        dataStore.edit { prefs ->
            prefs[TokenPreferencesKeys.TOKEN] = token
        }
    }

    override suspend fun clearToken() {
        dataStore.edit { prefs ->
            prefs.remove(TokenPreferencesKeys.TOKEN)
        }
    }
}