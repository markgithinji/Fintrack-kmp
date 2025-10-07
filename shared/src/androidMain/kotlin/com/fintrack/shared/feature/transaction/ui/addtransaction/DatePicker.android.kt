package com.fintrack.shared.feature.transaction.ui.addtransaction

import android.app.DatePickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import kotlinx.datetime.LocalDate


@Composable
actual fun PickDate(
    initialDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current
    val date = initialDate ?: LocalDate(2025, 1, 1)

    DatePickerDialog(
        ctx,
        { _, year, month, day ->
            onDateSelected(LocalDate(year, month + 1, day))
        },
        date.year,
        date.monthNumber - 1,
        date.dayOfMonth
    ).apply {
        setOnCancelListener { onDismiss() }
        show()
    }
}