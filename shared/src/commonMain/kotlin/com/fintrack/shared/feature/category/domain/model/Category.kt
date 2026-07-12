package com.fintrack.shared.feature.category.domain.model

data class Category(
    val id: String,
    val name: String,
    val isExpense: Boolean,
    val iconName: String? = null,
    val isDefault: Boolean = false
) {
    companion object {
        // Expense categories
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
        val Internet = Category("internet", "Internet", true, "Wifi", true)
        val Airtime = Category("airtime", "Airtime", true, "Smartphone", true)
        val Bank = Category("bank", "Bank", true, "AccountBalance", true)
        val Loans = Category("loans", "Loans", true, "AccountBalanceWallet", true)
        val Charity = Category("charity", "Charity", true, "VolunteerActivism", true)
        val Government = Category("government", "Government", true, "AccountBalance", true)
        val Savings = Category("savings", "Savings", true, "Savings", true)
        val Transfer = Category("transfer", "Transfer", true, "Sync", true)
        val Pets = Category("pets", "Pets", true, "Pets", true)
        val Fitness = Category("fitness", "Fitness", true, "FitnessCenter", true)
        val Maintenance = Category("maintenance", "Maintenance", true, "Build", true)
        val TransactionCost = Category("transaction_cost", "Transaction Fees", true, "Receipt", true)
        val MiscExpense = Category("misc_expense", "Misc", true, "HelpOutline", true)

        // Income categories
        val Salary = Category("salary", "Salary", false, "AttachMoney", true)
        val Freelance = Category("freelance", "Freelance", false, "Work", true)
        val Investments = Category("investments", "Investments", false, "TrendingUp", true)
        val GiftsIncome = Category("gifts_income", "Gifts", false, "CardGiftcard", true)
        val Bonus = Category("bonus", "Bonus", false, "Paid", true)
        val Rental = Category("rental", "Rental", false, "RealEstateAgent", true)
        val Dividends = Category("dividends", "Dividends", false, "Analytics", true)
        val Interest = Category("interest", "Interest", false, "Percent", true)
        val LoansIncome = Category("loans_income", "Loans", false, "AccountBalanceWallet", true)
        val TransferIncome = Category("transfer_income", "Transfer", false, "Sync", true)
        val SavingsIncome = Category("savings_income", "Savings", false, "Savings", true)
        val OtherIncome = Category("other_income", "Other Income", false, "AttachMoney", true)

        val allCategories = listOf(
            Food, Transport, Shopping, Health, Bills,
            Entertainment, Education, GiftsExpense, Travel,
            PersonalCare, Subscriptions, Rent, Groceries,
            Insurance, DiningOut, Utilities, Internet, Airtime, Bank, Loans, Charity, Government, Savings, Transfer,
            Pets, Fitness, Maintenance, TransactionCost, MiscExpense,
            Salary, Freelance, Investments, GiftsIncome, Bonus, Rental, Dividends, Interest, 
            LoansIncome, TransferIncome, SavingsIncome, OtherIncome
        )

        val expenseCategories = allCategories.filter { it.isExpense }
        val incomeCategories = allCategories.filter { !it.isExpense }

        fun fromName(name: String, isExpense: Boolean): Category =
            allCategories.find { it.name == name && it.isExpense == isExpense }
                ?: Category(id = "custom_$name", name = name, isExpense = isExpense)

        fun fromId(id: String, name: String? = null, isExpense: Boolean = true, knownCategories: List<Category> = emptyList()): Category =
            allCategories.find { it.id == id }
                ?: knownCategories.find { it.id == id }
                ?: Category(id = id, name = name ?: "Unknown", isExpense = isExpense)
    }
}
