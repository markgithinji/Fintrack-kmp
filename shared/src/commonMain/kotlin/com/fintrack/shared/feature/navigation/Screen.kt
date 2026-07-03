package com.fintrack.shared.feature.navigation


sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddTransaction : Screen("add_transaction?transactionId={transactionId}") {
        fun createRoute(transactionId: String? = null): String {
            return if (transactionId == null) "add_transaction" else "add_transaction?transactionId=$transactionId"
        }
    }
    object Statistics : Screen("statistics")
    object Budget : Screen("budget")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
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

    object TransactionList : Screen("transaction_list?accountId={accountId}&isIncome={isIncome}&category={category}&startDate={startDate}&endDate={endDate}&hasTransactionCost={hasTransactionCost}") {
        fun createRoute(
            accountId: String? = null,
            isIncome: Boolean? = null,
            category: String? = null,
            startDate: String? = null,
            endDate: String? = null,
            hasTransactionCost: Boolean? = null
        ): String {
            val builder = StringBuilder("transaction_list?")
            accountId?.let { builder.append("accountId=$it&") }
            isIncome?.let { builder.append("isIncome=$it&") }
            category?.let { builder.append("category=$it&") }
            startDate?.let { builder.append("startDate=$it&") }
            endDate?.let { builder.append("endDate=$it&") }
            hasTransactionCost?.let { builder.append("hasTransactionCost=$it&") }
            return builder.toString().removeSuffix("&").removeSuffix("?")
        }
    }
}