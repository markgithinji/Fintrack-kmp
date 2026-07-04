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
val LocalPrivacyMode = compositionLocalOf { false }
val LocalShowDecimals = compositionLocalOf { true }

@Composable
fun Double.toCurrencyString(): String {
    if (LocalPrivacyMode.current) {
        return "${LocalCurrency.current.symbol} ****"
    }
    return this.formatToCurrency(LocalCurrency.current.symbol, showDecimals = LocalShowDecimals.current)
}

@Composable
fun CurrencyProvider(
    viewModel: SettingsViewModel = koinViewModel(),
    content: @Composable () -> Unit
) {
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val isBalanceHidden by viewModel.isBalanceHidden.collectAsStateWithLifecycle()
    val showDecimals by viewModel.showDecimals.collectAsStateWithLifecycle()

    CompositionLocalProvider(
        LocalCurrency provides currency,
        LocalPrivacyMode provides isBalanceHidden,
        LocalShowDecimals provides showDecimals
    ) {
        content()
    }
}
