package com.fintrack.shared.feature.settings.di

import com.fintrack.shared.feature.settings.data.local.createSettingsDataSource
import com.fintrack.shared.feature.settings.domain.util.createBiometricAuthenticator
import com.fintrack.shared.feature.settings.ui.SecurityViewModel
import com.fintrack.shared.feature.settings.ui.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    single { createSettingsDataSource() }
    single { createBiometricAuthenticator() }
    factory { SettingsViewModel(get()) }
    viewModel { SecurityViewModel(get(), get(), get(), get()) }
}