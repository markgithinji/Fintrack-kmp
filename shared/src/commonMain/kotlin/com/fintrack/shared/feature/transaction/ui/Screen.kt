package com.fintrack.shared.feature.transaction.ui

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddTransaction : Screen("add_transaction")
    object Statistics : Screen("statistics")
    object Budget : Screen("budget")
    object Profile : Screen("profile")

    object BudgetDetail : Screen("budget_detail/{budgetId}") {
        fun createRoute(budgetId: Int?) = "budget_detail/${budgetId ?: -1}"
    }
}
