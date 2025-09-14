package com.fintrack.shared.feature.transaction.model

sealed class Category(
    val name: String,
    val isExpense: Boolean
) {
    // --- Expense categories ---
    object Food : Category("Food", true)
    object Transport : Category("Transport", true)
    object Shopping : Category("Shopping", true)
    object Health : Category("Health", true)
    object Bills : Category("Bills", true)
    object Entertainment : Category("Entertainment", true)
    object Education : Category("Education", true)
    object GiftsExpense : Category("Gifts", true)
    object Travel : Category("Travel", true)
    object PersonalCare : Category("Personal Care", true)
    object Subscriptions : Category("Subscriptions", true)
    object Rent : Category("Rent", true)
    object Groceries : Category("Groceries", true)
    object Insurance : Category("Insurance", true)
    object MiscExpense : Category("Misc", true)

    // --- Income categories ---
    object Salary : Category("Salary", false)
    object Freelance : Category("Freelance", false)
    object Investments : Category("Investments", false)
    object GiftsIncome : Category("Gifts", false)
    object OtherIncome : Category("Other", false)

    companion object {
        val allCategories = listOf(
            Food, Transport, Shopping, Health, Bills,
            Entertainment, Education, GiftsExpense, Travel,
            PersonalCare, Subscriptions, Rent, Groceries,
            Insurance, MiscExpense,
            Salary, Freelance, Investments, GiftsIncome, OtherIncome
        )

        val expenseCategories = allCategories.filter { it.isExpense }
        val incomeCategories = allCategories.filter { !it.isExpense }

        fun fromName(name: String, isExpense: Boolean): Category =
            allCategories.find { it.name == name && it.isExpense == isExpense }
                ?: if (isExpense) MiscExpense else OtherIncome
    }
}
