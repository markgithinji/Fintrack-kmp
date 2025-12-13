package com.fintrack.shared.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.auth.domain.datasource.TokenDataSource
import com.fintrack.shared.feature.auth.domain.model.AuthResponse
import com.fintrack.shared.feature.auth.domain.model.AuthState
import com.fintrack.shared.feature.auth.domain.repository.AuthRepository
import com.fintrack.shared.feature.core.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository,
    private val tokenDataSource: TokenDataSource
) : ViewModel() {

    private val _loginState = MutableStateFlow<AuthState<AuthResponse>>(AuthState.Idle)
    val loginState: StateFlow<AuthState<AuthResponse>> = _loginState

    private val _registerState = MutableStateFlow<AuthState<AuthResponse>>(AuthState.Idle)
    val registerState: StateFlow<AuthState<AuthResponse>> = _registerState

    private val _authStatus =
        MutableStateFlow<AuthState<Boolean>>(AuthState.Loading("Checking authentication..."))
    val authStatus: StateFlow<AuthState<Boolean>> = _authStatus

    val token: StateFlow<String?> = tokenDataSource.token
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        checkAuthenticationStatus()
    }

    fun login(email: String, password: String) {
        _loginState.value = AuthState.Loading("Logging in...")
        viewModelScope.launch {
            when (val result = repository.login(email, password)) {
                is Result.Success -> {
                    tokenDataSource.saveToken(result.data.token)
                    _loginState.value = AuthState.Success(result.data)
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

    fun register(name: String, email: String, password: String) {
        _registerState.value = AuthState.Loading("Creating account...")
        viewModelScope.launch {
            when (val result = repository.register(name, email, password)) {
                is Result.Success -> {
                    tokenDataSource.saveToken(result.data.token)
                    _registerState.value = AuthState.Success(result.data)
                    _authStatus.value = AuthState.Success(true)
                }

                is Result.Error -> {
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
            val currentToken = tokenDataSource.token.first()

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
                        tokenDataSource.clearToken()
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
            tokenDataSource.clearToken()
            _authStatus.value = AuthState.Success(false)
            // Reset to Idle
            _loginState.value = AuthState.Idle
            _registerState.value = AuthState.Idle
        }
    }

    fun resetLoginState() {
        _loginState.value = AuthState.Idle
    }

    fun resetRegisterState() {
        _registerState.value = AuthState.Idle
    }
}

