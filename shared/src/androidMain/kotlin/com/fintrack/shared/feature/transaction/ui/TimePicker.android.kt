package com.fintrack.shared.feature.transaction.ui

import android.app.TimePickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import kotlinx.datetime.LocalTime

@Composable
actual fun PickTime(
    initialTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val hour = initialTime?.hour ?: 0
    val minute = initialTime?.minute ?: 0

    TimePickerDialog(
        LocalContext.current,
        { _, selectedHour, selectedMinute ->
            onTimeSelected(LocalTime(selectedHour, selectedMinute))
        },
        hour,
        minute,
        true
    ).apply {
        setOnCancelListener { onDismiss() }
        show()
    }
}
