package com.fintrack.shared.feature.budget.ui


import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.budget.domain.Budget
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = viewModel(),
    onAddBudget: () -> Unit = {},
    onBudgetClick: (Budget) -> Unit
) {
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when (budgets) {
            is Result.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

            is Result.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Failed to load budgets")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.reloadBudgets() }) {
                        Text("Retry")
                    }
                }
            }

            is Result.Success -> {
                val data = (budgets as Result.Success<List<Budget>>).data
                LazyColumn {
                    // Always show "+" at the top
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onAddBudget)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Budget"
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Add Budget")
                        }
                    }

                    if (data.isEmpty()) {
                        item {
                            Text(
                                "No budgets yet. Add one to get started.",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
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

@Composable
fun BudgetItem(budget: Budget, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header row: Name + Type chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = budget.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Box(
                    modifier = Modifier
                        .background(
                            color = if (budget.isExpense)
                                MaterialTheme.colorScheme.errorContainer
                            else
                                MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (budget.isExpense) "Expense" else "Income",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (budget.isExpense)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Limit
            Text(
                text = "Limit: ${budget.limit}",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )

            // Categories
            if (budget.categories.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    budget.categories.forEach { category ->
                        AssistChip(
                            onClick = {},
                            label = { Text(category.name) }
                        )
                    }
                }
            }

            // Period
            Text(
                text = "Period: ${budget.startDate} → ${budget.endDate}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
