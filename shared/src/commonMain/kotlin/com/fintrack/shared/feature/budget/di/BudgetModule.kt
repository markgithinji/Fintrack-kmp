package com.fintrack.shared.feature.budget.di

import com.fintrack.shared.feature.budget.data.remote.BudgetApi
import com.fintrack.shared.feature.budget.data.repository.BudgetRepositoryImpl
import com.fintrack.shared.feature.budget.domain.repository.BudgetRepository
import com.fintrack.shared.feature.budget.ui.BudgetViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val budgetModule = module {
    factory { BudgetApi(get(), getProperty("baseUrl")) }
    single<BudgetRepository> { BudgetRepositoryImpl(get()) }
    viewModel{ BudgetViewModel(get()) }
}