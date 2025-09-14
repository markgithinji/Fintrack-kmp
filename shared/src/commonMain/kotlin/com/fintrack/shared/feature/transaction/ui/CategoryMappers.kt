package com.fintrack.shared.feature.transaction.ui

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
import com.fintrack.shared.feature.transaction.model.Category


fun Category.toIcon(): ImageVector = when (this) {
    Category.Food -> Icons.Default.Fastfood
    Category.Transport -> Icons.Default.DirectionsCar
    Category.Shopping -> Icons.Default.ShoppingCart
    Category.Health -> Icons.Default.LocalHospital
    Category.Bills -> Icons.Default.Receipt
    Category.Entertainment -> Icons.Default.Movie
    Category.Education -> Icons.Default.School
    Category.GiftsExpense, Category.GiftsIncome -> Icons.Default.CardGiftcard
    Category.Travel -> Icons.Default.Flight
    Category.PersonalCare -> Icons.Default.ContentCut
    Category.Subscriptions -> Icons.Default.Subscriptions
    Category.Rent -> Icons.Default.Home
    Category.Groceries -> Icons.Default.ShoppingBag
    Category.Insurance -> Icons.Default.Shield
    Category.MiscExpense, Category.OtherIncome -> Icons.Default.HelpOutline
    Category.Salary -> Icons.Default.AttachMoney
    Category.Freelance -> Icons.Default.Work
    Category.Investments -> Icons.Default.TrendingUp
    Category.OtherIncome -> Icons.Default.AttachMoney
}

fun Category.toColor(): Color = when (this) {
    Category.Food -> Color(0xFFFFA726)
    Category.Transport -> Color(0xFF29B6F6)
    Category.Shopping -> Color(0xFFAB47BC)
    Category.Health -> Color(0xFFEF5350)
    Category.Bills -> Color(0xFF8D6E63)
    Category.Entertainment -> Color(0xFFFF7043)
    Category.Education -> Color(0xFF42A5F5)
    Category.GiftsExpense, Category.GiftsIncome -> Color(0xFFEC407A)
    Category.Travel -> Color(0xFF26C6DA)
    Category.PersonalCare -> Color(0xFFFFCA28)
    Category.Subscriptions -> Color(0xFF66BB6A)
    Category.Rent -> Color(0xFF7E57C2)
    Category.Groceries -> Color(0xFF8BC34A)
    Category.Insurance -> Color(0xFF78909C)
    Category.MiscExpense, Category.OtherIncome -> Color.Gray
    Category.Salary -> Color(0xFF2E7D32)
    Category.Freelance -> Color(0xFF0097A7)
    Category.Investments -> Color(0xFFFFB300)
}
