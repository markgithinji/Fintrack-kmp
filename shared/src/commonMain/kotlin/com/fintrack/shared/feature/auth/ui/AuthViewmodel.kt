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
import com.fintrack.shared.feature.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository,
    private val userRepository: UserRepository,
    private val tokenDataSource: TokenDataSource,
    private val registerValidationUseCase: RegisterValidationUseCase,
    private val loginValidationUseCase: LoginValidationUseCase,
    private val settingsDataSource: SettingsDataSource,
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
        _isAppLocked.value = false
    }

    private fun observeTokenChanges() {
        viewModelScope.launch {
            tokenDataSource.accessToken.collect { token ->
                val currentStatus = _authStatus.value
                if (token == null) {
                    if (currentStatus != AuthState.Success(false)) {
                        _authStatus.value = AuthState.Success(false)
                    }
                } else {
                    // Only update authStatus to Success(true) if we were explicitly not authenticated
                    // This avoids the race condition at startup where a cached token might 
                    // set Success(true) before validateToken has finished.
                    if (currentStatus is AuthState.Success && !currentStatus.data) {
                        
                        // If we are currently logging in or just succeeded, delay to let UI show success
                        if (_loginState.value !is AuthState.Idle || _registerState.value !is AuthState.Idle) {
                            delay(1000)
                        }

                        _authStatus.value = AuthState.Success(true)
                    }
                }
            }
        }
    }

    fun updateLoginEmail(email: String) {
        val currentState = _loginFormState.value
        _loginFormState.value = currentState.copy(
            email = email,
            emailError = null, // Clear error when typing
            isFormValid = loginValidationUseCase(email, currentState.password).isValid
        )
    }

    fun validateLoginEmail() {
        val currentState = _loginFormState.value
        val result = loginValidationUseCase(currentState.email, currentState.password)
        val emailError = (result.emailResult as? ValidationResult.Error)?.message
        _loginFormState.value = currentState.copy(emailError = emailError)
    }

    fun updateLoginPassword(password: String) {
        val currentState = _loginFormState.value
        _loginFormState.value = currentState.copy(
            password = password,
            passwordError = null, // Clear error when typing
            isFormValid = loginValidationUseCase(currentState.email, password).isValid
        )
    }

    fun validateLoginPassword() {
        val currentState = _loginFormState.value
        val result = loginValidationUseCase(currentState.email, currentState.password)
        val passwordError = (result.passwordResult as? ValidationResult.Error)?.message
        _loginFormState.value = currentState.copy(passwordError = passwordError)
    }

    fun login() {
        val formState = _loginFormState.value
        if (!formState.isFormValid) return

        _loginState.value = AuthState.Loading("Logging in...")
        viewModelScope.launch {
            when (val result = repository.login(formState.email, formState.password)) {
                is Result.Success -> {
                    // Refresh user profile before proceeding
                    try {
                        userRepository.refreshProfile()
                    } catch (_: Exception) {}

                    // Set login state to success to show success on button
                    _loginState.value = AuthState.Success(result.data)
                    
                    // Delay setting the global auth status to give the UI time to show success state
                    delay(1000)

                    _authStatus.value = AuthState.Success(true)
                }

                is Result.Error -> {
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
            isFormValid = registerValidationUseCase(
                name = name,
                email = currentState.email,
                password = currentState.password,
                confirmPassword = currentState.confirmPassword
            ).isValid
        )
    }

    fun validateName() {
        val currentState = _registerFormState.value
        val result = registerValidationUseCase(
            name = currentState.name,
            email = currentState.email,
            password = currentState.password,
            confirmPassword = currentState.confirmPassword
        )
        val nameError = (result.nameResult as? ValidationResult.Error)?.message
        _registerFormState.value = currentState.copy(nameError = nameError)
    }

    fun updateEmail(email: String) {
        val currentState = _registerFormState.value
        _registerFormState.value = currentState.copy(
            email = email,
            emailError = null, // Clear error when typing
            isFormValid = registerValidationUseCase(
                name = currentState.name,
                email = email,
                password = currentState.password,
                confirmPassword = currentState.confirmPassword
            ).isValid
        )
    }

    fun validateEmail() {
        val currentState = _registerFormState.value
        val result = registerValidationUseCase(
            name = currentState.name,
            email = currentState.email,
            password = currentState.password,
            confirmPassword = currentState.confirmPassword
        )
        val emailError = (result.emailResult as? ValidationResult.Error)?.message
        _registerFormState.value = currentState.copy(emailError = emailError)
    }

    fun updatePassword(password: String) {
        val currentState = _registerFormState.value
        val result = registerValidationUseCase(
            name = currentState.name,
            email = currentState.email,
            password = password,
            confirmPassword = currentState.confirmPassword
        )

        _registerFormState.value = currentState.copy(
            password = password,
            passwordError = null, // Clear error when typing
            passwordStrength = result.passwordStrength,
            isFormValid = result.isValid
        )
    }

    fun validatePassword() {
        val currentState = _registerFormState.value
        val result = registerValidationUseCase(
            name = currentState.name,
            email = currentState.email,
            password = currentState.password,
            confirmPassword = currentState.confirmPassword
        )
        val passwordError = (result.passwordResult as? ValidationResult.Error)?.message
        _registerFormState.value = currentState.copy(passwordError = passwordError)
    }

    fun updateConfirmPassword(confirmPassword: String) {
        val currentState = _registerFormState.value
        _registerFormState.value = currentState.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = null, // Clear error when typing
            isFormValid = registerValidationUseCase(
                name = currentState.name,
                email = currentState.email,
                password = currentState.password,
                confirmPassword = confirmPassword
            ).isValid
        )
    }

    fun validateConfirmPassword() {
        val currentState = _registerFormState.value
        val result = registerValidationUseCase(
            name = currentState.name,
            email = currentState.email,
            password = currentState.password,
            confirmPassword = currentState.confirmPassword
        )
        val confirmPasswordError = (result.confirmPasswordResult as? ValidationResult.Error)?.message
        _registerFormState.value = currentState.copy(confirmPasswordError = confirmPasswordError)
    }

    fun register() {
        val formState = _registerFormState.value
        if (!formState.isFormValid) return

        _registerState.value = AuthState.Loading("Creating account...")
        viewModelScope.launch {
            when (val result =
                repository.register(formState.name, formState.email, formState.password)) {
                is Result.Success -> {
                    // Refresh user profile before proceeding
                    try {
                        userRepository.refreshProfile()
                    } catch (_: Exception) {}

                    // Set register state to success to show success on button
                    _registerState.value = AuthState.Success(result.data)
                    
                    // Delay setting the global auth status to give the UI time to show success state
                    delay(1000)

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
            val currentStatus = _authStatus.value
            // Only set loading if we are currently Idle or Error
            if (currentStatus is AuthState.Idle || currentStatus is AuthState.Error) {
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
                        // Refresh profile on successful token validation (app start)
                        try {
                            userRepository.refreshProfile()
                        } catch (_: Exception) {}

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
            try {
                repository.logout()
            } catch (e: Exception) {
                logger.error(LogTags.AUTH, "Logout error", e)
            } finally {
                tokenDataSource.clearTokens()
                _authStatus.value = AuthState.Success(false)
                
                // Reset states
                _loginState.value = AuthState.Idle
                _registerState.value = AuthState.Idle
                _loginFormState.value = LoginFormState()
                _registerFormState.value = RegisterFormState()
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