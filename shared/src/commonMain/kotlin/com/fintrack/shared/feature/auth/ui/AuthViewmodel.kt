package com.fintrack.shared.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.auth.domain.model.AuthResponse
import com.fintrack.shared.feature.auth.domain.repository.AuthRepository
import com.fintrack.shared.feature.auth.domain.datasource.TokenDataSource
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

    private val _loginState = MutableStateFlow<Result<AuthResponse>?>(null)
    val loginState: StateFlow<Result<AuthResponse>?> = _loginState

    private val _registerState = MutableStateFlow<Result<AuthResponse>?>(null)
    val registerState: StateFlow<Result<AuthResponse>?> = _registerState

    private val _authStatus = MutableStateFlow<Result<Boolean>>(Result.Loading)
    val authStatus: StateFlow<Result<Boolean>> = _authStatus

    val token: StateFlow<String?> = tokenDataSource.token
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        checkAuthenticationStatus()
    }

    fun login(email: String, password: String) {
        _loginState.value = Result.Loading
        viewModelScope.launch {
            when (val result = repository.login(email, password)) {
                is Result.Success -> {
                    tokenDataSource.saveToken(result.data.token)
                    _loginState.value = result
                    _authStatus.value = Result.Success(true)
                }

                is Result.Error -> {
                    _loginState.value = result
                }

                else -> {}
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        _registerState.value = Result.Loading
        viewModelScope.launch {
            when (val result = repository.register(name, email, password)) {
                is Result.Success -> {
                    tokenDataSource.saveToken(result.data.token)
                    _registerState.value = result
                    _authStatus.value = Result.Success(true)
                }

                is Result.Error -> {
                    _registerState.value = result
                }

                else -> {}
            }
        }
    }

    fun checkAuthenticationStatus() {
        viewModelScope.launch {
            _authStatus.value = Result.Loading
            val currentToken = tokenDataSource.token.first()

            if (currentToken == null) {
                _authStatus.value = Result.Success(false)
                return@launch
            }

            when (val result = repository.validateToken(currentToken)) {
                is Result.Success -> {
                    _authStatus.value = if (result.data) {
                        Result.Success(true)
                    } else {
                        // Clear invalid token
                        tokenDataSource.clearToken()
                        Result.Success(false)
                    }
                }

                is Result.Error -> {
                    // On network error, treat as unauthenticated to be safe
                    _authStatus.value = Result.Success(false)
                }

                else -> {
                    _authStatus.value = Result.Success(false)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenDataSource.clearToken()
            _authStatus.value = Result.Success(false)
            _loginState.value = null
            _registerState.value = null
        }
    }
}

