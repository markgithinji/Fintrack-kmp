package com.fintrack.shared.feature.account.di

import com.fintrack.shared.feature.account.data.remote.AccountsApi
import com.fintrack.shared.feature.account.data.repository.AccountRepositoryImpl
import com.fintrack.shared.feature.account.domain.repository.AccountRepository
import com.fintrack.shared.feature.account.domain.usecase.GetAccountsUseCase
import com.fintrack.shared.feature.account.ui.AccountsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val accountModule = module {
    single { AccountsApi(client = get()) }
    single<AccountRepository> { AccountRepositoryImpl(accountsApi = get()) }
    factory { GetAccountsUseCase(accountRepository = get()) }
    viewModel {
        AccountsViewModel(
            repo = get(),
            getAccountsUseCase = get(),
            clearAllUserDataUseCase = get()
        )
    }
}