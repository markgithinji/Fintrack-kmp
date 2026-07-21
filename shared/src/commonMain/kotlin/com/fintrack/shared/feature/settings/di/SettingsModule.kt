package com.fintrack.shared.feature.settings.di

import com.fintrack.shared.feature.settings.data.local.createSettingsDataSource
import com.fintrack.shared.feature.settings.domain.util.createBiometricAuthenticator
import com.fintrack.shared.feature.settings.domain.util.createNotificationService
import com.fintrack.shared.feature.settings.ui.SettingsViewModel
import com.fintrack.shared.feature.settings.domain.usecase.ClearAllUserDataUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsModule = module {
    single { createSettingsDataSource() }
    single { createNotificationService(get()) }
    singleOf(::ClearAllUserDataUseCase)
    viewModelOf(::SettingsViewModel)
}
