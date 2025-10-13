package com.fintrack.shared.feature.core.di

import com.fintrack.shared.feature.account.di.accountModule
import com.fintrack.shared.feature.auth.di.authModule
import com.fintrack.shared.feature.budget.di.budgetModule
import com.fintrack.shared.feature.core.data.remote.ApiConfig
import com.fintrack.shared.feature.core.data.remote.Environment
import com.fintrack.shared.feature.summary.di.summaryModule
import com.fintrack.shared.feature.transaction.di.transactionModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

object Koin {
    private var _koin: KoinApplication? = null
    val koin: KoinApplication
        get() = _koin ?: throw IllegalStateException("Koin not initialized")

    fun init(
        environment: Environment = Environment.STAGING,
        enableNetworkLogs: Boolean = false
    ) {
        ApiConfig.initialize(environment)

        _koin = startKoin {

            properties(
                mapOf(
                    "baseUrl" to ApiConfig.BASE_URL,
                    "enableNetworkLogs" to enableNetworkLogs.toString()
                )
            )

            // Modules
            modules(
                coreModule,
                authModule,
                accountModule,
                transactionModule,
                budgetModule,
                summaryModule
            )
        }
    }

    // Helper functions to get dependencies
    inline fun <reified T> get(): T = koin.koin.get()
    inline fun <reified T> inject(): Lazy<T> = koin.koin.inject()
}