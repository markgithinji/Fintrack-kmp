package com.fintrack.shared.feature.budget.data.remote

import com.fintrack.shared.feature.auth.data.local.TokenProvider
import com.fintrack.shared.feature.auth.data.local.TokenProviderImpl
import com.fintrack.shared.feature.auth.data.repository.TokenRepository
import com.fintrack.shared.feature.auth.data.local.createTokenDataStore
import com.fintrack.shared.feature.core.ApiConfig
import com.fintrack.shared.feature.core.ApiResponse
import com.fintrack.shared.feature.budget.data.model.BudgetDto
import com.fintrack.shared.feature.budget.data.model.BudgetWithStatusDto
import com.fintrack.shared.feature.core.ApiClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.plugin
import io.ktor.client.request.HttpRequestPipeline
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


class BudgetApi(
    private val client: HttpClient = ApiClient.authenticatedClient,
    private val baseUrl: String = ApiConfig.BASE_URL
) {

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

    suspend fun getBudgets(): List<BudgetWithStatusDto> {
        val response: ApiResponse<List<BudgetWithStatusDto>> = client.get("$baseUrl/budgets").body()
        return response.result
    }

    suspend fun getBudgetById(id: Int): BudgetWithStatusDto {
        val response: ApiResponse<BudgetWithStatusDto> = client.get("$baseUrl/budgets/$id").body()
        return response.result
    }
}