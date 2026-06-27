package com.fintrack.shared.feature.transaction.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.fintrack.shared.feature.transaction.domain.model.Category


fun Category.toIcon(): ImageVector = when (this.id) {
    Category.Food.id -> Icons.Default.Fastfood
    Category.Transport.id -> Icons.Default.DirectionsCar
    Category.Shopping.id -> Icons.Default.ShoppingCart
    Category.Health.id -> Icons.Default.LocalHospital
    Category.Bills.id -> Icons.Default.Receipt
    Category.Entertainment.id -> Icons.Default.Movie
    Category.Education.id -> Icons.Default.School
    Category.GiftsExpense.id, Category.GiftsIncome.id -> Icons.Default.CardGiftcard
    Category.Travel.id -> Icons.Default.Flight
    Category.PersonalCare.id -> Icons.Default.ContentCut
    Category.Subscriptions.id -> Icons.Default.Subscriptions
    Category.Rent.id -> Icons.Default.Home
    Category.Groceries.id -> Icons.Default.ShoppingBag
    Category.Insurance.id -> Icons.Default.Shield
    Category.MiscExpense.id, Category.OtherIncome.id -> Icons.Default.HelpOutline
    Category.Salary.id -> Icons.Default.AttachMoney
    Category.Freelance.id -> Icons.Default.Work
    Category.Investments.id -> Icons.Default.TrendingUp
    else -> Icons.Default.HelpOutline
}

fun Category.toColor(): Color = when (this.id) {
    Category.Food.id -> Color(0xFFFFA726)
    Category.Transport.id -> Color(0xFF29B6F6)
    Category.Shopping.id -> Color(0xFFAB47BC)
    Category.Health.id -> Color(0xFFEF5350)
    Category.Bills.id -> Color(0xFF8D6E63)
    Category.Entertainment.id -> Color(0xFFFF7043)
    Category.Education.id -> Color(0xFF42A5F5)
    Category.GiftsExpense.id, Category.GiftsIncome.id -> Color(0xFFEC407A)
    Category.Travel.id -> Color(0xFF26C6DA)
    Category.PersonalCare.id -> Color(0xFFFFCA28)
    Category.Subscriptions.id -> Color(0xFF66BB6A)
    Category.Rent.id -> Color(0xFF7E57C2)
    Category.Groceries.id -> Color(0xFF8BC34A)
    Category.Insurance.id -> Color(0xFF78909C)
    Category.MiscExpense.id, Category.OtherIncome.id -> Color.Gray
    Category.Salary.id -> Color(0xFF2E7D32)
    Category.Freelance.id -> Color(0xFF0097A7)
    Category.Investments.id -> Color(0xFFFFB300)
    else -> Color.Gray
}
