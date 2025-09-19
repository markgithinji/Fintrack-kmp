package com.fintrack.shared.feature.transaction.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

// Auth API using Ktor
class AuthApi(private val baseUrl: String = ApiConfig.BASE_URL) {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; explicitNulls = false })
        }
    }

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
