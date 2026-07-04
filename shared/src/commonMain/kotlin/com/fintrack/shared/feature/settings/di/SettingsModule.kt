package com.fintrack.shared.feature.settings.di

import com.fintrack.shared.feature.settings.data.local.createSettingsDataSource
import com.fintrack.shared.feature.settings.domain.util.createBiometricAuthenticator
import com.fintrack.shared.feature.settings.domain.util.createNotificationService
import com.fintrack.shared.feature.settings.ui.SettingsViewModel
import com.fintrack.shared.feature.core.domain.usecase.ClearAllUserDataUseCase
import org.koin.dsl.module

val settingsModule = module {
    single { createSettingsDataSource() }
    single { createBiometricAuthenticator() }
    single { createNotificationService(get()) }
    single { ClearAllUserDataUseCase(get(), get(), get()) }
    factory { SettingsViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}
