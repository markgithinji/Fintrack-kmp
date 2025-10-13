package com.fintrack.shared.feature.core.di

import com.fintrack.shared.feature.auth.data.local.createTokenDataStore
import com.fintrack.shared.feature.core.data.remote.ApiClient
import com.fintrack.shared.feature.core.logger.KMPLogger
import io.ktor.client.HttpClient
import org.koin.dsl.module

val coreModule = module {
    single { createTokenDataStore() }
    single { KMPLogger() }
    single<HttpClient> { ApiClient(get(), get()).httpClient }
}