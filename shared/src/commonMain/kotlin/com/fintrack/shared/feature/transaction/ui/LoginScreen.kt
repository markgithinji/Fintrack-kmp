package com.fintrack.shared.feature.transaction.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fintrack.shared.feature.transaction.data.AuthViewModel
import com.fintrack.shared.feature.transaction.data.HighlightsSummary
import com.fintrack.shared.feature.transaction.data.Result
import com.fintrack.shared.feature.transaction.data.User
import com.fintrack.shared.feature.transaction.model.Transaction
import kotlinx.datetime.LocalDate


@Composable
fun LoginScreen(
    viewModel: AuthViewModel = viewModel(),
    onLoginSuccess: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("mark") } // prefilled
    var password by remember { mutableStateOf("mypassword") }   // prefilled

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Login",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            when (val state = loginState) {
                is Result.Loading -> {
                    CircularProgressIndicator()
                }

                is Result.Error -> {
                    Text(
                        text = state.exception.message ?: "Unknown error",
                        color = Color.Red
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.login(email, password) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Retry")
                    }
                }

                is Result.Success -> {
                    // Call success callback once we get user
                    LaunchedEffect(state.data) {
                        onLoginSuccess(state.data)
                    }
                    Text("Login successful!", color = Color.Green)
                }

                null -> {
                    Button(
                        onClick = { viewModel.login(email, password) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Login")
                    }
                }
            }
        }
    }
}
