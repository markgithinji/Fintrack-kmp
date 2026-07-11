package com.fintrack.shared.feature.budget.di

import com.fintrack.shared.feature.budget.data.remote.BudgetApi
import com.fintrack.shared.feature.budget.data.repository.BudgetRepositoryImpl
import com.fintrack.shared.feature.budget.domain.repository.BudgetRepository
import com.fintrack.shared.feature.budget.domain.usecase.BudgetValidationUseCase
import com.fintrack.shared.feature.budget.domain.usecase.CheckBudgetThresholdsUseCase
import com.fintrack.shared.feature.budget.ui.BudgetViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val budgetModule = module {
    single { BudgetApi(get()) }
    single<BudgetRepository> { BudgetRepositoryImpl(get()) }
    single { BudgetValidationUseCase() }
    single { CheckBudgetThresholdsUseCase(get(), get(), get()) }
    viewModel { BudgetViewModel(get(), get(), get()) }
}