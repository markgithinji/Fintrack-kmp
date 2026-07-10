package com.fintrack.shared.feature.navigation

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val navigationModule = module {
    viewModelOf(::MainViewModel)
}
