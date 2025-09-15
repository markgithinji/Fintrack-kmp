package com.fintrack.shared.feature.transaction.data
import com.fintrack.shared.feature.transaction.model.Budget
import com.fintrack.shared.feature.transaction.model.Category
import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDate

@Serializable
data class BudgetDto(
    val id: Int? = null,
    val name: String,
    val categories: List<String>,
    val limit: Double,
    val isExpense: Boolean,
    val startDate: LocalDate,
    val endDate: LocalDate
)

fun BudgetDto.toDomain(): Budget =
    Budget(
        id = id,
        name = name,
        categories = categories.map { n -> Category.fromName(n, isExpense) },
        limit = limit,
        isExpense = isExpense,
        startDate = startDate,
        endDate = endDate
    )

fun Budget.toDto(): BudgetDto =
    BudgetDto(
        id = id,
        name = name,
        categories = categories.map { it.name },
        limit = limit,
        isExpense = isExpense,
        startDate = startDate,
        endDate = endDate
    )


