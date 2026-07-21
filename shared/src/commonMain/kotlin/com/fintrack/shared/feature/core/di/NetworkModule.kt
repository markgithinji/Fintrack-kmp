package com.fintrack.shared.feature.core.di

import com.fintrack.shared.feature.core.data.remote.ApiClient
import com.fintrack.shared.feature.core.data.remote.ApiConfig
import com.fintrack.shared.feature.core.logger.KMPLogger
import com.fintrack.shared.feature.core.util.createFileSaver
import io.ktor.client.HttpClient
import org.koin.dsl.module

val coreModule = module {
    single { KMPLogger() }
    single<HttpClient> {
        ApiClient(
            tokenDataSource = get(),
            baseUrl = ApiConfig.BASE_URL
        ).httpClient
    }
    single { createFileSaver() }
}
