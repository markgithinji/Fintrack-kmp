package com.fintrack.shared.feature.budget.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.compose.GreenIncome
import com.example.compose.PinkExpense
import com.example.compose.accountChipBorder
import com.example.compose.accountChipSelectedBg
import com.example.compose.currencyTextColor
import com.example.compose.incomeButtonColor
import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.account.ui.AccountsViewModel
import com.fintrack.shared.feature.budget.domain.model.Budget
import com.fintrack.shared.feature.budget.domain.model.BudgetFormState
import com.fintrack.shared.feature.budget.domain.model.BudgetWithStatus
import com.fintrack.shared.feature.core.domain.SaveState
import com.fintrack.shared.feature.core.domain.ValidationResult
import com.fintrack.shared.feature.core.ui.MaterialToast
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.core.util.formatAsShortDateWithYear
import com.fintrack.shared.feature.core.util.formatToCurrency
import com.fintrack.shared.feature.transaction.domain.model.Category
import com.fintrack.shared.feature.transaction.ui.addtransaction.CategoryChip
import com.fintrack.shared.feature.transaction.ui.addtransaction.PickDate
import com.fintrack.shared.feature.transaction.ui.addtransaction.TypeToggleButton
import com.fintrack.shared.feature.transaction.ui.home.AccountIcon
import com.fintrack.shared.feature.transaction.ui.util.toColor
import com.fintrack.shared.feature.transaction.ui.util.toIcon
import kotlinx.coroutines.delay
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// This screen handles creating and editing of budgets; that explains the multiple launched effect. Later we could break ti down to create and edit screens separately
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
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val validationState by viewModel.validationState.collectAsStateWithLifecycle()
    val accountsResult by accountsViewModel.accounts.collectAsStateWithLifecycle()

    // Compute initial form state
    val initialFormState = remember(budgetId, selectedBudgetResult, accountsResult) {
        computeInitialFormState(budgetId, selectedBudgetResult, accountsResult)
    }

    // Set initial form state once
    LaunchedEffect(initialFormState) {
        viewModel.setFormState(initialFormState)
    }

    // Load budget if needed
    LaunchedEffect(budgetId) {
        budgetId?.let { viewModel.loadBudgetById(it) }
    }

    // Handle save state
    LaunchedEffect(saveState) {
        if (saveState is SaveState.Success) {
            delay(1000)
            onSave()
            viewModel.resetSaveState()
        }
    }

    val themeColor by animateColorAsState(
        targetValue = if (formState.isExpense) PinkExpense else GreenIncome,
        animationSpec = tween(durationMillis = 500)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            budgetId != null && selectedBudgetResult is Result.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = themeColor)
                }
            }

            budgetId != null && selectedBudgetResult is Result.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Failed to load budget",
                        color = Color.Red
                    )
                }
            }

            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header with Amount
                    BudgetAmountHeader(
                        amount = formState.amount,
                        onAmountChange = { viewModel.setAmount(it) },
                        isExpense = formState.isExpense,
                        themeColor = themeColor
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        BudgetTypeSection(
                            isExpense = formState.isExpense,
                            onExpenseChange = { viewModel.setIsExpense(it) },
                            themeColor = themeColor
                        )

                        BudgetNameSection(
                            name = formState.name,
                            onNameChange = { viewModel.setName(it) }
                        )

                        AccountSelectionSection(
                            accountsResult = accountsResult,
                            selectedAccount = formState.selectedAccount,
                            onAccountSelected = { viewModel.setAccount(it) },
                            onRetry = { accountsViewModel.reloadAccounts() }
                        )

                        CategorySelectionSection(
                            isExpense = formState.isExpense,
                            selectedCategories = formState.selectedCategories,
                            onCategoryChange = { viewModel.setCategories(it) }
                        )

                        PeriodSelectionSection(
                            startDate = formState.startDate,
                            endDate = formState.endDate,
                            onPeriodChange = { start, end -> viewModel.setPeriod(start, end) }
                        )

                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Prominent Save Button at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp)
        ) {
            SaveBudgetButton(
                saveState = saveState,
                validationState = validationState,
                themeColor = themeColor,
                onSaveClick = { viewModel.saveBudget() }
            )
        }

        // Show validation error from save state if any
        if (saveState is SaveState.Error) {
            MaterialToast(
                message = (saveState as SaveState.Error).exception.message
                    ?: "Failed to save budget",
                isError = true,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun BudgetAmountHeader(
    amount: String,
    onAmountChange: (String) -> Unit,
    isExpense: Boolean,
    themeColor: Color
) {
    Surface(
        color = themeColor,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isExpense) "Expense Budget Limit" else "Income Target Limit",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .width(IntrinsicSize.Min)
            ) {
                Text(
                    text = "KSh",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp, end = 8.dp)
                )

                BasicTextField(
                    value = amount,
                    onValueChange = { newAmount ->
                        val filtered = newAmount.filter { it.isDigit() || it == '.' }
                        val dotCount = filtered.count { it == '.' }
                        if (dotCount <= 1) {
                            if (dotCount == 1) {
                                val parts = filtered.split('.')
                                if (parts[1].length <= 2) onAmountChange(filtered)
                            } else onAmountChange(filtered)
                        }
                    },
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Start
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(Color.White),
                    singleLine = true,
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .widthIn(min = 32.dp),
                    decorationBox = { innerTextField ->
                        Box {
                            if (amount.isEmpty()) {
                                Text(
                                    "0",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
private fun computeInitialFormState(
    budgetId: String?,
    selectedBudgetResult: Result<BudgetWithStatus>?,
    accountsResult: Result<List<Account>>
): BudgetFormState {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val isAccountsSuccess = accountsResult is Result.Success
    val accountsData = if (isAccountsSuccess) accountsResult.data else emptyList()
    val firstExpenseCategory = Category.expenseCategories.firstOrNull()

    return if (budgetId == null) {
        // New budget - default values
        BudgetFormState(
            name = "",
            amount = "",
            selectedCategories = if (firstExpenseCategory != null) {
                setOf(firstExpenseCategory)
            } else {
                emptySet()
            },
            isExpense = true,
            startDate = today,
            endDate = today.plus(DatePeriod(months = 1)),
            selectedAccount = accountsData.firstOrNull()
        )
    } else {
        // Existing budget
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
                    selectedAccount = if (isAccountsSuccess) {
                        accountsData.firstOrNull { it.id == budget.accountId }
                    } else {
                        null
                    }
                )
            }

            else -> {
                // Loading or error state
                BudgetFormState()
            }
        }
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
    val selectedAccountId by remember(selectedAccount) { mutableStateOf(selectedAccount?.id) }

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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
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
                                isSelected = selectedAccountId == account.id,
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
                    MaterialTheme.colorScheme.surface
                }
            ),
            border = BorderStroke(
                width = 2.dp,
                color = if (isSelected) accountChipBorder else Color.LightGray.copy(alpha = 0.3f)
            )
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

            Text(
                text = account.balance?.formatToCurrency() ?: "KSh --",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun SaveBudgetButton(
    saveState: SaveState<Budget>,
    validationState: ValidationResult,
    themeColor: Color,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFormValid = validationState is ValidationResult.Success
    val isInProgress = saveState is SaveState.Loading
    val isSuccess = saveState is SaveState.Success<*>

    Button(
        onClick = onSaveClick,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = themeColor,
            disabledContainerColor = themeColor.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
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
                Text("Save Budget", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun BudgetTypeSection(
    isExpense: Boolean,
    onExpenseChange: (Boolean) -> Unit,
    themeColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TypeToggleButton(
            text = "Expense",
            isSelected = isExpense,
            selectedColor = PinkExpense,
            modifier = Modifier.weight(1f),
            onClick = { onExpenseChange(true) }
        )
        TypeToggleButton(
            text = "Income",
            isSelected = !isExpense,
            selectedColor = GreenIncome,
            modifier = Modifier.weight(1f),
            onClick = { onExpenseChange(false) }
        )
    }
}

@Composable
fun BudgetNameSection(
    name: String,
    onNameChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Budget Name", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = name,
                onValueChange = onNameChange,
                placeholder = { Text("Enter budget name") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        Icons.Default.Edit,
                        null,
                        tint = Color.Gray
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
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
fun CategorySelectionSection(
    isExpense: Boolean,
    selectedCategories: Set<Category>,
    onCategoryChange: (Set<Category>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Categories", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
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

                item {
                    val allSelected = selectedCategories.size == categories.size
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
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun PeriodSelectionSection(
    startDate: LocalDate?,
    endDate: LocalDate?,
    onPeriodChange: (LocalDate?, LocalDate?) -> Unit
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Budget Period", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
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
                        .clickable { showStartPicker = true },
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("Start Date", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = startDate?.formatAsShortDateWithYear() ?: "Select",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Pick Start Date",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showEndPicker = true },
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("End Date", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = endDate?.formatAsShortDateWithYear() ?: "Select",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Pick End Date",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    if (showStartPicker) {
        PickDate(
            initialDate = startDate ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
            onDateSelected = {
                onPeriodChange(it, endDate)
                showStartPicker = false
            },
            onDismiss = { showStartPicker = false }
        )
    }

    if (showEndPicker) {
        PickDate(
            initialDate = endDate ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.plus(DatePeriod(months = 1)),
            onDateSelected = {
                onPeriodChange(startDate, it)
                showEndPicker = false
            },
            onDismiss = { showEndPicker = false }
        )
    }
}
