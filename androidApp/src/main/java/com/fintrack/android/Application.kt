package com.fintrack.android

import android.app.Application
import com.fintrack.shared.feature.auth.data.local.initTokenDataStore
import com.fintrack.shared.feature.core.Environment
import com.fintrack.shared.feature.core.Koin

class FintrackApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initTokenDataStore(this)

        val environment = if (BuildConfig.DEBUG) {
            Environment.DEVELOPMENT
        } else {
            Environment.PRODUCTION
        }

        Koin.init(
            environment = environment,
            enableNetworkLogs = BuildConfig.DEBUG
        )
    }
}
