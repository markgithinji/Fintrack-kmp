package com.fintrack.shared.feature.budget.ui


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fintrack.shared.feature.transaction.data.Result
import com.fintrack.shared.feature.budget.domain.Budget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = viewModel(),
    onAddBudget: () -> Unit= {},
    onBudgetClick: (Budget) -> Unit
) {
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budgets") },
                actions = {
                    IconButton(onClick = onAddBudget) {
                        Icon(Icons.Default.Add, contentDescription = "Add Budget")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (budgets) {
                is Result.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                is Result.Error -> Text(
                    "Failed to load budgets",
                    modifier = Modifier.align(Alignment.Center)
                )

                is Result.Success -> {
                    val data = (budgets as Result.Success<List<Budget>>).data
                    if (data.isEmpty()) {
                        Text(
                            "No budgets yet. Add one to get started.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn {
                            items(data) { budget ->
                                BudgetItem(
                                    budget = budget,
                                    onClick = { onBudgetClick(budget) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetItem(budget: Budget, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(16.dp)) {
            // Name
            Text(budget.name, style = MaterialTheme.typography.titleMedium)

            // Limit
            Text("Limit: ${budget.limit}", style = MaterialTheme.typography.bodyMedium)

            // Expense/Income label
            Text(
                text = if (budget.isExpense) "Type: Expense" else "Type: Income",
                style = MaterialTheme.typography.bodyMedium
            )

            // Categories
            Text(
                "Categories: ${budget.categories.joinToString { it.name }}",
                style = MaterialTheme.typography.bodyMedium
            )

            // Period
            Text(
                "Period: ${budget.startDate} → ${budget.endDate}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


