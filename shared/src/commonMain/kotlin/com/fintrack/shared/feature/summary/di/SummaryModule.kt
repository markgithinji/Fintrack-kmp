package com.fintrack.shared.feature.summary.di

import com.fintrack.shared.feature.summary.data.network.SummaryApi
import com.fintrack.shared.feature.summary.data.repository.SummaryRepositoryImpl
import com.fintrack.shared.feature.summary.domain.repository.SummaryRepository
import com.fintrack.shared.feature.summary.ui.StatisticsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val summaryModule = module {
    factory { SummaryApi(get(), getProperty("baseUrl")) }
    single<SummaryRepository> { SummaryRepositoryImpl(get()) }
    viewModel { StatisticsViewModel(get()) }
}