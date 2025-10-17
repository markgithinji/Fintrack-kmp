package com.fintrack.shared.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.compose.backgroundDark
import com.example.compose.backgroundLight
import com.example.compose.errorContainerDark
import com.example.compose.errorContainerLight
import com.example.compose.errorDark
import com.example.compose.errorLight
import com.example.compose.inverseOnSurfaceDark
import com.example.compose.inverseOnSurfaceLight
import com.example.compose.inverseSurfaceDark
import com.example.compose.inverseSurfaceLight
import com.example.compose.onBackgroundDark
import com.example.compose.onBackgroundLight
import com.example.compose.onErrorContainerDark
import com.example.compose.onErrorContainerLight
import com.example.compose.onErrorDark
import com.example.compose.onErrorLight
import com.example.compose.onPrimaryContainerDark
import com.example.compose.onPrimaryContainerLight
import com.example.compose.onPrimaryDark
import com.example.compose.onPrimaryLight
import com.example.compose.onSecondaryContainerDark
import com.example.compose.onSecondaryContainerLight
import com.example.compose.onSecondaryDark
import com.example.compose.onSecondaryLight
import com.example.compose.onSurfaceDark
import com.example.compose.onSurfaceLight
import com.example.compose.onSurfaceVariantDark
import com.example.compose.onSurfaceVariantLight
import com.example.compose.onTertiaryContainerDark
import com.example.compose.onTertiaryContainerLight
import com.example.compose.onTertiaryDark
import com.example.compose.onTertiaryLight
import com.example.compose.outlineDark
import com.example.compose.outlineLight
import com.example.compose.primaryContainerDark
import com.example.compose.primaryContainerLight
import com.example.compose.primaryDark
import com.example.compose.primaryLight
import com.example.compose.secondaryContainerDark
import com.example.compose.secondaryContainerLight
import com.example.compose.secondaryDark
import com.example.compose.secondaryLight
import com.example.compose.surfaceDark
import com.example.compose.surfaceLight
import com.example.compose.surfaceVariantDark
import com.example.compose.surfaceVariantLight
import com.example.compose.tertiaryContainerDark
import com.example.compose.tertiaryContainerLight
import com.example.compose.tertiaryDark
import com.example.compose.tertiaryLight

private val LightColorScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    outline = outlineLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    outline = outlineDark,
)

@Composable
fun FinanceTrackerTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FinanceTypography,
        content = content
    )
}