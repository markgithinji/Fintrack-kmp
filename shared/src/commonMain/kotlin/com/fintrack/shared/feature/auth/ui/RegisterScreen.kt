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
import com.fintrack.shared.feature.auth.domain.model.AuthState
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
    val registerFormState by viewModel.registerFormState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Track if error dialog should be shown
    var showErrorDialog by remember { mutableStateOf(false) }
    var currentError by remember { mutableStateOf<String?>(null) }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(registerState) {
        when (val state = registerState) {
            is AuthState.Success -> {
                onRegisterSuccess()
            }

            is AuthState.Error -> {
                currentError = state.exception.message ?: "Registration failed. Please try again."
                showErrorDialog = true
            }

            else -> {
                // Handle Idle, Loading states - nothing to do here
            }
        }
    }

    // Custom Error Dialog
    if (showErrorDialog) {
        currentError?.let { errorMessage ->
            ErrorDialog(
                errorMessage = errorMessage,
                onDismiss = {
                    showErrorDialog = false
                    currentError = null
                    viewModel.resetRegisterState()
                },
                onRetry = {
                    showErrorDialog = false
                    currentError = null
                    viewModel.register()
                },
                colorScheme = colorScheme
            )
        }
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

        // 2. Input Fields with validation
        FinanceTextField(
            value = registerFormState.name,
            onValueChange = { viewModel.updateName(it) },
            label = "Full Name",
            leadingIcon = Icons.Default.Person,
            keyboardType = KeyboardType.Text,
            colorScheme = colorScheme,
            isError = registerFormState.nameError != null,
            errorMessage = registerFormState.nameError
        )

        Spacer(modifier = Modifier.height(16.dp))

        FinanceTextField(
            value = registerFormState.email,
            onValueChange = { viewModel.updateEmail(it) },
            label = "Email Address",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            colorScheme = colorScheme,
            isError = registerFormState.emailError != null,
            errorMessage = registerFormState.emailError
        )

        Spacer(modifier = Modifier.height(16.dp))

        FinanceTextField(
            value = registerFormState.password,
            onValueChange = { viewModel.updatePassword(it) },
            label = "Password",
            leadingIcon = Icons.Default.Lock,
            keyboardType = KeyboardType.Password,
            isPassword = true,
            passwordVisible = passwordVisible,
            onPasswordToggle = { passwordVisible = !passwordVisible },
            colorScheme = colorScheme,
            isError = registerFormState.passwordError != null,
            errorMessage = registerFormState.passwordError
        )

        Spacer(modifier = Modifier.height(16.dp))

        FinanceTextField(
            value = registerFormState.confirmPassword,
            onValueChange = { viewModel.updateConfirmPassword(it) },
            label = "Confirm Password",
            leadingIcon = Icons.Default.Lock,
            keyboardType = KeyboardType.Password,
            isPassword = true,
            passwordVisible = confirmPasswordVisible,
            onPasswordToggle = { confirmPasswordVisible = !confirmPasswordVisible },
            colorScheme = colorScheme,
            isError = registerFormState.confirmPasswordError != null,
            errorMessage = registerFormState.confirmPasswordError
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Register Button with state handling
        when (val state = registerState) {
            is AuthState.Loading -> {
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
                        state.message.ifEmpty { "Creating Account..." }.let {
                            Text(
                                text = it,
                                color = colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            is AuthState.Success -> {
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

            is AuthState.Error -> {
                // Error is shown in dialog, so just show normal button
                Button(
                    onClick = {
                        if (registerFormState.isFormValid) {
                            viewModel.register()
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
                    enabled = registerFormState.isFormValid
                ) {
                    Text("Create Account", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }

            is AuthState.Idle -> {
                Button(
                    onClick = {
                        if (registerFormState.isFormValid) {
                            viewModel.register()
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
                    enabled = registerFormState.isFormValid
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