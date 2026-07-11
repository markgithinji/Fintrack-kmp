package com.fintrack.shared.feature.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.shared.feature.core.util.formatToCurrency
import com.fintrack.shared.feature.settings.domain.model.Currency
import com.fintrack.shared.feature.settings.domain.model.TimeFormat
import org.koin.compose.viewmodel.koinViewModel

val LocalCurrency = compositionLocalOf { Currency.KES }
val LocalPrivacyMode = compositionLocalOf { false }
val LocalShowDecimals = compositionLocalOf { true }
val LocalTimeFormat = compositionLocalOf { TimeFormat.TWENTY_FOUR_HOUR }

@Composable
fun Double.toCurrencyString(): String {
    if (LocalPrivacyMode.current) {
        return "${LocalCurrency.current.symbol} ****"
    }
    return this.formatToCurrency(LocalCurrency.current.symbol, showDecimals = LocalShowDecimals.current)
}

@Composable
fun AppStateProvider(
    viewModel: MainViewModel = koinViewModel(),
    content: @Composable () -> Unit
) {
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val isBalanceHidden by viewModel.isBalanceHidden.collectAsStateWithLifecycle()
    val showDecimals by viewModel.showDecimals.collectAsStateWithLifecycle()
    val timeFormat by viewModel.timeFormat.collectAsStateWithLifecycle()

    CompositionLocalProvider(
        LocalCurrency provides currency,
        LocalPrivacyMode provides isBalanceHidden,
        LocalShowDecimals provides showDecimals,
        LocalTimeFormat provides timeFormat
    ) {
        content()
    }
}
