package com.fintrack.shared.feature.transaction.data

import com.fintrack.shared.feature.transaction.model.Transaction
import com.fintrack.shared.feature.transaction.data.TransactionResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class TransactionApi(
    private val baseUrl: String
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun getTransactions(): List<Transaction> {
        val response: TransactionResponse =
            client.get("$baseUrl/transactions").body()
        return response.data
    }

    suspend fun addTransaction(transaction: Transaction): Transaction {
        return client.post("$baseUrl/transactions") {
            contentType(ContentType.Application.Json)
            setBody(transaction)
        }.body()
    }

    suspend fun getSummary(): Map<String, Any> {
        return client.get("$baseUrl/transactions/summary").body()
    }
}