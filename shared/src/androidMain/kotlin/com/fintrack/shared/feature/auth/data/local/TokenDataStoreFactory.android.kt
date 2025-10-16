package com.fintrack.shared.feature.auth.data.local

import android.content.Context
import com.fintrack.shared.feature.auth.domain.datasource.TokenDataSource

private lateinit var appContext: Context

fun initTokenDataStore(context: Context) {
    appContext = context.applicationContext
}

actual fun createTokenDataSource(): TokenDataSource {
    return AndroidTokenDataSource(appContext)
}