package com.fintrack.shared.feature.transaction.ui

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddTransaction : Screen("add_transaction")
    object Statistics : Screen("statistics")
    object Budget : Screen("budget")
    object Profile : Screen("profile")

    object BudgetDetail : Screen("budget_detail/{budgetId}") {
        fun createRoute(budgetId: Int?) = "budget_detail/${budgetId ?: -1}"
    }

    object Login : Screen("login")

    object TransactionList : Screen("transaction_list/{accountId}?isIncome={isIncome}") {
        fun createRoute(accountId: Int, isIncome: Boolean? = null): String {
            return if (isIncome == null) {
                "transaction_list/$accountId"
            } else {
                "transaction_list/$accountId?isIncome=$isIncome"
            }
        }
    }

}
