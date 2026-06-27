package com.fintrack.shared.feature.transaction.domain.model

data class Category(
    val id: String,
    val name: String,
    val isExpense: Boolean,
    val iconName: String? = null,
    val isDefault: Boolean = false
) {
    companion object {
        // --- Expense categories ---
        val Food = Category("food", "Food", true, "Fastfood", true)
        val Transport = Category("transport", "Transport", true, "DirectionsCar", true)
        val Shopping = Category("shopping", "Shopping", true, "ShoppingCart", true)
        val Health = Category("health", "Health", true, "LocalHospital", true)
        val Bills = Category("bills", "Bills", true, "Receipt", true)
        val Entertainment = Category("entertainment", "Entertainment", true, "Movie", true)
        val Education = Category("education", "Education", true, "School", true)
        val GiftsExpense = Category("gifts_expense", "Gifts", true, "CardGiftcard", true)
        val Travel = Category("travel", "Travel", true, "Flight", true)
        val PersonalCare = Category("personal_care", "Personal Care", true, "ContentCut", true)
        val Subscriptions = Category("subscriptions", "Subscriptions", true, "Subscriptions", true)
        val Rent = Category("rent", "Rent", true, "Home", true)
        val Groceries = Category("groceries", "Groceries", true, "ShoppingBag", true)
        val Insurance = Category("insurance", "Insurance", true, "Shield", true)
        val MiscExpense = Category("misc_expense", "Misc", true, "HelpOutline", true)

        // --- Income categories ---
        val Salary = Category("salary", "Salary", false, "AttachMoney", true)
        val Freelance = Category("freelance", "Freelance", false, "Work", true)
        val Investments = Category("investments", "Investments", false, "TrendingUp", true)
        val GiftsIncome = Category("gifts_income", "Gifts", false, "CardGiftcard", true)
        val OtherIncome = Category("other_income", "Other", false, "AttachMoney", true)

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
        
        fun fromId(id: String): Category? = 
            allCategories.find { it.id == id }
    }
}
