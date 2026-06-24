package com.fintrack.shared.feature.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.compose.GreenIncome
import com.fintrack.shared.feature.auth.domain.model.AuthState
import com.fintrack.shared.feature.core.data.domain.ApiException
import com.fintrack.shared.feature.core.data.domain.getUserFriendlyMessage
import com.fintrack.shared.feature.auth.ui.common.FinanceTextField
import com.fintrack.shared.feature.auth.ui.common.SocialLoginButton
import fintrack.shared.generated.resources.Res
import fintrack.shared.generated.resources.apple_signIn_icon
import fintrack.shared.generated.resources.fintrack_app_icon
import fintrack.shared.generated.resources.google_signIn_icon
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit,
    onSignUp: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    println("LOGIN_DEBUG: LoginScreen recomposing. ViewModel: ${viewModel.hashCode()}")
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()
    val loginFormState by viewModel.loginFormState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val emailFocusRequester = remember { FocusRequester() }

    // Inline error visibility
    var inlineErrorMessage by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(value = false) }

    val colorScheme = MaterialTheme.colorScheme

    // Automatically request focus on the email field when the screen opens
    LaunchedEffect(Unit) {
        delay(300) // Small delay to ensure UI is ready
        try {
            emailFocusRequester.requestFocus()
        } catch (e: Exception) {
            println("LOGIN_DEBUG: Failed to request focus: ${e.message}")
        }
    }

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is AuthState.Success -> {
                println("LOGIN_DEBUG: [4] Login successful in UI, delaying 1s then calling onLoginSuccess")
                delay(1000) // Delay to show the success state on the button
                println("LOGIN_DEBUG: [5] Calling onLoginSuccess()")
                onLoginSuccess()
            }

            is AuthState.Error -> {
                val exception = state.exception
                inlineErrorMessage = (exception as? ApiException)?.getUserFriendlyMessage()
                    ?: exception.message ?: "Login failed. Please try again."
            }

            else -> {
                inlineErrorMessage = null
            }
        }
    }

    // Consolidate validation and API errors
    val combinedErrorMessage = remember(loginFormState, inlineErrorMessage) {
        loginFormState.emailError ?: loginFormState.passwordError ?: inlineErrorMessage
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(colorScheme.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(64.dp))

        // 1. Logo and Title Area
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .shadow(4.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.fintrack_app_icon),
                    contentDescription = "FinTrack App",
                    modifier = Modifier
                        .size(64.dp)
                        .padding(4.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Securely access your financial insights",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // 2. Input Fields
        FinanceTextField(
            value = loginFormState.email,
            onValueChange = { 
                println("LOGIN_DEBUG: UI Email change to: '$it'")
                viewModel.updateLoginEmail(it) 
            },
            label = "Email Address",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            colorScheme = colorScheme,
            isError = loginFormState.emailError != null,
            errorMessage = null, // Consolidated in the error box
            contentType = ContentType.EmailAddress,
            modifier = Modifier.focusRequester(emailFocusRequester)
        )

        Spacer(modifier = Modifier.height(20.dp))

        FinanceTextField(
            value = loginFormState.password,
            onValueChange = { viewModel.updateLoginPassword(it) },
            label = "Password",
            leadingIcon = Icons.Default.Lock,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    if (loginFormState.isFormValid) viewModel.login()
                }
            ),
            isPassword = true,
            passwordVisible = passwordVisible,
            onPasswordToggle = { passwordVisible = !passwordVisible },
            colorScheme = colorScheme,
            isError = loginFormState.passwordError != null,
            errorMessage = null, // Consolidated in the error box
            contentType = ContentType.Password
        )

        // 3. Inline Error Message
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp), // Fixed height to prevent layout shift
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                AnimatedVisibility(
                    visible = combinedErrorMessage != null,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut()
                ) {
                    Text(
                        text = combinedErrorMessage ?: "",
                        color = colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Forgot Password
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "Forgot Password?",
                color = colorScheme.tertiary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onForgotPassword() }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 5. Login Button
        val isLoggingIn = loginState is AuthState.Loading
        val isSuccess = loginState is AuthState.Success<*>

        Button(
            onClick = {
                println("LOGIN_DEBUG: [0] Login button clicked for email: ${loginFormState.email}")
                focusManager.clearFocus()
                viewModel.login()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = if (loginFormState.isFormValid) 2.dp else 0.dp,
                pressedElevation = 8.dp,
                disabledElevation = 0.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary,
                disabledContainerColor = if (isSuccess) GreenIncome else colorScheme.primary.copy(alpha = 0.5f),
                disabledContentColor = if (isSuccess) Color.White else colorScheme.onPrimary.copy(alpha = 0.7f)
            ),
            enabled = loginFormState.isFormValid && !isLoggingIn && !isSuccess
        ) {
            when (loginState) {
                is AuthState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp,
                        color = colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                }

                is AuthState.Success -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Success",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                else -> {
                    Text(
                        "Sign In",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 6. 'or' Separator
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = colorScheme.outlineVariant
            )
            Text(
                text = "Secure Connect",
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.labelMedium
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = colorScheme.outlineVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 7. Social Login Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SocialLoginButton(
                text = "Google",
                iconResource = Res.drawable.google_signIn_icon,
                onClick = { /* Google Login */ },
                colorScheme = colorScheme,
                modifier = Modifier.weight(1f)
            )
            SocialLoginButton(
                text = "Apple",
                iconResource = Res.drawable.apple_signIn_icon,
                onClick = { /* Apple Login */ },
                colorScheme = colorScheme,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // 8. Sign Up Link
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "New to FinTrack?",
                color = colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Create Account",
                color = colorScheme.tertiary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.clickable { onSignUp() }
            )
        }
    }
}
