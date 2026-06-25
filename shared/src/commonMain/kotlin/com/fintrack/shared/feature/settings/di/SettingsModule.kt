package com.fintrack.shared.feature.settings.di

import com.fintrack.shared.feature.settings.data.local.createSettingsDataSource
import com.fintrack.shared.feature.settings.ui.SettingsViewModel
import org.koin.dsl.module

val settingsModule = module {
    single { createSettingsDataSource() }
    factory { SettingsViewModel(get()) }
}