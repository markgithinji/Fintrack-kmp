package com.fintrack.shared.feature.auth.data.remote

import com.fintrack.shared.feature.core.ApiConfig
import com.fintrack.shared.feature.auth.data.model.AuthResponseDto
import com.fintrack.shared.feature.auth.data.model.LoginRequestDto
import com.fintrack.shared.feature.auth.data.model.RegisterRequestDto
import com.fintrack.shared.feature.core.ApiClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class AuthApi(
    private val client: HttpClient,
    private val baseUrl: String
) {

    suspend fun login(request: LoginRequestDto): AuthResponseDto {
        return client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun register(request: RegisterRequestDto): AuthResponseDto {
        return client.post("$baseUrl/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getUserById(userId: String, token: String): AuthResponseDto {
        return client.get("$baseUrl/users/$userId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }
}