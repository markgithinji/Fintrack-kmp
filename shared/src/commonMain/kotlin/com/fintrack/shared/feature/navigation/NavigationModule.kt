package com.fintrack.shared.feature.navigation

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
