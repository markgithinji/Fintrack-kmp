package com.fintrack.shared.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.user.domain.model.User
import com.fintrack.shared.feature.user.domain.usecase.GetUserProfileUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {

    val userProfile: StateFlow<User?> = getUserProfileUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        refreshProfile()
    }

    fun refreshProfile() {
        viewModelScope.launch {
            getUserProfileUseCase.refresh()
        }
    }
}
