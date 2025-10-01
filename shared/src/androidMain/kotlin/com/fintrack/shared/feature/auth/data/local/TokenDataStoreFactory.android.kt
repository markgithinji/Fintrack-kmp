package com.fintrack.shared.feature.auth.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private const val TOKEN_PREFS_FILENAME = "auth_prefs.preferences_pb"

val Context.authPreferencesDataStore by preferencesDataStore(TOKEN_PREFS_FILENAME)

private lateinit var appContext: Context

fun initTokenDataStore(context: Context) {
    appContext = context.applicationContext
}

actual fun createTokenDataStore(): DataStore<Preferences> {
    return appContext.authPreferencesDataStore
}
