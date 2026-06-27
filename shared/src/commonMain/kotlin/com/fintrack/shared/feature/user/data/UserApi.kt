package com.fintrack.shared.feature.user.data

import com.fintrack.shared.feature.core.data.domain.ApiResponse
import com.fintrack.shared.feature.user.data.model.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class UserApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun getUserProfile(): UserDto {
        val response: ApiResponse<UserDto> = client.get("$baseUrl/users/me").body()
        return response.result
    }
}
