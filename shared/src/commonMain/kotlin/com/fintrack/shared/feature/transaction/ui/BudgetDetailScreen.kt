package com.fintrack.shared.feature.transaction.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fintrack.shared.feature.transaction.data.Result
import com.fintrack.shared.feature.transaction.model.Budget
import com.fintrack.shared.feature.transaction.model.Category
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format

@Composable
fun BudgetDetailScreen(
    budgetId: Int? = null,
    viewModel: BudgetViewModel = viewModel(),
    onSave: () -> Unit,
    onBack: () -> Unit = {}
) {
    val selectedBudgetResult by viewModel.selectedBudget.collectAsStateWithLifecycle()
    val saveResult by viewModel.saveResult.collectAsStateWithLifecycle()

    // --- Form state ---
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf<Set<Category>>(emptySet()) }
    var isExpense by remember { mutableStateOf(true) }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }

    // Load budget if editing
    LaunchedEffect(budgetId) {
        if (budgetId != null) viewModel.loadBudgetById(budgetId)
    }

    // Prefill form
    LaunchedEffect(selectedBudgetResult) {
        val budget = (selectedBudgetResult as? Result.Success<Budget>)?.data
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
        // --- Scrollable form ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 56.dp, bottom = 80.dp), // leave space for top bar & FAB
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

//        // --- Fixed Top Bar ---
//        BudgetDetailTopBar(budgetId = budgetId, onBack = onBack)

        // --- Fixed Save Button ---
        BudgetDetailSaveButton(
            isSaving = saveResult is Result.Loading,
            onSaveClick = {
                val limit = amount.toDoubleOrNull() ?: 0.0
                val valid = name.isNotBlank() &&
                        selectedCategories.isNotEmpty() &&
                        limit > 0 &&
                        startDate != null &&
                        endDate != null

                if (valid) {
                    viewModel.saveBudget(
                        id = budgetId,
                        name = name,
                        categories = selectedCategories.toList(),
                        limit = limit,
                        isExpense = isExpense,
                        startDate = startDate!!,
                        endDate = endDate!!
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
    // Name & Amount
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("Budget Name") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = amount,
        onValueChange = onAmountChange,
        label = { Text("Amount (Ksh)") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
    Spacer(Modifier.height(16.dp))

    // Expense / Income
    Text("Budget Type", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    Row {
        FilterChip(selected = isExpense, onClick = { onExpenseChange(true) }, label = { Text("Expense") })
        Spacer(Modifier.width(8.dp))
        FilterChip(selected = !isExpense, onClick = { onExpenseChange(false) }, label = { Text("Income") })
    }
    Spacer(Modifier.height(16.dp))

    // Categories
    Text("Categories", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val availableCategories = if (isExpense) Category.expenseCategories else Category.incomeCategories
        val allSelected = selectedCategories.containsAll(availableCategories)

        // "All" chip
        FilterChip(
            selected = allSelected,
            onClick = {
                onCategoryChange(
                    if (allSelected) emptySet() else availableCategories.toSet()
                )
            },
            label = { Text("All") },
            leadingIcon = { Icon(Icons.Default.SelectAll, contentDescription = null) }
        )

        // Individual category chips
        availableCategories.forEach { category ->
            val isSelected = category in selectedCategories
            FilterChip(
                selected = isSelected,
                onClick = {
                    val newSelection = if (isSelected) selectedCategories - category else selectedCategories + category
                    onCategoryChange(newSelection)
                },
                label = { Text(category.name) },
                leadingIcon = { Icon(category.toIcon(), contentDescription = null, tint = category.toColor()) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = category.toColor().copy(alpha = 0.2f),
                    selectedLeadingIconColor = category.toColor(),
                    selectedLabelColor = Color.Black
                )
            )
        }
    }
    Spacer(Modifier.height(16.dp))

    // Period Picker
    PeriodPicker(
        startDate = startDate,
        endDate = endDate,
        onPeriodChange = onPeriodChange
    )
}


@Composable
fun PeriodPicker(
    startDate: LocalDate?,
    endDate: LocalDate?,
    onPeriodChange: (Pair<LocalDate?, LocalDate?>) -> Unit
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }


    Column {
        // Start Date Button
        Button(
            onClick = { showStartPicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(text = startDate?.toFormattedString() ?: "Select Start Date")
        }
        Spacer(Modifier.height(8.dp))

        // End Date Button
        Button(
            onClick = { showEndPicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null)
            Spacer(Modifier.width(8.dp))

            Text(text = endDate?.toFormattedString() ?: "Select End Date")
        }
    }

    // Show native pickers conditionally
    if (showStartPicker) {
        PickDate(
            initialDate = startDate,
            onDateSelected = { date ->
                onPeriodChange(date to endDate)
                showStartPicker = false
            },
            onDismiss = { showStartPicker = false }
        )
    }

    if (showEndPicker) {
        PickDate(
            initialDate = endDate,
            onDateSelected = { date ->
                onPeriodChange(startDate to date)
                showEndPicker = false
            },
            onDismiss = { showEndPicker = false }
        )
    }
}

fun LocalDate.toFormattedString(): String {
    // Simple custom formatting: MMM dd, yyyy
    val month = when (this.monthNumber) {
        1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"
        5 -> "May"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Aug"
        9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dec"
        else -> ""
    }
    return "$month ${this.dayOfMonth}, ${this.year}"
}
