package com.fintrack.shared.feature.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.compose.GreenIncome
import com.fintrack.shared.feature.core.util.Result
import fintrack.shared.generated.resources.Res
import fintrack.shared.generated.resources.apple_signIn_icon
import fintrack.shared.generated.resources.fintrack_app_icon
import fintrack.shared.generated.resources.google_signIn_icon
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = koinViewModel(),
    onRegisterSuccess: () -> Unit,
    onLogin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val registerState by viewModel.registerState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Track if error dialog should be shown
    var showErrorDialog by remember { mutableStateOf(false) }
    var currentError by remember { mutableStateOf<String?>(null) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(registerState) {
        when (registerState) {
            is Result.Success -> {
                onRegisterSuccess()
            }

            is Result.Error -> {
                val errorState = registerState as Result.Error
                currentError =
                    errorState.exception.message ?: "Registration failed. Please try again."
                showErrorDialog = true
            }

            else -> {
                // TODO: Handle other states (Loading, null, etc.)
            }
        }
    }

    // Custom Error Dialog
    if (showErrorDialog && currentError != null) {
        ErrorDialog(
            errorMessage = currentError!!,
            onDismiss = {
                showErrorDialog = false
                currentError = null
            },
            onRetry = {
                showErrorDialog = false
                currentError = null
                viewModel.register(name, email, password)
            },
            colorScheme = colorScheme
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(colorScheme.background)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // 1. Logo and Title Area
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.fintrack_app_icon),
                    contentDescription = "FinTrack App",
                    modifier = Modifier.fillMaxSize()
                        .padding(12.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Join FinTrack to start managing your finances smarter.",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 2. Input Fields
        FinanceTextField(
            value = name,
            onValueChange = { name = it },
            label = "Full Name",
            leadingIcon = Icons.Default.Person,
            keyboardType = KeyboardType.Text,
            colorScheme = colorScheme
        )

        Spacer(modifier = Modifier.height(16.dp))

        FinanceTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email Address",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            colorScheme = colorScheme
        )

        Spacer(modifier = Modifier.height(16.dp))

        FinanceTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            leadingIcon = Icons.Default.Lock,
            keyboardType = KeyboardType.Password,
            isPassword = true,
            passwordVisible = passwordVisible,
            onPasswordToggle = { passwordVisible = !passwordVisible },
            colorScheme = colorScheme
        )

        Spacer(modifier = Modifier.height(16.dp))

        FinanceTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm Password",
            leadingIcon = Icons.Default.Lock,
            keyboardType = KeyboardType.Password,
            isPassword = true,
            passwordVisible = confirmPasswordVisible,
            onPasswordToggle = { confirmPasswordVisible = !confirmPasswordVisible },
            colorScheme = colorScheme
        )

        // Password validation feedback
        if (password.isNotEmpty() && confirmPassword.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            val passwordsMatch = password == confirmPassword
            val passwordStrength = getPasswordStrength(password)

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                if (!passwordsMatch) {
                    Text(
                        text = "Passwords do not match",
                        color = colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (password.isNotEmpty()) {
                    Text(
                        text = "Password strength: $passwordStrength",
                        color = when (passwordStrength) {
                            "Weak" -> colorScheme.error
                            "Medium" -> colorScheme.secondary
                            "Strong" -> GreenIncome
                            else -> colorScheme.onSurfaceVariant
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Register Button with state handling
        val isFormValid = name.isNotBlank() &&
                email.isNotBlank() &&
                password.isNotBlank() &&
                confirmPassword.isNotBlank() &&
                password == confirmPassword

        when (val state = registerState) {
            is Result.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(
                            color = colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Creating Account...",
                            color = colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            is Result.Success -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(
                            color = GreenIncome.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = GreenIncome,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Account created successfully!",
                            color = GreenIncome,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            else -> {
                Button(
                    onClick = {
                        if (isFormValid) {
                            viewModel.register(name, email, password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    ),
                    enabled = isFormValid
                ) {
                    Text("Create Account", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 4. 'or' Separator
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(
                modifier = Modifier.weight(1f),
                color = colorScheme.outline.copy(alpha = 0.3f)
            )
            Text(
                text = "or",
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
            Divider(
                modifier = Modifier.weight(1f),
                color = colorScheme.outline.copy(alpha = 0.3f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Social Login Buttons
        SocialLoginButton(
            text = "Sign up with Google",
            iconResource = Res.drawable.google_signIn_icon,
            onClick = { /* Google Sign Up */ },
            colorScheme = colorScheme
        )

        Spacer(modifier = Modifier.height(12.dp))

        SocialLoginButton(
            text = "Sign up with Apple",
            iconResource = Res.drawable.apple_signIn_icon,
            onClick = { /* Apple Sign Up */ },
            colorScheme = colorScheme
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 6. Login Link
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Already have an account?",
                color = colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Sign in",
                color = colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onLogin() }
            )
        }
    }
}

// Helper function for password strength
private fun getPasswordStrength(password: String): String {
    return when {
        password.length < 6 -> "Weak"
        password.length < 8 -> "Medium"
        password.any { it.isDigit() } && password.any { it.isLetter() } && password.any { !it.isLetterOrDigit() } -> "Strong"
        password.any { it.isDigit() } && password.any { it.isLetter() } -> "Medium"
        else -> "Weak"
    }
}