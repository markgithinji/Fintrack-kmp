package com.fintrack.shared.feature.auth.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

expect fun createTokenDataStore(): DataStore<Preferences>