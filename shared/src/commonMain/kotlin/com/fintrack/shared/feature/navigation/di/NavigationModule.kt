package com.fintrack.shared.feature.navigation.di

import com.fintrack.shared.feature.navigation.ui.MainViewModel
import org.koin.dsl.module

val navigationModule = module {
    single {
        MainViewModel(
            settingsDataSource = get(),
            tokenDataSource = get(),
            userRepository = get(),
            getAccountsUseCase = get(),
            checkBudgetThresholdsUseCase = get(),
            syncRecurringBillsUseCase = get()
        )
    }
}
