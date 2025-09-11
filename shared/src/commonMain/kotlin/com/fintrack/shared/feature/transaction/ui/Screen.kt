package com.fintrack.shared.feature.transaction.ui

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen(val route: String) {
    @Serializable
    object Home : Screen("home")

    @Serializable
    object AddTransaction : Screen("add_transaction")

    @Serializable
    object Statistics : Screen("statistics")

    @Serializable
    object Budget : Screen("budget")

    @Serializable
    object Profile : Screen("profile")
}