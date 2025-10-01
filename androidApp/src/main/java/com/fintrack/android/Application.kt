package com.fintrack.android

import android.app.Application
import com.fintrack.shared.feature.auth.data.local.initTokenDataStore

class FintrackApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initTokenDataStore(this)
    }
}
