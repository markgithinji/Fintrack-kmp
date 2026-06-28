package com.fintrack.shared.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.core.domain.SaveState
import com.fintrack.shared.feature.core.domain.ValidationResult
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.user.domain.model.User
import com.fintrack.shared.feature.user.domain.usecase.GetUserProfileUseCase
import com.fintrack.shared.feature.user.domain.usecase.ProfileValidationUseCase
import com.fintrack.shared.feature.user.domain.usecase.UpdateProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val validationUseCase: ProfileValidationUseCase
) : ViewModel() {

    val userProfile: StateFlow<User?> = getUserProfileUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _editState = MutableStateFlow<SaveState<Unit>>(SaveState.Idle)
    val editState: StateFlow<SaveState<Unit>> = _editState.asStateFlow()

    private val _formState = MutableStateFlow(ProfileFormState())
    val formState: StateFlow<ProfileFormState> = _formState.asStateFlow()

    init {
        refreshProfile()
        viewModelScope.launch {
            userProfile.collect { user ->
                user?.let {
                    _formState.update { state ->
                        state.copy(name = it.name, email = it.email)
                    }
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _formState.update { it.copy(name = name, nameError = null) }
    }

    fun onEmailChange(email: String) {
        _formState.update { it.copy(email = email, emailError = null) }
    }

    fun updateProfile() {
        val name = _formState.value.name
        val email = _formState.value.email

        val nameResult = validationUseCase.validateName(name)
        val emailResult = validationUseCase.validateEmail(email)

        if (nameResult is ValidationResult.Error || emailResult is ValidationResult.Error) {
            _formState.update {
                it.copy(
                    nameError = (nameResult as? ValidationResult.Error)?.message,
                    emailError = (emailResult as? ValidationResult.Error)?.message
                )
            }
            return
        }

        viewModelScope.launch {
            _editState.value = SaveState.Loading
            when (val result = updateProfileUseCase(name, email)) {
                is Result.Success -> {
                    _editState.value = SaveState.Success(Unit)
                }
                is Result.Error -> {
                    _editState.value = SaveState.Error(result.exception)
                }
                is Result.Loading -> { /* Should not happen */ }
            }
        }
    }

    fun resetEditState() {
        _editState.value = SaveState.Idle
    }

    fun refreshProfile() {
        viewModelScope.launch {
            getUserProfileUseCase.refresh()
        }
    }
}

data class ProfileFormState(
    val name: String = "",
    val nameError: String? = null,
    val email: String = "",
    val emailError: String? = null
)
