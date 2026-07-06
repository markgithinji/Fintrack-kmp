package com.fintrack.shared.feature.transaction.domain.usecase

import com.fintrack.shared.feature.core.util.FileSaver
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.settings.domain.model.ExportFormat
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import com.fintrack.shared.feature.transaction.domain.util.generatePdfBytes
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ExportTransactionsUseCase(
    private val repository: TransactionRepository,
    private val fileSaver: FileSaver,
) {
    private val json = Json { prettyPrint = true }

    @OptIn(ExperimentalTime::class)
    suspend operator fun invoke(format: ExportFormat = ExportFormat.CSV): Result<String> {
        val result = repository.getAllTransactions()
        if (result is Result.Error) return Result.Error(result.exception)
        
        val transactions = (result as Result.Success).data
        
        val now = Clock.System.now()
        val timestamp = now.toLocalDateTime(TimeZone.currentSystemDefault())
            .toString()
            .replace(":", "-")
            .split(".")[0]
        val fileName = "fintrack_export_$timestamp.${format.extension}"

        val savedPath = when (format) {
            ExportFormat.CSV -> fileSaver.saveFile(fileName, generateCsv(transactions))
            ExportFormat.JSON -> fileSaver.saveFile(fileName, json.encodeToString(transactions))
            ExportFormat.PDF -> fileSaver.saveFileBytes(fileName, generatePdfBytes(transactions))
        }
        
        return if (savedPath != null) {
            Result.Success(savedPath)
        } else {
            Result.Error(Exception("Failed to save export file"))
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun generateCsv(transactions: List<com.fintrack.shared.feature.transaction.domain.model.Transaction>): String {
        val csv = StringBuilder()
        csv.append("Date,Category,Amount,Transaction Cost,Total,Type,Account,Description\n")
        
        transactions.forEach { transaction ->
            csv.append("${transaction.dateTime},")
            csv.append("${escapeCsv(transaction.category)},")
            csv.append("${transaction.amount},")
            csv.append("${transaction.transactionCost},")
            csv.append("${transaction.totalAmount},")
            csv.append("${if (transaction.isIncome) "Income" else "Expense"},")
            csv.append("${escapeCsv(transaction.accountId)},")
            csv.append("${escapeCsv(transaction.description ?: "")}\n")
        }
        return csv.toString()
    }

    private fun escapeCsv(value: String): String {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\""
        }
        return value
    }
}
