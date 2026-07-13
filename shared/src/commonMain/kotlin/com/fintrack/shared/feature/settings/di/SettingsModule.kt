package com.fintrack.shared.feature.settings.di

import com.fintrack.shared.feature.settings.data.local.createSettingsDataSource
import com.fintrack.shared.feature.settings.domain.util.createBiometricAuthenticator
import com.fintrack.shared.feature.settings.domain.util.createNotificationService
import com.fintrack.shared.feature.settings.ui.SettingsViewModel
import com.fintrack.shared.feature.settings.domain.usecase.ClearAllUserDataUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

val settingsModule = module {
    single { createSettingsDataSource() }
    single { createBiometricAuthenticator() }
    single { createNotificationService(get()) }
    singleOf(::ClearAllUserDataUseCase)
    viewModel {
        SettingsViewModel(
            settingsDataSource = get(),
            exportTransactionsUseCase = get(),
            notificationService = get(),
            authRepository = get(),
            validationUseCase = get(),
            deleteAccountUseCase = get(),
            userRepository = get(),
            localCategoryDataSource = get(),
            syncCategoriesUseCase = get(),
            accountRepository = get(),
            budgetRepository = get()
        )
    }
}
