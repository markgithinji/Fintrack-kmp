package com.fintrack.shared.feature.summary.domain.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

sealed class Period {
    data class Week(val code: String) : Period()
    data class Month(val code: String) : Period()
    data class Year(val code: String) : Period()

    fun getDateRange(): Pair<String, String>? {
        return try {
            when (this) {
                is Week -> {
                    // code format: 2024-W25
                    val parts = code.split("-W")
                    if (parts.size == 2) {
                        val year = parts[0].toInt()
                        val week = parts[1].toInt()
                        
                        // ISO 8601 week calculation:
                        // 1. Find Jan 4th of the year (always in Week 1)
                        // 2. Find the Monday of that week
                        // 3. Add (week - 1) * 7 days
                        val jan4 = LocalDate(year, 1, 4)
                        val dayOfWeek = jan4.dayOfWeek.ordinal + 1 // Mon=1, Sun=7
                        val mondayOfWeek1 = jan4.plus(DatePeriod(days = -(dayOfWeek - 1)))
                        
                        val weekStart = mondayOfWeek1.plus(DatePeriod(days = (week - 1) * 7))
                        val weekEnd = weekStart.plus(DatePeriod(days = 6))

                        weekStart.toString() to weekEnd.toString()
                    } else null
                }
                is Month -> {
                    // code format: 2024-06
                    val parts = code.split("-")
                    if (parts.size == 2) {
                        val year = parts[0].toInt()
                        val month = parts[1].toInt()
                        val start = LocalDate(year, month, 1)
                        val end = if (month == 12) {
                            LocalDate(year + 1, 1, 1).plus(DatePeriod(days = -1))
                        } else {
                            LocalDate(year, month + 1, 1).plus(DatePeriod(days = -1))
                        }
                        start.toString() to end.toString()
                    } else null
                }
                is Year -> {
                    val year = code.toInt()
                    "$year-01-01" to "$year-12-31"
                }
            }
        } catch (_: Exception) {
            null
        }
    }
}
