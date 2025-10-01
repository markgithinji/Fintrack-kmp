package com.fintrack.shared.feature.auth.data.local

import androidx.datastore.preferences.core.stringPreferencesKey

object TokenPreferencesKeys {
    val TOKEN = stringPreferencesKey("auth_token")
}
