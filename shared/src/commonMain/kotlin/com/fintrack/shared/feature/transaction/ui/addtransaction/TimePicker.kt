package com.fintrack.shared.feature.transaction.ui.addtransaction

import kotlinx.datetime.LocalTime
import androidx.compose.runtime.Composable

@Composable
expect fun PickTime(
    initialTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
)
