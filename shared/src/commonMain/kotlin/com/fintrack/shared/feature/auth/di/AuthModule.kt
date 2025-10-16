package com.fintrack.shared.feature.auth.di

import com.fintrack.shared.feature.auth.data.local.createTokenDataSource
import com.fintrack.shared.feature.auth.data.remote.AuthApi
import com.fintrack.shared.feature.auth.data.repository.AuthRepositoryImpl
import com.fintrack.shared.feature.auth.domain.repository.AuthRepository
import com.fintrack.shared.feature.auth.domain.repository.TokenDataSource
import com.fintrack.shared.feature.auth.ui.AuthViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    single { AuthApi(get(), getProperty("baseUrl")) }
    single<TokenDataSource> { createTokenDataSource() }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    viewModel { AuthViewModel(get(), get()) }
}