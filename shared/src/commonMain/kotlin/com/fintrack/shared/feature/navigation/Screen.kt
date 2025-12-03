package com.fintrack.shared.feature.navigation


sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddTransaction : Screen("add_transaction")
    object Statistics : Screen("statistics")
    object Budget : Screen("budget")
    object Profile : Screen("profile")
    object Accounts : Screen("accounts")
    object Categories : Screen("categories")
    object Settings : Screen("settings")

    object BudgetDetail : Screen("budget_detail/{budgetId}") {
        fun createRoute(budgetId: String? = null): String {
            val id = budgetId ?: ""
            return "budget_detail/$id"
        }
    }

    object Login : Screen("login")
    object Register : Screen("register")

    object TransactionList : Screen("transaction_list/{accountId}?isIncome={isIncome}") {
        fun createRoute(accountId: String, isIncome: Boolean? = null): String {
            return if (isIncome == null) {
                "transaction_list/$accountId"
            } else {
                "transaction_list/$accountId?isIncome=$isIncome"
            }
        }
    }
}