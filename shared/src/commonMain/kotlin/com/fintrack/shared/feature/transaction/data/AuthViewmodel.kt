package com.fintrack.shared.feature.transaction.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
) : ViewModel() {

    private val repository: AuthRepository = AuthRepository()

    private val _loginState = MutableStateFlow<Result<User>?>(null)
    val loginState: StateFlow<Result<User>?> = _loginState

    private val _registerState = MutableStateFlow<Result<User>?>(null)
    val registerState: StateFlow<Result<User>?> = _registerState

    fun login(email: String, password: String) {
        _loginState.value = Result.Loading
        viewModelScope.launch {
            _loginState.value = repository.login(email, password)
        }
    }

    fun register(name: String, email: String, password: String) {
        _registerState.value = Result.Loading
        viewModelScope.launch {
            _registerState.value = repository.register(name, email, password)
        }
    }
}
