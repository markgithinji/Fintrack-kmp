package com.fintrack.shared.feature.user.di

import com.fintrack.shared.feature.profile.ProfileViewModel
import com.fintrack.shared.feature.user.data.UserApi
import com.fintrack.shared.feature.user.data.UserRepositoryImpl
import com.fintrack.shared.feature.user.domain.repository.UserRepository
import com.fintrack.shared.feature.user.domain.usecase.DeleteAccountUseCase
import com.fintrack.shared.feature.user.domain.usecase.GetUserProfileUseCase
import com.fintrack.shared.feature.user.domain.usecase.ProfileValidationUseCase
import com.fintrack.shared.feature.user.domain.usecase.UpdateProfileUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val userModule = module {
    single { UserApi(client = get(), baseUrl = getProperty("baseUrl")) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single { GetUserProfileUseCase(repository = get()) }
    single { UpdateProfileUseCase(repository = get()) }
    single { DeleteAccountUseCase(userRepository = get(), authRepository = get()) }
    single { ProfileValidationUseCase() }

    viewModel {
        ProfileViewModel(
            getUserProfileUseCase = get(),
            updateProfileUseCase = get(),
            validationUseCase = get(),
            summaryRepository = get()
        )
    }
}
