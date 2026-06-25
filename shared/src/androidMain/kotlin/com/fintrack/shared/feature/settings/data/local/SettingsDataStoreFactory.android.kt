package com.fintrack.shared.feature.settings.data.local

import android.content.Context
import com.fintrack.shared.feature.settings.domain.datasource.SettingsDataSource

private lateinit var appContext: Context

fun initSettingsDataStore(context: Context) {
    appContext = context.applicationContext
}

actual fun createSettingsDataSource(): SettingsDataSource {
    return AndroidSettingsDataSource(appContext)
}