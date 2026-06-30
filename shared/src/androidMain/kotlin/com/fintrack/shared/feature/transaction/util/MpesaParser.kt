package com.fintrack.shared.feature.transaction.util

import com.fintrack.shared.feature.transaction.domain.model.Transaction
import kotlinx.datetime.LocalDateTime
import java.text.SimpleDateFormat
import java.util.Locale

object MpesaParser {
    private val sentRegex = """([A-Z0-9]{10}) Confirmed\. Ksh([\d,]+\.\d{2}) sent to (.*) on (\d{1,2}/\d{1,2}/\d{2}) at (\d{1,2}:\d{2} [AP]M)\.""".toRegex()
    private val receivedRegex = """([A-Z0-9]{10}) Confirmed\. You have received Ksh([\d,]+\.\d{2}) from (.*) on (\d{1,2}/\d{1,2}/\d{2}) at (\d{1,2}:\d{2} [AP]M)\.""".toRegex()
    private val paidRegex = """([A-Z0-9]{10}) Confirmed\. Ksh([\d,]+\.\d{2}) paid to (.*) on (\d{1,2}/\d{1,2}/\d{2}) at (\d{1,2}:\d{2} [AP]M)\.""".toRegex()
    private val costRegex = """Transaction cost, Ksh([\d,]+\.\d{2})""".toRegex()

    private val dateFormat = SimpleDateFormat("dd/MM/yy h:mm a", Locale.ENGLISH)

    fun parse(message: String, accountId: String = "mpesa"): Transaction? {
        if (!message.contains("Confirmed") || !message.contains("Ksh")) return null

        val cost = costRegex.find(message)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0

        sentRegex.find(message)?.let {
            val amount = it.groupValues[2].replace(",", "").toDouble()
            val recipient = it.groupValues[3]
            val dateTime = parseDateTime(it.groupValues[4], it.groupValues[5])
            return Transaction(
                accountId = accountId,
                isIncome = false,
                amount = amount,
                transactionCost = cost,
                category = "Transfer",
                dateTime = dateTime,
                description = "Sent to $recipient (Ref: ${it.groupValues[1]})"
            )
        }

        receivedRegex.find(message)?.let {
            val amount = it.groupValues[2].replace(",", "").toDouble()
            val sender = it.groupValues[3]
            val dateTime = parseDateTime(it.groupValues[4], it.groupValues[5])
            return Transaction(
                accountId = accountId,
                isIncome = true,
                amount = amount,
                transactionCost = cost,
                category = "Income",
                dateTime = dateTime,
                description = "Received from $sender (Ref: ${it.groupValues[1]})"
            )
        }

        paidRegex.find(message)?.let {
            val amount = it.groupValues[2].replace(",", "").toDouble()
            val recipient = it.groupValues[3]
            val dateTime = parseDateTime(it.groupValues[4], it.groupValues[5])
            return Transaction(
                accountId = accountId,
                isIncome = false,
                amount = amount,
                transactionCost = cost,
                category = inferCategory(recipient),
                dateTime = dateTime,
                description = "Paid to $recipient (Ref: ${it.groupValues[1]})"
            )
        }

        return null
    }

    private fun parseDateTime(date: String, time: String): LocalDateTime {
        return try {
            val dateStr = "$date $time"
            val parsedDate = dateFormat.parse(dateStr)
            val cal = java.util.Calendar.getInstance()
            if (parsedDate != null) {
                cal.time = parsedDate
                LocalDateTime(
                    cal.get(java.util.Calendar.YEAR),
                    cal.get(java.util.Calendar.MONTH) + 1,
                    cal.get(java.util.Calendar.DAY_OF_MONTH),
                    cal.get(java.util.Calendar.HOUR_OF_DAY),
                    cal.get(java.util.Calendar.MINUTE)
                )
            } else {
                throw Exception("Parse failed")
            }
        } catch (e: Exception) {
            val now = java.util.Calendar.getInstance()
            LocalDateTime(
                now.get(java.util.Calendar.YEAR),
                now.get(java.util.Calendar.MONTH) + 1,
                now.get(java.util.Calendar.DAY_OF_MONTH),
                now.get(java.util.Calendar.HOUR_OF_DAY),
                now.get(java.util.Calendar.MINUTE)
            )
        }
    }

    private fun inferCategory(recipient: String): String {
        val r = recipient.lowercase(Locale.ENGLISH)
        return when {
            r.contains("kplc") -> "Utilities"
            r.contains("zuku") || r.contains("safaricom home") -> "Internet"
            r.contains("airtel") || r.contains("safaricom") -> "Airtime"
            r.contains("supermarket") || r.contains("naivas") || r.contains("carrefour") || r.contains("quickmart") -> "Groceries"
            r.contains("restaurant") || r.contains("cafe") || r.contains("kfc") || r.contains("java") -> "Dining"
            else -> "General"
        }
    }
}
