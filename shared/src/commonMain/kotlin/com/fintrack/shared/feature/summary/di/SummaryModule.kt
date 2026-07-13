package com.fintrack.shared.feature.summary.di

import com.fintrack.shared.feature.summary.data.network.SummaryApi
import com.fintrack.shared.feature.summary.data.repository.SummaryRepositoryImpl
import com.fintrack.shared.feature.summary.domain.repository.SummaryRepository
import com.fintrack.shared.feature.summary.ui.StatisticsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val summaryModule = module {
    single { SummaryApi(client = get()) }
    single<SummaryRepository> { SummaryRepositoryImpl(summaryApi = get()) }
    viewModel { StatisticsViewModel(summaryRepository = get()) }
}