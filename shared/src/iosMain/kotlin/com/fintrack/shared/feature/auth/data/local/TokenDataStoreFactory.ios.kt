package com.fintrack.shared.feature.auth.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual fun createTokenDataStore(): DataStore<Preferences> {
    val docDirUrl = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )
    val path = requireNotNull(docDirUrl).path + "/auth_prefs.preferences_pb"
    val okioPath = path.toPath()

    val storage = OkioStorage(
        fileSystem = FileSystem.SYSTEM,
        producePath = { okioPath },
        serializer = PreferencesSerializer
    )

    return DataStoreFactory.create(
        storage = storage,
        scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    )
}
