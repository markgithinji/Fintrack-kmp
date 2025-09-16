package com.fintrack.shared.feature.transaction.ui

data class AppBarState(
    val title: String,
    val showBackButton: Boolean = false,
    val onBack: (() -> Unit)? = null
)
