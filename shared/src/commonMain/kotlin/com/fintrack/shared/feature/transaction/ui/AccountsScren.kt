package com.fintrack.shared.feature.transaction.ui


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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import com.fintrack.shared.feature.transaction.data.Account
import com.fintrack.shared.feature.transaction.data.Result
import com.fintrack.shared.feature.transaction.model.Budget

@Composable
fun AccountsTestScreen(
    viewModel: AccountsViewModel = viewModel()
) {
    val accountsState by viewModel.accounts.collectAsStateWithLifecycle()

    // Reload button example
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "Accounts",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (accountsState) {
            is Result.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is Result.Success -> {
                val accounts = (accountsState as Result.Success<List<Account>>).data
                if (accounts.isEmpty()) {
                    Text("No accounts found")
                } else {
                    LazyColumn {
                        items(accounts) { account ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(account.name)
                                Text("Balance: ${account.balance ?: 0.0}")
                            }
                        }
                    }
                }
            }

            is Result.Error -> {
                val error = (accountsState as Result.Error).exception.message
                Text("Error loading accounts: $error", color = Color.Red)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.reloadAccounts() }) {
            Text("Reload Accounts")
        }
    }
}
