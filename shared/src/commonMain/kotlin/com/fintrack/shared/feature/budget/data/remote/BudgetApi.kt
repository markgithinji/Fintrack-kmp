package com.fintrack.shared.feature.budget.data.remote

import com.fintrack.shared.feature.Auth.data.SessionManager
import com.fintrack.shared.feature.core.ApiConfig
import com.fintrack.shared.feature.core.ApiResponse
import com.fintrack.shared.feature.budget.data.model.BudgetDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class BudgetApi(private val baseUrl: String = ApiConfig.BASE_URL) {
    private val client = HttpClient {
        install(ContentNegotiation.Plugin) {
            json(Json { ignoreUnknownKeys = true; explicitNulls = false })
        }
        // Automatically add Authorization header for every request
        defaultRequest {
            SessionManager.token?.let {
                header("Authorization", "Bearer $it")
            }
        }
    }

    suspend fun getBudgets(): List<BudgetDto> {
        val response: ApiResponse<List<BudgetDto>> =
            client.get("$baseUrl/budgets").body()
        return response.result
    }

    suspend fun addBudget(budget: BudgetDto): BudgetDto {
        val response: ApiResponse<BudgetDto> = client.post("$baseUrl/budgets") {
            contentType(ContentType.Application.Json)
            setBody(budget)
        }.body()
        return response.result
    }

    suspend fun updateBudget(id: Int, budget: BudgetDto): BudgetDto {
        val response: ApiResponse<BudgetDto> = client.put("$baseUrl/budgets/$id") {
            contentType(ContentType.Application.Json)
            setBody(budget)
        }.body()
        return response.result
    }

    suspend fun deleteBudget(id: Int) {
        client.delete("$baseUrl/budgets/$id")
    }

    suspend fun getBudgetById(id: Int): BudgetDto {
        val response: ApiResponse<BudgetDto> =
            client.get("$baseUrl/budgets/$id").body()
        return response.result
    }
}