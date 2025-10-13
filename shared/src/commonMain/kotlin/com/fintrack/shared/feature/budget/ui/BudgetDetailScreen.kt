package com.fintrack.shared.feature.budget.ui

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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.shared.feature.account.ui.AccountsViewModel
import com.fintrack.shared.feature.budget.domain.model.BudgetWithStatus
import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.transaction.domain.model.Category
import com.fintrack.shared.feature.transaction.ui.addtransaction.CategoryChip
import com.fintrack.shared.feature.transaction.ui.addtransaction.ToggleChip
import com.fintrack.shared.feature.transaction.ui.util.toColor
import com.fintrack.shared.feature.transaction.ui.util.toIcon
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BudgetDetailScreen(
    budgetId: String? = null,
    accountId: String? = null,
    viewModel: BudgetViewModel = koinViewModel(),
    accountsViewModel: AccountsViewModel = koinViewModel(),
    onSave: () -> Unit,
    onBack: () -> Unit = {}
) {
    val selectedBudgetResult by viewModel.selectedBudget.collectAsStateWithLifecycle()
    val saveResult by viewModel.saveResult.collectAsStateWithLifecycle()
    val selectedAccountResult by accountsViewModel.selectedAccount.collectAsStateWithLifecycle()

    val effectiveAccountId = remember(accountId, selectedAccountResult) {
        accountId ?: (selectedAccountResult as? Result.Success)?.data?.id
    }

    // --- Form state ---
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf<Set<Category>>(emptySet()) }
    var isExpense by remember { mutableStateOf(true) }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }

    LaunchedEffect(budgetId, selectedBudgetResult) {
        // Load budget if we have an ID and no data yet
        if (budgetId != null && selectedBudgetResult == null) {
            viewModel.loadBudgetById(budgetId)
        }

        // Prefill form when budget is successfully loaded
        val budgetWithStatus = (selectedBudgetResult as? Result.Success<BudgetWithStatus>)?.data
        val budget = budgetWithStatus?.budget

        if (budget != null && name.isEmpty() && amount.isEmpty() && selectedCategories.isEmpty()) {
            name = budget.name
            amount = budget.limit.toString()
            selectedCategories = budget.categories.toSet()
            isExpense = budget.isExpense
            startDate = budget.startDate
            endDate = budget.endDate
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
            when (selectedBudgetResult) {
                is Result.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                }

                is Result.Error -> {
                    Text(
                        text = "Failed to load budget",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                is Result.Success, null -> {
                    BudgetForm(
                        name = name,
                        onNameChange = { name = it },
                        amount = amount,
                        onAmountChange = { amount = it },
                        isExpense = isExpense,
                        onExpenseChange = { isExpense = it },
                        selectedCategories = selectedCategories,
                        onCategoryChange = { selectedCategories = it },
                        startDate = startDate,
                        endDate = endDate,
                        onPeriodChange = { startDate = it.first; endDate = it.second }
                    )
                }
            }

            if (saveResult is Result.Error) {
                Text(
                    text = (saveResult as Result.Error).exception.message ?: "Failed to save",
                    color = Color.Red
                )
            }

            if (saveResult is Result.Success) {
                LaunchedEffect(saveResult) { onSave() }
            }
        }

        // --- Save FAB ---
        BudgetDetailSaveButton(
            isSaving = saveResult is Result.Loading,
            onSaveClick = {
                val limit = amount.toDoubleOrNull() ?: 0.0
                val valid = name.isNotBlank() &&
                        selectedCategories.isNotEmpty() &&
                        limit > 0 &&
                        startDate != null &&
                        endDate != null &&
                        effectiveAccountId != null

                if (valid) {
                    viewModel.saveBudget(
                        id = budgetId,
                        name = name,
                        categories = selectedCategories.toList(),
                        limit = limit,
                        isExpense = isExpense,
                        startDate = startDate!!,
                        endDate = endDate!!,
                        accountId = effectiveAccountId
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

@Composable
fun BudgetDetailSaveButton(
    isSaving: Boolean,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = { if (!isSaving) onSaveClick() },
        modifier = modifier
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Icon(Icons.Default.Check, contentDescription = "Save")
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
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
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
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    disabledContainerColor = Color(0xFFF5F5F5),
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
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
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
                    Text("Ksh", color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    disabledContainerColor = Color(0xFFF5F5F5),
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
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
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
                    color = Color.Red
                )
                ToggleChip(
                    text = "Income",
                    icon = Icons.Default.ArrowUpward,
                    selected = !isExpense,
                    onClick = { onExpenseChange(false) },
                    color = Color(0xFF2E7D32)
                )
            }
        }

        // --- Categories ---
        Text("Categories", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
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
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
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
    val formatted = date?.toFormattedString() ?: "Select"

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

fun LocalDate.toFormattedString(): String {
    val month = when (this.monthNumber) {
        1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"
        5 -> "May"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Aug"
        9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dec"
        else -> ""
    }
    return "$month ${this.dayOfMonth}, ${this.year}"
}

