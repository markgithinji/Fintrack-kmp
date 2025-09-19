package com.fintrack.shared.feature.Auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.Auth.domain.AuthResponse
import com.fintrack.shared.feature.Auth.data.repository.AuthRepository
import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.Auth.data.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository: AuthRepository = AuthRepository()

    private val _loginState = MutableStateFlow<Result<AuthResponse>?>(null)
    val loginState: StateFlow<Result<AuthResponse>?> = _loginState

    private val _registerState = MutableStateFlow<Result<AuthResponse>?>(null)
    val registerState: StateFlow<Result<AuthResponse>?> = _registerState

    var token: String? = null
        private set

    fun login(email: String, password: String) {
        _loginState.value = Result.Loading
        viewModelScope.launch {
            when (val result = repository.login(email, password)) {
                is Result.Success -> {
                    token = result.data.token
                    SessionManager.saveToken(token!!) // save in memory
                    _loginState.value = result
                    println("Login successful, token: ${token}")
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
                    token = result.data.token
                    SessionManager.saveToken(token!!)
                    _registerState.value = result
                    println("Register successful, token: ${token}")
                }
                is Result.Error -> {
                    _registerState.value = result
                    println("Register failed: ${result.exception.message}")
                }
                else -> {}
            }
        }
    }
}
