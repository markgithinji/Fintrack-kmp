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
import com.example.compose.GreenIncome
import com.example.compose.PinkExpense
import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.account.ui.AccountsViewModel
import com.fintrack.shared.feature.budget.ui.AccountSelectionSection
import com.fintrack.shared.feature.budget.ui.MaterialToast
import com.fintrack.shared.feature.core.domain.SaveState
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
    val saveState by transactionsViewModel.saveState.collectAsStateWithLifecycle()
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

    var showValidationToast by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf("") }

    val onAccountSelected = remember { { account: Account? -> selectedAccount = account } }
    val onRetry = remember(accountsViewModel) { { accountsViewModel.reloadAccounts() } }

    val onAmountChange = remember { { newAmount: String -> amount = newAmount } }
    val onIncomeChange = remember { { newIsIncome: Boolean -> isIncome = newIsIncome } }
    val onCategorySelected = remember { { newCategory: Category? -> category = newCategory } }
    val onDescriptionChange = remember { { newDescription: String -> description = newDescription } }

    val onDateClicked = remember { { showDatePicker = true } }
    val onTimeClicked = remember { { showTimePicker = true } }

    val validationResult = remember(amount, category, selectedAccount) {
        validateTransactionForm(
            amount = amount,
            category = category,
            selectedAccount = selectedAccount
        )
    }

    val onSaveClick = remember(validationResult) {
        {
            if (validationResult.isValid) {
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
                validationMessage = validationResult.errorMessage
                showValidationToast = true
            }
        }
    }

    LaunchedEffect(saveState) {
        when (saveState) {
            is SaveState.Success -> {
                delay(1000)
                onBack()
            }

            else -> Unit
        }
    }

    // Auto-hide validation toast
    LaunchedEffect(showValidationToast) {
        if (showValidationToast) {
            delay(3000)
            showValidationToast = false
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

            AccountSelectionSection(
                accountsResult = accountsResult,
                selectedAccount = selectedAccount,
                onAccountSelected = onAccountSelected,
                onRetry = onRetry
            )

            AmountInputSection(
                amount = amount,
                onAmountChange = onAmountChange
            )

            TransactionTypeSection(
                isIncome = isIncome,
                onIncomeChange = onIncomeChange
            )

            CategorySelectionSection(
                isIncome = isIncome,
                selectedCategory = category,
                onCategorySelected = onCategorySelected
            )

            DescriptionInputSection(
                description = description,
                onDescriptionChange = onDescriptionChange
            )

            DateTimeSelectionSection(
                dateTime = dateTime,
                onDateClicked = onDateClicked,
                onTimeClicked = onTimeClicked
            )

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

            SaveTransactionButton(
                saveState = saveState,
                amount = amount,
                category = category,
                selectedAccount = selectedAccount,
                onSaveClick = onSaveClick
            )
        }

        if (showValidationToast) {
            MaterialToast(
                message = validationMessage,
                isError = true,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        if (saveState is SaveState.Error) {
            val error = (saveState as SaveState.Error).exception
            MaterialToast(
                message = error.message ?: "Failed to save transaction",
                isError = true,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun AmountInputSection(
    amount: String,
    onAmountChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Amount", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = amount,
                onValueChange = { newAmount ->
                    val filtered = newAmount.filter { it.isDigit() || it == '.' }
                    val dotCount = filtered.count { it == '.' }

                    if (dotCount <= 1) {
                        if (dotCount == 1) {
                            val parts = filtered.split('.')
                            if (parts[1].length <= 2) {
                                onAmountChange(filtered)
                            }
                        } else {
                            onAmountChange(filtered)
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
    }
}

@Composable
fun TransactionTypeSection(
    isIncome: Boolean,
    onIncomeChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    onClick = { onIncomeChange(false) },
                    color = Color.Red
                )
                ToggleChip(
                    text = "Income",
                    icon = Icons.Default.ArrowUpward,
                    selected = isIncome,
                    onClick = { onIncomeChange(true) },
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }
}

@Composable
fun CategorySelectionSection(
    isIncome: Boolean,
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    val selected = selectedCategory == cat
                    CategoryChip(
                        text = cat.name,
                        icon = cat.toIcon(),
                        color = cat.toColor(),
                        selected = selected,
                        onClick = { onCategorySelected(if (selected) null else cat) }
                    )
                }
            }
        }
    }
}

@Composable
fun DescriptionInputSection(
    description: String,
    onDescriptionChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Description", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = description,
                onValueChange = onDescriptionChange,
                placeholder = { Text("Optional description") },
                singleLine = false,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        Icons.AutoMirrored.Filled.Notes,
                        null,
                        tint = Color.Gray
                    )
                },
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
    }
}

@Composable
fun DateTimeSelectionSection(
    dateTime: LocalDateTime,
    onDateClicked: () -> Unit,
    onTimeClicked: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onDateClicked),
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

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onTimeClicked),
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
    }
}

@Composable
fun SaveTransactionButton(
    saveState: SaveState<Transaction>,
    amount: String,
    category: Category?,
    selectedAccount: Account?,
    onSaveClick: () -> Unit
) {
    // Determine if we should show the button as interactive
    val isFormValid = amount.isNotBlank() && category != null && selectedAccount != null
    val isInProgress = saveState is SaveState.Loading
    val isSuccess = saveState is SaveState.Success<*>

    val baseColor = category?.toColor() ?: MaterialTheme.colorScheme.primary

    val buttonColor = when {
        isSuccess -> GreenIncome
        else -> baseColor
    }

    Button(
        onClick = onSaveClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            disabledContainerColor = when {
                isSuccess -> GreenIncome.copy(alpha = 0.9f)
                isInProgress -> baseColor.copy(alpha = 0.9f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            disabledContentColor = when {
                isSuccess -> Color.White
                isInProgress -> Color.White
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        ),
        enabled = !isInProgress && !isSuccess && isFormValid
    ) {
        when (saveState) {
            is SaveState.Loading -> {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            is SaveState.Success<*> -> {
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
    color: Color? = null
) {
    val chipColor = color ?: when (text.lowercase()) {
        "expense" -> PinkExpense
        "income" -> GreenIncome
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) chipColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, if (selected) chipColor else Color.Transparent)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Icon(icon, null, tint = if (selected) chipColor else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text, color = if (selected) chipColor else MaterialTheme.colorScheme.onSurfaceVariant)
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

