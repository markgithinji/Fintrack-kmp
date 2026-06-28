package com.fintrack.shared.feature.transaction.domain.usecase

import com.fintrack.shared.feature.core.util.FileSaver
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ExportTransactionsUseCase(
    private val repository: TransactionRepository,
    private val fileSaver: FileSaver,
) {
    suspend operator fun invoke(): Result<String> {
        val result = repository.getAllTransactions()
        if (result is Result.Error) return Result.Error(result.exception)
        
        val transactions = (result as Result.Success).data
        val csv = StringBuilder()
        csv.append("Date,Category,Amount,Type,Account,Description\n")
        
        transactions.forEach { transaction ->
            csv.append("${transaction.dateTime},")
            csv.append("${escapeCsv(transaction.category)},")
            csv.append("${transaction.amount},")
            csv.append("${if (transaction.isIncome) "Income" else "Expense"},")
            csv.append("${escapeCsv(transaction.accountId)},")
            csv.append("${escapeCsv(transaction.description ?: "")}\n")
        }

        val now = kotlinx.datetime.Clock.System.now()
        val timestamp = now.toLocalDateTime(TimeZone.currentSystemDefault())
            .toString()
            .replace(":", "-")
            .split(".")[0]
        val fileName = "fintrack_export_$timestamp.csv"
        
        val savedPath = fileSaver.saveFile(fileName, csv.toString())
        
        return if (savedPath != null) {
            Result.Success(savedPath)
        } else {
            Result.Error(Exception("Failed to save export file"))
        }
    }

    private fun escapeCsv(value: String): String {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\""
        }
        return value
    }
}
