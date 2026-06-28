package com.fintrack.shared.feature.auth.di

import com.fintrack.shared.feature.auth.data.local.createTokenDataSource
import com.fintrack.shared.feature.auth.data.remote.AuthApi
import com.fintrack.shared.feature.auth.data.repository.AuthRepositoryImpl
import com.fintrack.shared.feature.auth.domain.datasource.TokenDataSource
import com.fintrack.shared.feature.auth.domain.repository.AuthRepository
import com.fintrack.shared.feature.auth.domain.usecase.ChangePasswordUseCase
import com.fintrack.shared.feature.auth.domain.usecase.ChangePasswordValidationUseCase
import com.fintrack.shared.feature.auth.domain.usecase.LoginValidationUseCase
import com.fintrack.shared.feature.auth.domain.usecase.RegisterValidationUseCase
import com.fintrack.shared.feature.auth.ui.AuthViewModel
import com.fintrack.shared.feature.core.data.remote.ApiConfig
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    single { AuthApi(get(), ApiConfig.BASE_URL) }
    single<TokenDataSource> { createTokenDataSource() }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single { RegisterValidationUseCase() }
    single { LoginValidationUseCase() }
    single { ChangePasswordUseCase(get()) }
    single { ChangePasswordValidationUseCase() }
    viewModel {
        AuthViewModel(
            repository = get(),
            tokenDataSource = get(),
            registerValidationUseCase = get(),
            loginValidationUseCase = get(),
            logger = get()
        )
    }
}