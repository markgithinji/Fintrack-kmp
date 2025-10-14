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

    object BudgetDetail : Screen("budget_detail/{budgetId}?accountId={accountId}") {
        fun createRoute(budgetId: String? = null, accountId: String? = null): String {
            val id = budgetId ?: ""
            return if (accountId != null) {
                "budget_detail/$id?accountId=$accountId"
            } else {
                "budget_detail/$id"
            }
        }
    }

    object Login : Screen("login")

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