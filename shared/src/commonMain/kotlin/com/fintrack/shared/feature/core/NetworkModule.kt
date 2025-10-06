package com.fintrack.shared.feature.core

import com.fintrack.shared.feature.auth.data.local.TokenProvider
import com.fintrack.shared.feature.auth.data.local.TokenProviderImpl
import com.fintrack.shared.feature.auth.data.local.createTokenDataStore
import com.fintrack.shared.feature.auth.data.repository.TokenRepository
import org.koin.dsl.module

val networkModule = module {
    single { createTokenDataStore() }

    single<TokenRepository> { TokenRepository(get()) }
    single<TokenProvider> { TokenProviderImpl(get()) }

    single { ApiClient.authenticatedClient }
    single { ApiClient.simpleClient }
}