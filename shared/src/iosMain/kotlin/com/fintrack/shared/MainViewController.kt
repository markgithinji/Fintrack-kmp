package com.fintrack.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.fintrack.shared.feature.transaction.ui.IncomeTrackerScreen

fun MainViewController() = ComposeUIViewController { IncomeTrackerScreen() }