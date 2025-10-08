package com.fintrack.shared.feature.core

import com.fintrack.shared.feature.auth.data.local.createTokenDataStore
import com.fintrack.shared.feature.auth.domain.repository.TokenDataSource
import com.fintrack.shared.feature.auth.data.repository.TokenDataSourceImpl
import io.ktor.client.HttpClient
import org.koin.dsl.module

val networkModule = module {
    single { createTokenDataStore() }
    single<TokenDataSource> { TokenDataSourceImpl(get()) }
    single { KMPLogger() }
    single<HttpClient> { ApiClient(get(), get()).httpClient }
}