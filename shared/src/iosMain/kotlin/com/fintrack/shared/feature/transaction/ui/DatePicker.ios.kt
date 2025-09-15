package com.fintrack.shared.feature.transaction.ui

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate

@Composable
actual fun PickDate(
    initialDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
}