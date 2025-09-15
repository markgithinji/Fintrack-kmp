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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetDetailScreen(
    budgetId: Int? = null,
    viewModel: BudgetViewModel = viewModel(),
    onSave: () -> Unit,
    onBack: () -> Unit = {}
) {
    val selectedBudget by viewModel.selectedBudget.collectAsStateWithLifecycle()

    // Form state
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf<Set<Category>>(emptySet()) }
    var isExpense by remember { mutableStateOf(true) }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }

    // Load the budget if editing
    LaunchedEffect(budgetId) {
        if (budgetId != null) {
            viewModel.loadBudgetById(budgetId)
        }
    }

    // Populate form once budget is loaded
    LaunchedEffect(selectedBudget) {
        if (selectedBudget is Result.Success) {
            (selectedBudget as Result.Success<Budget>).data.let { budget ->
                name = budget.name
                amount = budget.limit.toString()
                selectedCategories = budget.categories.toSet()
                isExpense = budget.isExpense
                startDate = budget.startDate
                endDate = budget.endDate
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (budgetId == null) "Add Budget" else "Edit Budget") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
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
                    onSave()
                }
            }) {
                Icon(Icons.Default.Check, contentDescription = "Save")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Budget Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Budget Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // Amount
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (Ksh)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(Modifier.height(16.dp))

            // Expense / Income toggle
            Text("Budget Type", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row {
                FilterChip(
                    selected = isExpense,
                    onClick = { isExpense = true },
                    label = { Text("Expense") }
                )
                Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = !isExpense,
                    onClick = { isExpense = false },
                    label = { Text("Income") }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Categories
            Text("Categories", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val availableCategories =
                    if (isExpense) Category.expenseCategories else Category.incomeCategories

                availableCategories.forEach { category ->
                    val isSelected = category in selectedCategories
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategories = if (isSelected) {
                                selectedCategories - category
                            } else {
                                selectedCategories + category
                            }
                        },
                        label = { Text(category.name) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Period
            Text("Period", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = startDate?.toString() ?: "",
                onValueChange = { startDate = runCatching { LocalDate.parse(it) }.getOrNull() },
                label = { Text("Start Date (yyyy-MM-dd)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = endDate?.toString() ?: "",
                onValueChange = { endDate = runCatching { LocalDate.parse(it) }.getOrNull() },
                label = { Text("End Date (yyyy-MM-dd)") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
