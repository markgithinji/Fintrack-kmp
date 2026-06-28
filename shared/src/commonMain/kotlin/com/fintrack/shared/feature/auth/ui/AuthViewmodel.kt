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
import com.fintrack.shared.feature.settings.domain.datasource.SettingsDataSource
import com.fintrack.shared.feature.settings.domain.util.BiometricAuthenticator
import com.fintrack.shared.feature.settings.domain.util.BiometricResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository,
    private val tokenDataSource: TokenDataSource,
    private val registerValidationUseCase: RegisterValidationUseCase,
    private val loginValidationUseCase: LoginValidationUseCase,
    private val settingsDataSource: SettingsDataSource,
    private val biometricAuthenticator: BiometricAuthenticator,
    private val logger: KMPLogger
) : ViewModel() {

    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _loginState = MutableStateFlow<AuthState<AuthResponse>>(AuthState.Idle)
    val loginState: StateFlow<AuthState<AuthResponse>> = _loginState

    private val _registerState = MutableStateFlow<AuthState<AuthResponse>>(AuthState.Idle)
    val registerState: StateFlow<AuthState<AuthResponse>> = _registerState

    private val _authStatus =
        MutableStateFlow<AuthState<Boolean>>(AuthState.Idle)
    val authStatus: StateFlow<AuthState<Boolean>> = _authStatus

    private val _registerFormState = MutableStateFlow(RegisterFormState())
    val registerFormState: StateFlow<RegisterFormState> = _registerFormState

    private val _loginFormState = MutableStateFlow(LoginFormState())
    val loginFormState: StateFlow<LoginFormState> = _loginFormState

    val token: StateFlow<String?> = tokenDataSource.accessToken
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        checkAuthenticationStatus()
        observeTokenChanges()
        checkAppLockStatus()
    }

    private fun checkAppLockStatus() {
        viewModelScope.launch {
            val isBiometricEnabled = settingsDataSource.isBiometricEnabled.first()
            _isAppLocked.value = isBiometricEnabled
        }
    }

    fun unlockWithBiometrics() {
        viewModelScope.launch {
            val result = biometricAuthenticator.authenticate(
                title = "Unlock Fintrack",
                subtitle = "Authenticate to access your account"
            )
            if (result is BiometricResult.Success) {
                _isAppLocked.value = false
            }
        }
    }

    private fun observeTokenChanges() {
        viewModelScope.launch {
            tokenDataSource.accessToken.collect { token ->
                logger.debug(LogTags.AUTH, "LOGIN_DEBUG: AuthViewModel(${hashCode()}): Token changed, new token present: ${token != null}")
                if (token == null) {
                    val currentStatus = _authStatus.value
                    if (currentStatus != AuthState.Success(false)) {
                        logger.info(LogTags.AUTH, "LOGIN_DEBUG: AuthViewModel(${hashCode()}): Token is null, setting status to Success(false)")
                        _authStatus.value = AuthState.Success(false)
                    }
                } else {
                    val currentStatus = _authStatus.value
                    if (currentStatus !is AuthState.Success || !currentStatus.data) {
                        logger.info(LogTags.AUTH, "LOGIN_DEBUG: AuthViewModel(${hashCode()}): Token detected, updating status to Success(true)")
                        _authStatus.value = AuthState.Success(true)
                    }
                }
            }
        }
    }

    fun updateLoginEmail(email: String) {
        println("LOGIN_DEBUG: AuthViewModel(${hashCode()}): updateLoginEmail called with: '$email'")
        val currentState = _loginFormState.value
        _loginFormState.value = currentState.copy(
            email = email,
            emailError = null, // Clear error when typing
            isFormValid = loginValidationUseCase.validateForm(email, currentState.password)
        )
    }

    fun validateLoginEmail() {
        val currentState = _loginFormState.value
        val emailError = when (val result = loginValidationUseCase.validateEmail(currentState.email)) {
            is ValidationResult.Error -> result.message
            is ValidationResult.Success -> null
        }
        _loginFormState.value = currentState.copy(emailError = emailError)
    }

    fun updateLoginPassword(password: String) {
        logger.debug(LogTags.AUTH, "LOGIN_DEBUG: AuthViewModel(${hashCode()}): updateLoginPassword")
        val currentState = _loginFormState.value
        _loginFormState.value = currentState.copy(
            password = password,
            passwordError = null, // Clear error when typing
            isFormValid = loginValidationUseCase.validateForm(currentState.email, password)
        )
    }

    fun validateLoginPassword() {
        val currentState = _loginFormState.value
        val passwordError = when (val result = loginValidationUseCase.validatePassword(currentState.password)) {
            is ValidationResult.Error -> result.message
            is ValidationResult.Success -> null
        }
        _loginFormState.value = currentState.copy(passwordError = passwordError)
    }

    fun login() {
        val formState = _loginFormState.value
        if (!formState.isFormValid) return

        _loginState.value = AuthState.Loading("Logging in...")
        viewModelScope.launch {
            logger.debug(LogTags.AUTH, "LOGIN_DEBUG: [1] AuthViewModel: Attempting login for email: ${formState.email}")
            when (val result = repository.login(formState.email, formState.password)) {
                is Result.Success -> {
                    logger.info(LogTags.AUTH, "LOGIN_DEBUG: [2] AuthViewModel: Login successful for: ${formState.email}")
                    // Add a small delay to ensure tokens are persisted and flows are updated
                    // before the UI navigates and triggers follow-up requests.
                    kotlinx.coroutines.delay(100)
                    _loginState.value = AuthState.Success(result.data)
                    logger.info(LogTags.AUTH, "LOGIN_DEBUG: [3] AuthViewModel: Setting _authStatus to Success(true)")
                    _authStatus.value = AuthState.Success(true)
                }

                is Result.Error -> {
                    logger.error(LogTags.AUTH, "LOGIN_DEBUG: [2] Login failed for ${formState.email}: ${result.exception.message}", result.exception)
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
        _registerFormState.value = currentState.copy(
            name = name,
            nameError = null, // Clear error when typing
            isFormValid = registerValidationUseCase.validateForm(
                name = name,
                email = currentState.email,
                password = currentState.password,
                confirmPassword = currentState.confirmPassword
            )
        )
    }

    fun validateName() {
        val currentState = _registerFormState.value
        val nameError = when (val result = registerValidationUseCase.validateName(currentState.name)) {
            is ValidationResult.Error -> result.message
            is ValidationResult.Success -> null
        }
        _registerFormState.value = currentState.copy(nameError = nameError)
    }

    fun updateEmail(email: String) {
        val currentState = _registerFormState.value
        _registerFormState.value = currentState.copy(
            email = email,
            emailError = null, // Clear error when typing
            isFormValid = registerValidationUseCase.validateForm(
                name = currentState.name,
                email = email,
                password = currentState.password,
                confirmPassword = currentState.confirmPassword
            )
        )
    }

    fun validateEmail() {
        val currentState = _registerFormState.value
        val emailError = when (val result = registerValidationUseCase.validateEmail(currentState.email)) {
            is ValidationResult.Error -> result.message
            is ValidationResult.Success -> null
        }
        _registerFormState.value = currentState.copy(emailError = emailError)
    }

    fun updatePassword(password: String) {
        val currentState = _registerFormState.value
        val passwordStrength = registerValidationUseCase.calculatePasswordStrength(password)

        _registerFormState.value = currentState.copy(
            password = password,
            passwordError = null, // Clear error when typing
            passwordStrength = passwordStrength,
            isFormValid = registerValidationUseCase.validateForm(
                name = currentState.name,
                email = currentState.email,
                password = password,
                confirmPassword = currentState.confirmPassword
            )
        )
    }

    fun validatePassword() {
        val currentState = _registerFormState.value
        val passwordError = when (val result = registerValidationUseCase.validatePassword(currentState.password)) {
            is ValidationResult.Error -> result.message
            is ValidationResult.Success -> null
        }
        _registerFormState.value = currentState.copy(passwordError = passwordError)
    }

    fun updateConfirmPassword(confirmPassword: String) {
        val currentState = _registerFormState.value
        _registerFormState.value = currentState.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = null, // Clear error when typing
            isFormValid = registerValidationUseCase.validateForm(
                name = currentState.name,
                email = currentState.email,
                password = currentState.password,
                confirmPassword = confirmPassword
            )
        )
    }

    fun validateConfirmPassword() {
        val currentState = _registerFormState.value
        val confirmPasswordError = when (val result = registerValidationUseCase.validateConfirmPassword(
            currentState.password,
            currentState.confirmPassword
        )) {
            is ValidationResult.Error -> result.message
            is ValidationResult.Success -> null
        }
        _registerFormState.value = currentState.copy(confirmPasswordError = confirmPasswordError)
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
                    // Add a small delay for token persistence propagation
                    kotlinx.coroutines.delay(100)
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
            // Only set loading if we are currently Idle or Error
            if (_authStatus.value is AuthState.Idle || _authStatus.value is AuthState.Error) {
                _authStatus.value = AuthState.Loading("Checking authentication...")
            }
            val currentToken = tokenDataSource.accessToken.first()

            if (currentToken == null) {
                _authStatus.value = AuthState.Success(false)
                return@launch
            }

            when (val result = repository.validateToken(currentToken)) {
                is Result.Success -> {
                    if (result.data) {
                        logger.info(LogTags.AUTH, "Token validation successful")
                        _authStatus.value = AuthState.Success(true)
                    } else {
                        logger.warning(LogTags.AUTH, "Token validation failed (invalid token). Clearing session.")
                        tokenDataSource.clearTokens()
                        _authStatus.value = AuthState.Success(false)
                    }
                }

                is Result.Error -> {
                    // On network error, show error state to allow retry instead of forcing login
                    _authStatus.value = AuthState.Error(result.exception)
                }

                is Result.Loading -> {
                    // Stay in loading if the repository returns loading
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logger.info(LogTags.AUTH, "LOGOUT_DEBUG: [1] AuthViewModel.logout() called")
            try {
                when (val result = repository.logout()) {
                    is Result.Success -> {
                        logger.info(LogTags.AUTH, "LOGOUT_DEBUG: [2] Repository logout SUCCESS")
                    }
                    is Result.Error -> {
                        logger.error(LogTags.AUTH, "LOGOUT_DEBUG: [2] Repository logout ERROR: ${result.exception.message}", result.exception)
                    }
                    is Result.Loading -> {}
                }
            } catch (e: Exception) {
                logger.error(LogTags.AUTH, "LOGOUT_DEBUG: [2] Repository logout EXCEPTION: ${e.message}", e)
            } finally {
                logger.info(LogTags.AUTH, "LOGOUT_DEBUG: [3] Clearing local tokens and setting _authStatus to Success(false)")
                tokenDataSource.clearTokens()
                _authStatus.value = AuthState.Success(false)
                
                // Reset states
                _loginState.value = AuthState.Idle
                _registerState.value = AuthState.Idle
                _loginFormState.value = LoginFormState()
                _registerFormState.value = RegisterFormState()
                logger.info(LogTags.AUTH, "LOGOUT_DEBUG: [4] AuthViewModel.logout() finished. Auth status is now: ${_authStatus.value}")
            }
        }
    }

    fun resetLoginState() {
        _loginState.value = AuthState.Idle
    }

    fun resetRegisterState() {
        _registerState.value = AuthState.Idle
    }
}