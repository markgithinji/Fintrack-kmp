package com.fintrack.shared.feature.transaction.ui.addtransaction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.account.ui.AccountsViewModel
import com.fintrack.shared.feature.budget.ui.AccountSelectionSection
import com.fintrack.shared.feature.budget.ui.MaterialToast
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.transaction.domain.model.Category
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import com.fintrack.shared.feature.transaction.ui.TransactionViewModel
import com.fintrack.shared.feature.transaction.ui.util.toColor
import com.fintrack.shared.feature.transaction.ui.util.toIcon
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime

//TODO: better organise states and move validation logic to viewmodel or usecases


@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun AddTransactionScreen(
    transactionsViewModel: TransactionViewModel = koinViewModel(),
    accountsViewModel: AccountsViewModel = koinViewModel(),
    onBack: () -> Unit = {}
) {
    val saveResult by transactionsViewModel.saveResult.collectAsStateWithLifecycle()
    val accountsResult by accountsViewModel.accounts.collectAsStateWithLifecycle()

    var amount by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf<Category?>(null) }
    var description by remember { mutableStateOf("") }
    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    var dateTime by remember {
        mutableStateOf(
            kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        )
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // State for showing validation toast
    var showValidationToast by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf("") }


    LaunchedEffect(saveResult) {
        when (saveResult) {
            is Result.Success -> {
                // Navigate back after a short delay to show success state
                delay(1000)
                onBack()
            }
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // Account Selection
            AccountSelectionSection(
                accountsResult = accountsResult,
                selectedAccount = selectedAccount,
                onAccountSelected = { account ->
                    selectedAccount = account
                },
                onRetry = {
                    accountsViewModel.reloadAccounts()
                }
            )

            // Amount
            Text("Amount", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = amount,
                    onValueChange = { newAmount ->
                        // Validate amount input
                        val filtered = newAmount.filter { it.isDigit() || it == '.' }
                        val dotCount = filtered.count { it == '.' }

                        // Allow only one decimal point
                        if (dotCount <= 1) {
                            // Allow only 2 decimal places
                            if (dotCount == 1) {
                                val parts = filtered.split('.')
                                if (parts[1].length <= 2) {
                                    amount = filtered
                                }
                            } else {
                                amount = filtered
                            }
                        }
                    },
                    placeholder = { Text("Enter amount") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        Text(
                            "Ksh",
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    },
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
            }

            // Transaction Type
            Text("Transaction Type", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
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
            }

            // Categories
            Text("Category", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                LazyHorizontalStaggeredGrid(
                    rows = StaggeredGridCells.Adaptive(48.dp),
                    horizontalItemSpacing = 8.dp,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    val categories =
                        if (isIncome) Category.incomeCategories else Category.expenseCategories
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
            }

            // Description
            Text("Description", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                modifier = Modifier.fillMaxWidth()
            ) {
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
            }

            // Date & Time Section
            Text("Date & Time", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date Column
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showDatePicker = true },
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text("Date", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${dateTime.date}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = "Pick Date",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Time Column
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showTimePicker = true },
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text("Time", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val hourStr = dateTime.time.hour.toString().padStart(2, '0')
                            val minuteStr = dateTime.time.minute.toString().padStart(2, '0')
                            Text(
                                text = "$hourStr:$minuteStr",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = "Pick Time",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Pickers
            if (showDatePicker) {
                PickDate(
                    initialDate = dateTime.date,
                    onDateSelected = { selectedDate ->
                        dateTime = LocalDateTime(date = selectedDate, time = dateTime.time)
                        showDatePicker = false
                    },
                    onDismiss = { showDatePicker = false }
                )
            }

            if (showTimePicker) {
                PickTime(
                    initialTime = dateTime.time,
                    onTimeSelected = { selectedTime ->
                        dateTime = LocalDateTime(date = dateTime.date, time = selectedTime)
                        showTimePicker = false
                    },
                    onDismiss = { showTimePicker = false }
                )
            }

            // Save Button
            Button(
                onClick = {
                    // Validate all fields
                    val validation = validateTransactionForm(
                        amount = amount,
                        category = category,
                        selectedAccount = selectedAccount
                    )

                    if (validation.isValid) {
                        val parsedAmount = amount.toDoubleOrNull()
                        if (parsedAmount != null) {
                            transactionsViewModel.addTransaction(
                                transaction = Transaction(
                                    id = null,
                                    accountId = selectedAccount!!.id,
                                    amount = parsedAmount,
                                    isIncome = isIncome,
                                    category = category!!.name,
                                    description = description.takeIf { it.isNotBlank() },
                                    dateTime = dateTime
                                )
                            )
                        }
                    } else {
                        // Show validation error toast
                        validationMessage = validation.errorMessage
                        showValidationToast = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = category?.toColor() ?: MaterialTheme.colorScheme.primary
                ),
                enabled = amount.isNotBlank() && category != null && selectedAccount != null
            ) {
                when (saveResult) {
                    is Result.Loading -> {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    is Result.Success -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Saved",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text("Saved ✓", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }
                    }
                    else -> {
                        Text("Save Transaction", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                }
            }
        }

        // Show validation error toast
        if (showValidationToast) {
            MaterialToast(
                message = validationMessage,
                isError = true,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        // Show API error toast
        if (saveResult is Result.Error) {
            val error = (saveResult as Result.Error).exception
            MaterialToast(
                message = error.message ?: "Failed to save transaction",
                isError = true,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

// Validation helper function
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String = ""
)

fun validateTransactionForm(
    amount: String,
    category: Category?,
    selectedAccount: Account?
): ValidationResult {
    return when {
        amount.isBlank() -> ValidationResult(
            isValid = false,
            errorMessage = "Please enter an amount"
        )
        amount.toDoubleOrNull() == null -> ValidationResult(
            isValid = false,
            errorMessage = "Please enter a valid amount"
        )
        amount.toDoubleOrNull()?.let { it <= 0 } == true -> ValidationResult(
            isValid = false,
            errorMessage = "Amount must be greater than zero"
        )
        category == null -> ValidationResult(
            isValid = false,
            errorMessage = "Please select a category"
        )
        selectedAccount == null -> ValidationResult(
            isValid = false,
            errorMessage = "Please select an account"
        )
        else -> ValidationResult(isValid = true)
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


