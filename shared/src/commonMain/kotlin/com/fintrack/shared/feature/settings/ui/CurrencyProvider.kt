package com.fintrack.shared.feature.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.shared.feature.core.util.formatToCurrency
import com.fintrack.shared.feature.settings.domain.model.Currency
import org.koin.compose.viewmodel.koinViewModel

val LocalCurrency = compositionLocalOf { Currency.KES }

@Composable
fun Double.toCurrencyString(): String {
    return this.formatToCurrency(LocalCurrency.current.symbol)
}

@Composable
fun CurrencyProvider(
    viewModel: SettingsViewModel = koinViewModel(),
    content: @Composable () -> Unit
) {
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    CompositionLocalProvider(LocalCurrency provides currency) {
        content()
    }
}