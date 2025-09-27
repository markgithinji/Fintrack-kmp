package com.fintrack.shared.feature.transaction.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.datetime.LocalTime

@Composable
actual fun PickTime(
    initialTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    var hour by remember { mutableStateOf(initialTime?.hour ?: 0) }
    var minute by remember { mutableStateOf(initialTime?.minute ?: 0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pick Time") },
        text = {
            Column {
                Row {
                    Text("Hour:")
                    Slider(
                        value = hour.toFloat(),
                        onValueChange = { hour = it.toInt() },
                        valueRange = 0f..23f,
                        steps = 23
                    )
                    Text("$hour")
                }
                Row {
                    Text("Minute:")
                    Slider(
                        value = minute.toFloat(),
                        onValueChange = { minute = it.toInt() },
                        valueRange = 0f..59f,
                        steps = 59
                    )
                    Text("$minute")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onTimeSelected(LocalTime(hour, minute)) }) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
