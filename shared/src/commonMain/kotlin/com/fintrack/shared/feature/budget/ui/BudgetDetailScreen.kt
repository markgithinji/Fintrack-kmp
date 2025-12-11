package com.fintrack.shared.feature.budget.ui

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.compose.PinkExpense
import com.example.compose.accountChipBorder
import com.example.compose.accountChipSelectedBg
import com.example.compose.cardBackground
import com.example.compose.currencyTextColor
import com.example.compose.incomeButtonColor
import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.account.ui.AccountsViewModel
import com.fintrack.shared.feature.budget.domain.model.BudgetWithStatus
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.core.util.formatAsShortDateWithYear
import com.fintrack.shared.feature.transaction.domain.model.Category
import com.fintrack.shared.feature.transaction.ui.addtransaction.CategoryChip
import com.fintrack.shared.feature.transaction.ui.addtransaction.ToggleChip
import com.fintrack.shared.feature.transaction.ui.home.AccountIcon
import com.fintrack.shared.feature.transaction.ui.util.toColor
import com.fintrack.shared.feature.transaction.ui.util.toIcon
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// TODO: Improve state handling in these composable; reduce the no of LaunchedEffects and move some states & validation logic to ViewModel
@OptIn(ExperimentalTime::class)
@Composable
fun BudgetDetailScreen(
    budgetId: String?,
    viewModel: BudgetViewModel = koinViewModel(),
    accountsViewModel: AccountsViewModel = koinViewModel(),
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val selectedBudgetResult by viewModel.selectedBudget.collectAsStateWithLifecycle()
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()
    val accountsResult by accountsViewModel.accounts.collectAsStateWithLifecycle()

    val formState = remember(budgetId, selectedBudgetResult, accountsResult) {
        computeInitialFormState(budgetId, selectedBudgetResult, accountsResult)
    }

    // For showing validation errors
    var showValidationError by remember { mutableStateOf(false) }
    var validationErrorMessage by remember { mutableStateOf("") }

    LaunchedEffect(budgetId) {
        budgetId?.let { viewModel.loadBudgetById(it) }
    }

    // Handle save result
    LaunchedEffect(saveState) {
        if (saveState is SaveState.Success) {
            onSave()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 56.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                budgetId != null && selectedBudgetResult is Result.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                }

                budgetId != null && selectedBudgetResult is Result.Error -> {
                    Text(
                        text = "Failed to load budget",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                else -> {
                    AccountSelectionSection(
                        accountsResult = accountsResult,
                        selectedAccount = formState.selectedAccount,
                        onAccountSelected = { account ->
                            formState.selectedAccount = account
                        },
                        onRetry = {
                            accountsViewModel.reloadAccounts()
                        }
                    )

                    BudgetForm(
                        name = formState.name,
                        onNameChange = { formState.name = it },
                        amount = formState.amount,
                        onAmountChange = { formState.amount = it },
                        isExpense = formState.isExpense,
                        onExpenseChange = { formState.isExpense = it },
                        selectedCategories = formState.selectedCategories,
                        onCategoryChange = { formState.selectedCategories = it },
                        startDate = formState.startDate,
                        endDate = formState.endDate,
                        onPeriodChange = {
                            formState.startDate = it.first; formState.endDate = it.second
                        }
                    )
                }
            }
        }

        if (saveState is SaveState.Error) {
            MaterialToast(
                message = (saveState as SaveState.Error).exception.message
                    ?: "Failed to save budget",
                isError = true,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        if (showValidationError) {
            MaterialToast(
                message = validationErrorMessage,
                isError = true,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        // --- Save FAB ---
        BudgetDetailSaveButton(
            saveState = saveState,
            onSaveClick = {
                val validationResult = validateForm(formState)
                if (validationResult.isValid) {
                    viewModel.saveBudget(
                        id = budgetId,
                        name = formState.name,
                        categories = formState.selectedCategories.toList(),
                        limit = formState.amount.toDoubleOrNull() ?: 0.0,
                        isExpense = formState.isExpense,
                        startDate = formState.startDate!!,
                        endDate = formState.endDate!!,
                        accountId = formState.selectedAccount!!.id
                    )
                } else {
                    validationErrorMessage = validationResult.errorMessage
                    showValidationError = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

data class BudgetFormState(
    var name: String,
    var amount: String,
    var selectedCategories: Set<Category>,
    var isExpense: Boolean,
    var startDate: LocalDate?,
    var endDate: LocalDate?,
    var selectedAccount: Account?
)

@OptIn(ExperimentalTime::class)
private fun computeInitialFormState(
    budgetId: String?,
    selectedBudgetResult: Result<BudgetWithStatus>?,
    accountsResult: Result<List<Account>>
): BudgetFormState {
    return if (budgetId == null) {
        // New budget - default values
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        BudgetFormState(
            name = "",
            amount = "",
            selectedCategories = if (Category.expenseCategories.isNotEmpty()) {
                setOf(Category.expenseCategories[0])
            } else {
                emptySet()
            },
            isExpense = true,
            startDate = today,
            endDate = today.plus(DatePeriod(months = 1)),
            selectedAccount = if (accountsResult is Result.Success && accountsResult.data.isNotEmpty()) {
                accountsResult.data.first()
            } else {
                null
            }
        )
    } else {
        // Existing budget - handle different loading states
        when (selectedBudgetResult) {
            is Result.Success -> {
                val budgetWithStatus = selectedBudgetResult.data
                val budget = budgetWithStatus.budget
                BudgetFormState(
                    name = budget.name,
                    amount = budget.limit.toString(),
                    selectedCategories = budget.categories.toSet(),
                    isExpense = budget.isExpense,
                    startDate = budget.startDate,
                    endDate = budget.endDate,
                    selectedAccount = if (accountsResult is Result.Success) {
                        accountsResult.data.firstOrNull { it.id == budget.accountId }
                    } else {
                        null
                    }
                )
            }
            is Result.Error, is Result.Loading, null -> {
                // Loading or error state - return empty form
                BudgetFormState(
                    name = "",
                    amount = "",
                    selectedCategories = emptySet(),
                    isExpense = true,
                    startDate = null,
                    endDate = null,
                    selectedAccount = null
                )
            }
        }
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String = ""
)

private fun validateForm(formState: BudgetFormState): ValidationResult {
    val limit = formState.amount.toDoubleOrNull() ?: 0.0
    val errors = mutableListOf<String>()

    if (formState.name.isBlank()) {
        errors.add("Budget name is required")
    }
    if (formState.selectedCategories.isEmpty()) {
        errors.add("At least one category is required")
    }
    if (limit <= 0) {
        errors.add("Valid amount is required")
    }
    if (formState.startDate == null) {
        errors.add("Start date is required")
    }
    if (formState.endDate == null) {
        errors.add("End date is required")
    }
    if (formState.selectedAccount == null) {
        errors.add("Please select an account for this budget")
    }

    return if (errors.isEmpty()) {
        ValidationResult(isValid = true)
    } else {
        ValidationResult(
            isValid = false,
            errorMessage = errors.joinToString("\n")
        )
    }
}

@Composable
fun AccountSelectionSection(
    accountsResult: Result<List<Account>>,
    selectedAccount: Account?,
    onAccountSelected: (Account) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Select Account",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )

        when (accountsResult) {
            is Result.Loading -> {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            is Result.Error -> {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Failed to load accounts",
                            color = Color.Red,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accountChipBorder
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            is Result.Success -> {
                if (accountsResult.data.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBackground),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        ) {
                            Text(
                                text = "No accounts available. Create an account first.",
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(accountsResult.data) { account ->
                            AccountChip(
                                account = account,
                                isSelected = selectedAccount?.id == account.id,
                                onClick = { onAccountSelected(account) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountChip(
    account: Account,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accountIcon = AccountIcon.fromAccountName(account.name)

    Card(
        modifier = modifier
            .clickable { onClick() }
            .widthIn(min = 120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                accountChipSelectedBg
            } else {
                cardBackground
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, accountChipBorder)
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = accountIcon.icon,
                contentDescription = account.name,
                tint = accountIcon.color,
                modifier = Modifier.size(24.dp)
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = account.name,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val balanceText = when {
                account.balance == null -> "Ksh --"
                else -> {
                    val balanceValue = account.balance
                    val integerPart = balanceValue.toLong()
                    val decimalPart = ((balanceValue - integerPart) * 100).toInt()
                    "Ksh $integerPart.${decimalPart.toString().padStart(2, '0')}"
                }
            }

            Text(
                text = balanceText,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun BudgetDetailSaveButton(
    saveState: SaveState,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLoading = saveState is SaveState.Loading

    FloatingActionButton(
        onClick = {
            if (!isLoading) {
                onSaveClick()
            }
        },
        modifier = modifier,
        containerColor = when (saveState) {
            is SaveState.Success -> Color.Green
            else -> MaterialTheme.colorScheme.primary
        }
    ) {
        when (saveState) {
            is SaveState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            }

            is SaveState.Success -> {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Saved",
                    modifier = Modifier.size(24.dp)
                )
            }

            else -> {
                Icon(Icons.Default.Check, contentDescription = "Save")
            }
        }
    }
}

@Composable
fun BudgetForm(
    name: String,
    onNameChange: (String) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    isExpense: Boolean,
    onExpenseChange: (Boolean) -> Unit,
    selectedCategories: Set<Category>,
    onCategoryChange: (Set<Category>) -> Unit,
    startDate: LocalDate?,
    endDate: LocalDate?,
    onPeriodChange: (Pair<LocalDate?, LocalDate?>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // --- Name ---
        Text("Budget Name", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = name,
                onValueChange = onNameChange,
                placeholder = { Text("Enter budget name") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = cardBackground,
                    unfocusedContainerColor = cardBackground,
                    disabledContainerColor = cardBackground,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
        }

        // --- Amount ---
        Text("Limit", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = amount,
                onValueChange = onAmountChange,
                placeholder = { Text("Enter amount") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = {
                    Text("Ksh", color = currencyTextColor, fontWeight = FontWeight.SemiBold)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = cardBackground,
                    unfocusedContainerColor = cardBackground,
                    disabledContainerColor = cardBackground,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
        }

        // --- Expense / Income ---
        Text("Budget Type", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(12.dp)
            ) {
                ToggleChip(
                    text = "Expense",
                    icon = Icons.Default.ArrowDownward,
                    selected = isExpense,
                    onClick = { onExpenseChange(true) },
                    color = PinkExpense
                )
                ToggleChip(
                    text = "Income",
                    icon = Icons.Default.ArrowUpward,
                    selected = !isExpense,
                    onClick = { onExpenseChange(false) },
                    color = incomeButtonColor
                )
            }
        }

        // --- Categories ---
        Text("Categories", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
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
                    if (isExpense) Category.expenseCategories else Category.incomeCategories

                // --- Add "All" chip at the start ---
                item {
                    val allSelected = selectedCategories.containsAll(categories)
                    CategoryChip(
                        text = "All",
                        icon = Icons.Default.SelectAll,
                        color = Color.Gray,
                        selected = allSelected,
                        onClick = {
                            val newSelection = if (allSelected) {
                                emptySet()
                            } else {
                                categories.toSet()
                            }
                            onCategoryChange(newSelection)
                        }
                    )
                }

                // --- Category chips ---
                items(categories.size) { index ->
                    val cat = categories[index]
                    val selected = selectedCategories.contains(cat)
                    CategoryChip(
                        text = cat.name,
                        icon = cat.toIcon(),
                        color = cat.toColor(),
                        selected = selected,
                        onClick = {
                            val newSelection =
                                if (selected) selectedCategories - cat else selectedCategories + cat
                            onCategoryChange(newSelection)
                        }
                    )
                }
            }
        }

        // --- Period Picker ---
        Text("Period", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                PeriodPicker(
                    startDate = startDate,
                    endDate = endDate,
                    onPeriodChange = onPeriodChange
                )
            }
        }
    }
}

@Composable
fun PeriodPicker(
    startDate: LocalDate?,
    endDate: LocalDate?,
    onPeriodChange: (Pair<LocalDate?, LocalDate?>) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DateField(
            label = "Start",
            date = startDate,
            onDateSelected = { onPeriodChange(it to endDate) }
        )
        DateField(
            label = "End",
            date = endDate,
            onDateSelected = { onPeriodChange(startDate to it) }
        )
    }
}

@Composable
private fun DateField(
    label: String,
    date: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val formatted = date?.formatAsShortDateWithYear() ?: "Select"

    Box {
        OutlinedCard(
            modifier = Modifier
                .widthIn(min = 120.dp)
                .height(64.dp)
                .clickable { expanded = true },
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.outlinedCardColors()
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(label, fontSize = 12.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray)
                    Spacer(Modifier.width(6.dp))
                    Text(formatted, fontWeight = FontWeight.Medium)
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            val today = LocalDate(2025, 1, 1)
            var year by remember { mutableStateOf(date?.year ?: today.year) }
            var month by remember { mutableStateOf(date?.monthNumber ?: today.monthNumber) }
            var day by remember { mutableStateOf(date?.dayOfMonth ?: today.dayOfMonth) }

            Column(Modifier.padding(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberSelector("Year", year, 2000..2100) { year = it }
                    NumberSelector("Month", month, 1..12) { month = it }
                    NumberSelector("Day", day, 1..31) { day = it }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        expanded = false
                        onDateSelected(LocalDate(year, month, day))
                    }
                ) { Text("Select") }
            }
        }
    }
}

@Composable
private fun NumberSelector(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = {
                val new = if (value > range.first) value - 1 else range.last
                onValueChange(new)
            }) { Text("-") }

            Text(value.toString(), fontWeight = FontWeight.Medium)

            IconButton(onClick = {
                val new = if (value < range.last) value + 1 else range.first
                onValueChange(new)
            }) { Text("+") }
        }
    }
}
