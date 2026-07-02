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
        val DiningOut = Category("dining_out", "Dining Out", true, "Restaurant", true)
        val Utilities = Category("utilities", "Utilities", true, "Lightbulb", true)
        val Pets = Category("pets", "Pets", true, "Pets", true)
        val Fitness = Category("fitness", "Fitness", true, "FitnessCenter", true)
        val Maintenance = Category("maintenance", "Maintenance", true, "Build", true)
        val TransactionCost = Category("transaction_cost", "Transaction Cost", true, "Receipt", true)
        val MiscExpense = Category("misc_expense", "Misc", true, "HelpOutline", true)

        // --- Income categories ---
        val Salary = Category("salary", "Salary", false, "AttachMoney", true)
        val Freelance = Category("freelance", "Freelance", false, "Work", true)
        val Investments = Category("investments", "Investments", false, "TrendingUp", true)
        val GiftsIncome = Category("gifts_income", "Gifts", false, "CardGiftcard", true)
        val Bonus = Category("bonus", "Bonus", false, "Paid", true)
        val Rental = Category("rental", "Rental", false, "RealEstateAgent", true)
        val Dividends = Category("dividends", "Dividends", false, "Analytics", true)
        val Interest = Category("interest", "Interest", false, "Percent", true)
        val OtherIncome = Category("other_income", "Other", false, "AttachMoney", true)

        val allCategories = listOf(
            Food, Transport, Shopping, Health, Bills,
            Entertainment, Education, GiftsExpense, Travel,
            PersonalCare, Subscriptions, Rent, Groceries,
            Insurance, DiningOut, Utilities, Pets, Fitness, Maintenance, TransactionCost, MiscExpense,
            Salary, Freelance, Investments, GiftsIncome, Bonus, Rental, Dividends, Interest, OtherIncome
        )

        val expenseCategories = allCategories.filter { it.isExpense }
        val incomeCategories = allCategories.filter { !it.isExpense }

        fun fromName(name: String, isExpense: Boolean): Category =
            allCategories.find { it.name == name && it.isExpense == isExpense }
                ?: Category(id = "custom_$name", name = name, isExpense = isExpense)
        
        fun fromId(id: String): Category? = 
            allCategories.find { it.id == id }
    }
}
