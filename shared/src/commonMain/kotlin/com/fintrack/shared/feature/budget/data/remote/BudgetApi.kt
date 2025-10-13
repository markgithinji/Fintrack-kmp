package com.fintrack.shared.feature.budget.data.remote

import com.fintrack.shared.feature.budget.data.model.BudgetDto
import com.fintrack.shared.feature.budget.data.model.BudgetWithStatusDto
import com.fintrack.shared.feature.budget.data.model.CreateBudgetRequest
import com.fintrack.shared.feature.budget.data.model.UpdateBudgetRequest
import com.fintrack.shared.feature.core.ApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType


class BudgetApi(
    private val client: HttpClient,
    private val baseUrl: String
) {

    suspend fun addBudget(request: CreateBudgetRequest): BudgetDto {
        val response: ApiResponse<BudgetDto> = client.post("$baseUrl/budgets") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        return response.result
    }

    suspend fun updateBudget(id: String, request: UpdateBudgetRequest): BudgetDto {
        val response: ApiResponse<BudgetDto> = client.put("$baseUrl/budgets/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        return response.result
    }

    suspend fun deleteBudget(id: String) {
        client.delete("$baseUrl/budgets/$id")
    }

    suspend fun getBudgets(): List<BudgetWithStatusDto> {
        val response: ApiResponse<List<BudgetWithStatusDto>> = client.get("$baseUrl/budgets").body()
        return response.result
    }

    suspend fun getBudgetById(id: String): BudgetWithStatusDto {
        val response: ApiResponse<BudgetWithStatusDto> = client.get("$baseUrl/budgets/$id").body()
        return response.result
    }
}