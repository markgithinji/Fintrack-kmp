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
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.autofill.ContentType
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
import com.example.compose.AuthGold
import com.example.compose.GreenIncome
import com.fintrack.shared.feature.auth.domain.model.AuthState
import com.fintrack.shared.feature.core.data.domain.ApiException
import com.fintrack.shared.feature.core.data.domain.getUserFriendlyMessage
import com.fintrack.shared.feature.core.ui.MaterialToast
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
fun RegisterScreen(
    viewModel: AuthViewModel = koinViewModel(),
    onRegisterSuccess: () -> Unit,
    onLogin: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val registerState by viewModel.registerState.collectAsStateWithLifecycle()
    val registerFormState by viewModel.registerFormState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val nameFocusRequester = remember { FocusRequester() }

    // Validation state
    var mostRecentValidationError by remember { mutableStateOf<String?>(null) }
    
    // Track which fields have been interacted with to avoid showing errors on initial load
    var nameTouched by remember { mutableStateOf(false) }
    var emailTouched by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }
    var confirmPasswordTouched by remember { mutableStateOf(false) }

    // Consolidate validation errors for single display
    val validationErrorMessage = remember(registerFormState, mostRecentValidationError) {
        val allErrors = listOfNotNull(
            registerFormState.nameError,
            registerFormState.emailError,
            registerFormState.passwordError,
            registerFormState.confirmPasswordError
        )
        
        if (allErrors.isEmpty()) {
            null
        } else if (allErrors.contains(mostRecentValidationError)) {
            mostRecentValidationError
        } else {
            allErrors.firstOrNull()
        }
    }

    // Update most recent error when form state errors change
    LaunchedEffect(registerFormState.nameError) { registerFormState.nameError?.let { mostRecentValidationError = it } }
    LaunchedEffect(registerFormState.emailError) { registerFormState.emailError?.let { mostRecentValidationError = it } }
    LaunchedEffect(registerFormState.passwordError) { registerFormState.passwordError?.let { mostRecentValidationError = it } }
    LaunchedEffect(registerFormState.confirmPasswordError) { registerFormState.confirmPasswordError?.let { mostRecentValidationError = it } }

    // Toast message state (for server errors)
    var toastMessage by remember { mutableStateOf<String?>(null) }

    var passwordVisible by remember { mutableStateOf(value = false) }
    var confirmPasswordVisible by remember { mutableStateOf(value = false) }

    val colorScheme = MaterialTheme.colorScheme

    // Automatically request focus on the name field when the screen opens
    LaunchedEffect(Unit) {
        delay(300)
        try {
            nameFocusRequester.requestFocus()
        } catch (e: Exception) {
            println("LOGIN_DEBUG: Failed to request focus in Register: ${e.message}")
        }
    }

    LaunchedEffect(registerState) {
        when (val state = registerState) {
            is AuthState.Success -> {
                println("REGISTER_DEBUG: Registration successful, delaying 1s then navigating")
                delay(1000)
                onRegisterSuccess()
            }

            is AuthState.Error -> {
                val exception = state.exception
                toastMessage = (exception as? ApiException)?.getUserFriendlyMessage()
                    ?: exception.message ?: "Registration failed. Please try again."
            }

            else -> {
                toastMessage = null
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(colorScheme.background)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.fintrack_app_icon),
                        contentDescription = "FinTrack App",
                        modifier = Modifier
                            .size(64.dp)
                            .padding(4.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Join FinTrack to start managing your finances smarter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 2. Input Fields
            FinanceTextField(
                value = registerFormState.name,
                onValueChange = { viewModel.updateName(it) },
                label = "Full Name",
                leadingIcon = Icons.Default.Person,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                colorScheme = colorScheme,
                isError = registerFormState.nameError != null,
                errorMessage = null, // Shown consolidated below
                onFocusChanged = { isFocused ->
                    if (isFocused) nameTouched = true
                    if (!isFocused && nameTouched) viewModel.validateName()
                },
                contentType = ContentType.PersonFullName,
                modifier = Modifier.focusRequester(nameFocusRequester)
            )

            Spacer(modifier = Modifier.height(16.dp))

            FinanceTextField(
                value = registerFormState.email,
                onValueChange = { viewModel.updateEmail(it) },
                label = "Email Address",
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                colorScheme = colorScheme,
                isError = registerFormState.emailError != null,
                errorMessage = null, // Shown consolidated below
                onFocusChanged = { isFocused ->
                    if (isFocused) emailTouched = true
                    if (!isFocused && emailTouched) viewModel.validateEmail()
                },
                contentType = ContentType.EmailAddress
            )

            Spacer(modifier = Modifier.height(16.dp))

            FinanceTextField(
                value = registerFormState.password,
                onValueChange = { viewModel.updatePassword(it) },
                label = "Password",
                leadingIcon = Icons.Default.Lock,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
                isPassword = true,
                passwordVisible = passwordVisible,
                onPasswordToggle = { passwordVisible = !passwordVisible },
                colorScheme = colorScheme,
                isError = registerFormState.passwordError != null,
                errorMessage = null, // Shown consolidated below
                onFocusChanged = { isFocused ->
                    if (isFocused) passwordTouched = true
                    if (!isFocused && passwordTouched) viewModel.validatePassword()
                },
                contentType = ContentType.NewPassword
            )

            Spacer(modifier = Modifier.height(16.dp))

            FinanceTextField(
                value = registerFormState.confirmPassword,
                onValueChange = { viewModel.updateConfirmPassword(it) },
                label = "Confirm Password",
                leadingIcon = Icons.Default.Lock,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (registerFormState.isFormValid) {
                            viewModel.register()
                        } else {
                            viewModel.validateName()
                            viewModel.validateEmail()
                            viewModel.validatePassword()
                            viewModel.validateConfirmPassword()
                        }
                    }
                ),
                isPassword = true,
                passwordVisible = confirmPasswordVisible,
                onPasswordToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                colorScheme = colorScheme,
                isError = registerFormState.confirmPasswordError != null,
                errorMessage = null, // Shown consolidated below
                onFocusChanged = { isFocused ->
                    if (isFocused) confirmPasswordTouched = true
                    if (!isFocused && confirmPasswordTouched) viewModel.validateConfirmPassword()
                },
                contentType = ContentType.NewPassword
            )

            // 3. Inline Validation Error
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column {
                    AnimatedVisibility(
                        visible = validationErrorMessage != null,
                        enter = fadeIn() + slideInVertically { it / 2 },
                        exit = fadeOut()
                    ) {
                        Text(
                            text = validationErrorMessage ?: "",
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

            // 4. Register Button
            val isRegistering = registerState is AuthState.Loading
            val isSuccess = registerState is AuthState.Success<*>

            Button(
                onClick = {
                    focusManager.clearFocus()
                    // If form is not valid, trigger all validations to show the errors
                    if (!registerFormState.isFormValid) {
                        viewModel.validateName()
                        viewModel.validateEmail()
                        viewModel.validatePassword()
                        viewModel.validateConfirmPassword()
                    } else {
                        viewModel.register()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (registerFormState.isFormValid) 2.dp else 0.dp,
                    pressedElevation = 8.dp,
                    disabledElevation = 0.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary,
                    disabledContainerColor = if (isSuccess) AuthGold else colorScheme.primary.copy(alpha = 0.5f),
                    disabledContentColor = if (isSuccess) colorScheme.onSecondary else colorScheme.onPrimary.copy(alpha = 0.7f)
                ),
                enabled = !isRegistering && !isSuccess
            ) {
                when (registerState) {
                    is AuthState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp,
                            color = colorScheme.onPrimary
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
                            "Create Account",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 5. 'or' Separator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = colorScheme.outlineVariant
                )
                Text(
                    text = "Join with Social",
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

            // 6. Social Login Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SocialLoginButton(
                    text = "Google",
                    iconResource = Res.drawable.google_signIn_icon,
                    onClick = { /* Google Sign Up */ },
                    colorScheme = colorScheme,
                    modifier = Modifier.weight(1f)
                )
                SocialLoginButton(
                    text = "Apple",
                    iconResource = Res.drawable.apple_signIn_icon,
                    onClick = { /* Apple Sign Up */ },
                    colorScheme = colorScheme,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 7. Login Link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account?",
                    color = colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sign in",
                    color = colorScheme.secondary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.clickable { onLogin() }
                )
            }
        }

        toastMessage?.let { message ->
            MaterialToast(
                message = message,
                isError = true,
                onDismiss = { toastMessage = null },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            )
        }
    }
}
