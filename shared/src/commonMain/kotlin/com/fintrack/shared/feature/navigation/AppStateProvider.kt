package com.fintrack.shared.feature.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.fintrack.shared.feature.core.util.formatToCurrency
import com.fintrack.shared.feature.settings.domain.model.AppTheme
import com.fintrack.shared.feature.settings.domain.model.Currency
import com.fintrack.shared.feature.settings.domain.model.TimeFormat
import com.fintrack.shared.feature.settings.domain.util.BiometricAuthenticator
import com.fintrack.shared.feature.settings.domain.util.format
import com.fintrack.shared.feature.settings.domain.util.rememberBiometricAuthenticator
import com.fintrack.shared.feature.user.domain.model.User
import kotlinx.datetime.LocalTime
import org.koin.compose.viewmodel.koinViewModel

val LocalCurrency = compositionLocalOf { Currency.KES }
val LocalPrivacyMode = compositionLocalOf { false }
val LocalShowDecimals = compositionLocalOf { true }
val LocalTimeFormat = compositionLocalOf { TimeFormat.TWENTY_FOUR_HOUR }
val LocalAppTheme = compositionLocalOf { AppTheme.SYSTEM }
val LocalUser = compositionLocalOf<User?> { null }

val LocalBiometricAuthenticator = staticCompositionLocalOf<BiometricAuthenticator> {
    error("No BiometricAuthenticator provided")
}

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("No NavController provided")
}

@Composable
fun Double.toCurrencyString(): String {
    val currency = LocalCurrency.current
    val isPrivacyMode = LocalPrivacyMode.current
    val showDecimals = LocalShowDecimals.current

    if (isPrivacyMode) {
        return "${currency.symbol} ****"
    }
    return this.formatToCurrency(currency.symbol, showDecimals = showDecimals)
}

@Composable
fun AppStateProvider(
    viewModel: MainViewModel = koinViewModel(),
    navController: NavHostController = rememberNavController(),
    content: @Composable () -> Unit
) {
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val isBalanceHidden by viewModel.isBalanceHidden.collectAsStateWithLifecycle()
    val showDecimals by viewModel.showDecimals.collectAsStateWithLifecycle()
    val timeFormat by viewModel.timeFormat.collectAsStateWithLifecycle()
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val biometricAuthenticator = rememberBiometricAuthenticator()

    CompositionLocalProvider(
        LocalCurrency provides currency,
        LocalPrivacyMode provides isBalanceHidden,
        LocalShowDecimals provides showDecimals,
        LocalTimeFormat provides timeFormat,
        LocalAppTheme provides theme,
        LocalUser provides userProfile,
        LocalBiometricAuthenticator provides biometricAuthenticator,
        LocalNavController provides navController
    ) {
        content()
    }
}
