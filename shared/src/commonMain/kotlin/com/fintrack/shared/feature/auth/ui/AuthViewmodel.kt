package com.fintrack.shared.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.auth.domain.model.AuthResponse
import com.fintrack.shared.feature.auth.data.repository.AuthRepository
import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.auth.data.repository.TokenRepository
import com.fintrack.shared.feature.auth.data.local.createTokenDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val tokenRepository: TokenRepository = TokenRepository(
        createTokenDataStore()
    )
    private val repository: AuthRepository = AuthRepository()

    private val _loginState = MutableStateFlow<Result<AuthResponse>?>(null)
    val loginState: StateFlow<Result<AuthResponse>?> = _loginState

    private val _registerState = MutableStateFlow<Result<AuthResponse>?>(null)
    val registerState: StateFlow<Result<AuthResponse>?> = _registerState

    val token: StateFlow<String?> = tokenRepository.token
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun login(email: String, password: String) {
        _loginState.value = Result.Loading
        viewModelScope.launch {
            when (val result = repository.login(email, password)) {
                is Result.Success -> {
                    tokenRepository.saveToken(result.data.token)
                    _loginState.value = result
                    println("Login successful, token persisted: ${result.data.token}")
                }
                is Result.Error -> {
                    _loginState.value = result
                    println("Login failed: ${result.exception.message}")
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
                    tokenRepository.saveToken(result.data.token)
                    _registerState.value = result
                    println("Register successful, token persisted: ${result.data.token}")
                }
                is Result.Error -> {
                    _registerState.value = result
                    println("Register failed: ${result.exception.message}")
                }
                else -> {}
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenRepository.clearToken()
            println("Token cleared from DataStore")
        }
    }
}


