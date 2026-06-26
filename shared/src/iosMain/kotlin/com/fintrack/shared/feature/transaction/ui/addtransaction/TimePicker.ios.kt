package com.fintrack.shared.feature.transaction.ui.addtransaction

import androidx.compose.runtime.Composable
import com.fintrack.shared.feature.core.ui.FintrackTimePickerDialog
import kotlinx.datetime.LocalTime


@Composable
actual fun PickTime(
    initialTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    FintrackTimePickerDialog(
        initialTime = initialTime,
        onTimeSelected = onTimeSelected,
        onDismiss = onDismiss
    )
}
