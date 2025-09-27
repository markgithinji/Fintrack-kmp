package com.fintrack.shared.feature.transaction.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.transaction.model.Category
import com.fintrack.shared.feature.transaction.model.Transaction
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    transactionsViewModel: TransactionListViewModel = viewModel()
) {
    val saveResult by transactionsViewModel.saveResult.collectAsStateWithLifecycle()

    var amount by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf<Category?>(null) }
    var description by remember { mutableStateOf("") }
    var dateTime by remember {
        mutableStateOf(
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        // Amount
        Text("Amount", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        TextField(
            value = amount,
            onValueChange = { amount = it },
            placeholder = { Text("Enter amount") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = { Icon(Icons.Default.AttachMoney, null, tint = Color(0xFF4CAF50)) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF5F5F5),
                unfocusedContainerColor = Color(0xFFF5F5F5),
                disabledContainerColor = Color(0xFFF5F5F5),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = Color(0xFF4CAF50)
            )
        )

        // Transaction type (Income / Expense)
        Text("Transaction Type", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ToggleChip(
                text = "Expense",
                icon = Icons.Default.ArrowDownward,
                selected = !isIncome,
                onClick = { isIncome = false },
                color = Color.Red
            )
            ToggleChip(
                text = "Income",
                icon = Icons.Default.ArrowUpward,
                selected = isIncome,
                onClick = { isIncome = true },
                color = Color(0xFF2E7D32)
            )
        }

        // Categories
        Text("Category", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

        val categories = if (isIncome) Category.incomeCategories else Category.expenseCategories

        LazyHorizontalStaggeredGrid(
            rows = StaggeredGridCells.Adaptive(48.dp),
            horizontalItemSpacing = 8.dp,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            items(categories.size) { index ->
                val cat = categories[index]
                val selected = category == cat
                CategoryChip(
                    text = cat.name,
                    icon = cat.toIcon(),
                    color = cat.toColor(),
                    selected = selected,
                    onClick = { category = if (selected) null else cat }
                )
            }
        }



        // Description
        Text("Description", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        TextField(
            value = description,
            onValueChange = { description = it },
            placeholder = { Text("Optional description") },
            singleLine = false,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, null, tint = Color.Gray) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF5F5F5),
                unfocusedContainerColor = Color(0xFFF5F5F5),
                disabledContainerColor = Color(0xFFF5F5F5),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = Color.Gray
            )
        )

        // Date & Time
        Text("Date & Time", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

        var showDatePicker by remember { mutableStateOf(false) }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column {
                Text("Date: ${dateTime.date}", color = Color.DarkGray)
                val hour = dateTime.time.hour.toString().padStart(2, '0')
                val minute = dateTime.time.minute.toString().padStart(2, '0')
                Text("Time: $hour:$minute", color = Color.DarkGray)
            }

            // Calendar icon button
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Pick Date",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

// Show the Date Picker
        if (showDatePicker) {
            PickDate(
                initialDate = dateTime.date,
                onDateSelected = { selectedDate ->
                    dateTime = LocalDateTime(
                        date = selectedDate,
                        time = dateTime.time
                    )
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }

        Button(
            onClick = {
                val parsedAmount = amount.toDoubleOrNull()
                if (parsedAmount == null || category == null) return@Button

                transactionsViewModel.addTransaction(
                    Transaction(
                        id = null,
                        amount = parsedAmount,
                        isIncome = isIncome,
                        category = category!!.name,
                        description = description.takeIf { it.isNotBlank() },
                        dateTime = dateTime
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = category?.toColor() ?: MaterialTheme.colorScheme.primary
            )
        ) {
            if (saveResult is Result.Loading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text("Save Transaction", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}


@Composable
fun ToggleChip(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    color: Color
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) color.copy(alpha = 0.15f) else Color(0xFFF0F0F0),
        border = BorderStroke(1.dp, if (selected) color else Color.Transparent)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Icon(icon, null, tint = if (selected) color else Color.Gray)
            Text(text, color = if (selected) color else Color.Gray)
        }
    }
}

@Composable
fun CategoryChip(
    text: String,
    icon: ImageVector,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) color else Color(0xFFF5F5F5),
        shadowElevation = if (selected) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, null, tint = if (selected) Color.White else color)
            Text(text, color = if (selected) Color.White else Color.Black)
        }
    }
}


