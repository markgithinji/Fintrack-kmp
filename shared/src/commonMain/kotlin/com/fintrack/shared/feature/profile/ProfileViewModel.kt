package com.fintrack.shared.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.core.domain.SaveState
import com.fintrack.shared.feature.core.domain.ValidationResult
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.core.logger.KMPLogger
import com.fintrack.shared.feature.core.logger.LogTags
import com.fintrack.shared.feature.user.domain.model.User
import com.fintrack.shared.feature.user.domain.usecase.GetUserProfileUseCase
import com.fintrack.shared.feature.user.domain.usecase.ProfileValidationUseCase
import com.fintrack.shared.feature.user.domain.usecase.UpdateProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
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

    private val _profileState = MutableStateFlow<Result<User>>(
        getUserProfileUseCase().value?.let { Result.Success(it) } ?: Result.Loading
    )
    val profileState: StateFlow<Result<User>> = _profileState.asStateFlow()

    private val _editState = MutableStateFlow<SaveState<Unit>>(SaveState.Idle)
    val editState: StateFlow<SaveState<Unit>> = _editState.asStateFlow()

    private val _formState = MutableStateFlow(
        getUserProfileUseCase().value?.let { 
            ProfileFormState(name = it.name, email = it.email) 
        } ?: ProfileFormState()
    )
    val formState: StateFlow<ProfileFormState> = _formState.asStateFlow()

    init {
        // Collect from the repository flow and update our Result state
        viewModelScope.launch {
            getUserProfileUseCase().collect { user ->
                if (user != null) {
                    _profileState.value = Result.Success(user)
                    _formState.update { state ->
                        state.copy(name = user.name, email = user.email)
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

        if ((nameResult is ValidationResult.Error) || (emailResult is ValidationResult.Error)) {
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
                    // State update is handled by the userProfile collector, but we can force it here
                    getUserProfileUseCase().value?.let {
                        _profileState.value = Result.Success(it)
                    }
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
            // Only show loading if we don't already have success data to prevent flicker
            if (_profileState.value !is Result.Success) {
                _profileState.value = Result.Loading
            }
            
            try {
                getUserProfileUseCase.refresh()
                // Ensure state is updated even if the user data is identical (StateFlow won't re-emit)
                getUserProfileUseCase().value?.let { 
                    _profileState.value = Result.Success(it)
                }
            } catch (e: Exception) {
                // If we have existing data, don't show the error as a full-screen state
                // but if we were stuck in loading, show the error.
                if (_profileState.value !is Result.Success) {
                    _profileState.value = Result.Error(e)
                }
            }
        }
    }
}

data class ProfileFormState(
    val name: String = "",
    val nameError: String? = null,
    val email: String = "",
    val emailError: String? = null
)
