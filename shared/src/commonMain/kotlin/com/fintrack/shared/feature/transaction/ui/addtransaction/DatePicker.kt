package com.fintrack.shared.feature.transaction.ui.addtransaction

import kotlinx.datetime.LocalDate

import androidx.compose.runtime.Composable

@Composable
expect fun PickDate(
    initialDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
)

