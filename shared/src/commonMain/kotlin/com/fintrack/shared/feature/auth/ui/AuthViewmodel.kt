package com.fintrack.shared.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.auth.domain.datasource.TokenDataSource
import com.fintrack.shared.feature.auth.domain.model.AuthResponse
import com.fintrack.shared.feature.auth.domain.model.AuthState
import com.fintrack.shared.feature.auth.domain.model.LoginFormState
import com.fintrack.shared.feature.auth.domain.model.RegisterFormState
import com.fintrack.shared.feature.core.domain.ValidationResult
import com.fintrack.shared.feature.auth.domain.repository.AuthRepository
import com.fintrack.shared.feature.auth.domain.usecase.LoginValidationUseCase
import com.fintrack.shared.feature.auth.domain.usecase.RegisterValidationUseCase
import com.fintrack.shared.feature.core.logger.KMPLogger
import com.fintrack.shared.feature.core.logger.LogTags
import com.fintrack.shared.feature.core.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository,
    private val tokenDataSource: TokenDataSource,
    private val registerValidationUseCase: RegisterValidationUseCase,
    private val loginValidationUseCase: LoginValidationUseCase,
    private val logger: KMPLogger
) : ViewModel() {

    private val _loginState = MutableStateFlow<AuthState<AuthResponse>>(AuthState.Idle)
    val loginState: StateFlow<AuthState<AuthResponse>> = _loginState

    private val _registerState = MutableStateFlow<AuthState<AuthResponse>>(AuthState.Idle)
    val registerState: StateFlow<AuthState<AuthResponse>> = _registerState

    private val _authStatus =
        MutableStateFlow<AuthState<Boolean>>(AuthState.Loading("Checking authentication..."))
    val authStatus: StateFlow<AuthState<Boolean>> = _authStatus

    private val _registerFormState = MutableStateFlow(RegisterFormState())
    val registerFormState: StateFlow<RegisterFormState> = _registerFormState

    private val _loginFormState = MutableStateFlow(LoginFormState())
    val loginFormState: StateFlow<LoginFormState> = _loginFormState

    val token: StateFlow<String?> = tokenDataSource.accessToken
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        checkAuthenticationStatus()
    }

    fun updateLoginEmail(email: String) {
        val currentState = _loginFormState.value
        val emailError = when (val result = loginValidationUseCase.validateEmail(email)) {
            is ValidationResult.Error -> result.message
            is ValidationResult.Success -> null
        }

        _loginFormState.value = currentState.copy(
            email = email,
            emailError = emailError,
            isFormValid = loginValidationUseCase.validateForm(email, currentState.password)
        )
    }

    fun updateLoginPassword(password: String) {
        val currentState = _loginFormState.value
        val passwordError = when (val result = loginValidationUseCase.validatePassword(password)) {
            is ValidationResult.Error -> result.message
            is ValidationResult.Success -> null
        }

        _loginFormState.value = currentState.copy(
            password = password,
            passwordError = passwordError,
            isFormValid = loginValidationUseCase.validateForm(currentState.email, password)
        )
    }

    fun login() {
        val formState = _loginFormState.value
        if (!formState.isFormValid) return

        _loginState.value = AuthState.Loading("Logging in...")
        viewModelScope.launch {
            logger.debug(LogTags.AUTH, "Attempting login for email: ${formState.email}")
            when (val result = repository.login(formState.email, formState.password)) {
                is Result.Success -> {
                    logger.info(LogTags.AUTH, "Login successful for: ${formState.email}")
                    _loginState.value = AuthState.Success(result.data)
                    _authStatus.value = AuthState.Success(true)
                }

                is Result.Error -> {
                    logger.error(LogTags.AUTH, "Login failed for ${formState.email}: ${result.exception.message}", result.exception)
                    _loginState.value = AuthState.Error(result.exception)
                }

                is Result.Loading -> {
                    _loginState.value = AuthState.Loading("Connecting...")
                }
            }
        }
    }

    fun updateName(name: String) {
        val currentState = _registerFormState.value
        val nameError = when (val result = registerValidationUseCase.validateName(name)) {
            is ValidationResult.Error -> result.message
            is ValidationResult.Success -> null
        }

        _registerFormState.value = currentState.copy(
            name = name,
            nameError = nameError,
            isFormValid = registerValidationUseCase.validateForm(
                name = name,
                email = currentState.email,
                password = currentState.password,
                confirmPassword = currentState.confirmPassword
            )
        )
    }

    fun updateEmail(email: String) {
        val currentState = _registerFormState.value
        val emailError = when (val result = registerValidationUseCase.validateEmail(email)) {
            is ValidationResult.Error -> result.message
            is ValidationResult.Success -> null
        }

        _registerFormState.value = currentState.copy(
            email = email,
            emailError = emailError,
            isFormValid = registerValidationUseCase.validateForm(
                name = currentState.name,
                email = email,
                password = currentState.password,
                confirmPassword = currentState.confirmPassword
            )
        )
    }

    fun updatePassword(password: String) {
        val currentState = _registerFormState.value
        val passwordError =
            when (val result = registerValidationUseCase.validatePassword(password)) {
                is ValidationResult.Error -> result.message
                is ValidationResult.Success -> null
            }
        val confirmPasswordError =
            when (val result = registerValidationUseCase.validateConfirmPassword(
                password,
                currentState.confirmPassword
            )) {
                is ValidationResult.Error -> result.message
                is ValidationResult.Success -> null
            }
        val passwordStrength = registerValidationUseCase.calculatePasswordStrength(password)

        _registerFormState.value = currentState.copy(
            password = password,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError,
            passwordStrength = passwordStrength,
            isFormValid = registerValidationUseCase.validateForm(
                name = currentState.name,
                email = currentState.email,
                password = password,
                confirmPassword = currentState.confirmPassword
            )
        )
    }

    fun updateConfirmPassword(confirmPassword: String) {
        val currentState = _registerFormState.value
        val confirmPasswordError =
            when (val result = registerValidationUseCase.validateConfirmPassword(
                currentState.password,
                confirmPassword
            )) {
                is ValidationResult.Error -> result.message
                is ValidationResult.Success -> null
            }

        _registerFormState.value = currentState.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = confirmPasswordError,
            isFormValid = registerValidationUseCase.validateForm(
                name = currentState.name,
                email = currentState.email,
                password = currentState.password,
                confirmPassword = confirmPassword
            )
        )
    }

    fun register() {
        val formState = _registerFormState.value
        if (!formState.isFormValid) return

        _registerState.value = AuthState.Loading("Creating account...")
        viewModelScope.launch {
            logger.debug(LogTags.AUTH, "Attempting registration for email: ${formState.email}")
            when (val result =
                repository.register(formState.name, formState.email, formState.password)) {
                is Result.Success -> {
                    logger.info(LogTags.AUTH, "Registration successful for: ${formState.email}")
                    _registerState.value = AuthState.Success(result.data)
                    _authStatus.value = AuthState.Success(true)
                }

                is Result.Error -> {
                    logger.error(LogTags.AUTH, "Registration failed for ${formState.email}: ${result.exception.message}", result.exception)
                    _registerState.value = AuthState.Error(result.exception)
                }

                is Result.Loading -> {
                    _registerState.value = AuthState.Loading("Processing...")
                }
            }
        }
    }

    fun checkAuthenticationStatus() {
        viewModelScope.launch {
            _authStatus.value = AuthState.Loading("Checking authentication...")
            val currentToken = tokenDataSource.accessToken.first()

            if (currentToken == null) {
                _authStatus.value = AuthState.Success(false)
                return@launch
            }

            when (val result = repository.validateToken(currentToken)) {
                is Result.Success -> {
                    _authStatus.value = if (result.data) {
                        AuthState.Success(true)
                    } else {
                        // Clear invalid token
                        // tokenDataSource.clearTokens() // Disabled for testing backend invalidation
                        AuthState.Success(false)
                    }
                }

                is Result.Error -> {
                    // On network error, treat as unauthenticated to be safe
                    _authStatus.value = AuthState.Success(false)
                }

                is Result.Loading -> {
                    _authStatus.value = AuthState.Success(false)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logger.info(LogTags.AUTH, "Logging out...")
            when (val result = repository.logout()) {
                is Result.Success -> {
                    logger.info(LogTags.AUTH, "Logout successful")
                }
                is Result.Error -> {
                    logger.error(LogTags.AUTH, "Server logout failed, but clearing local session anyway: ${result.exception.message}", result.exception)
                    // We still clear local tokens even if server call fails
                    // tokenDataSource.clearTokens() // Disabled for testing backend invalidation
                }
                is Result.Loading -> {}
            }
            _authStatus.value = AuthState.Success(false)
            // Reset states
            _loginState.value = AuthState.Idle
            _registerState.value = AuthState.Idle
            _loginFormState.value = LoginFormState()
            _registerFormState.value = RegisterFormState()
        }
    }

    fun resetLoginState() {
        _loginState.value = AuthState.Idle
    }

    fun resetRegisterState() {
        _registerState.value = AuthState.Idle
    }
}