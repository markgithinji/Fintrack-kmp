package com.fintrack.shared.feature.account.ui


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.core.Result
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AccountsTestScreen(
    viewModel: AccountsViewModel = koinViewModel()
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
