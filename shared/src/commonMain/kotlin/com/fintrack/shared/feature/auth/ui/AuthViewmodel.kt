package com.fintrack.shared.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.auth.domain.model.AuthResponse
import com.fintrack.shared.feature.auth.domain.repository.AuthRepository
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.auth.domain.repository.TokenDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    val token: StateFlow<String?> = tokenDataSource.token
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun login(email: String, password: String) {
        _loginState.value = Result.Loading
        viewModelScope.launch {
            when (val result = repository.login(email, password)) {
                is Result.Success -> {
                    tokenDataSource.saveToken(result.data.token)
                    _loginState.value = result
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
                }

                is Result.Error -> {
                    _registerState.value = result
                }

                else -> {}
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenDataSource.clearToken()
        }
    }
}


