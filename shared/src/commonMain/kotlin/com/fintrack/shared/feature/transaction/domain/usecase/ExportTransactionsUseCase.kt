package com.fintrack.shared.feature.transaction.domain.usecase

import com.fintrack.shared.feature.core.util.FileSaver
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ExportTransactionsUseCase(
    private val repository: TransactionRepository,
    private val fileSaver: FileSaver,
) {
    @OptIn(ExperimentalTime::class)
    suspend operator fun invoke(): Result<String> {
        val result = repository.getAllTransactions()
        if (result is Result.Error) return Result.Error(result.exception)
        
        val transactions = (result as Result.Success).data
        val csv = StringBuilder()
        csv.append("Date,Category,Amount,Type,Account,Description\n")
        
        transactions.forEach { transaction ->
            csv.append("${transaction.dateTime},")
            csv.append("${transaction.category},")
            csv.append("${transaction.amount},")
            csv.append("${if (transaction.isIncome) "Income" else "Expense"},")
            csv.append("${transaction.accountId},")
            csv.append("${transaction.description ?: ""}\n")
        }

        val now = Clock.System.now()
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
}
