package com.fintrack.shared.feature.transaction.util

import com.fintrack.shared.feature.transaction.domain.model.Transaction
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalTime::class)
object MpesaParser {
    // Basic regex components
    private const val CODE = """([A-Z0-9]{10})"""
    private const val AMOUNT = """Ksh([\d,]+\.\d{2})"""
    private const val DATE = """(\d{1,2}/\d{1,2}/\d{2})"""
    private const val TIME = """(\d{1,2}:\d{2} [AP]M)"""

    private val sentRegex = """$CODE Confirmed\. $AMOUNT sent to (.*?) on $DATE at $TIME""".toRegex()
    private val receivedRegex = """$CODE Confirmed\. You have received $AMOUNT from (.*?) on $DATE at $TIME""".toRegex()
    private val paidRegex = """$CODE Confirmed\. $AMOUNT paid to (.*?) on $DATE at $TIME""".toRegex()
    private val depositRegex = """$CODE Confirmed\. $AMOUNT deposited to your M-PESA account by (.*?) on $DATE at $TIME""".toRegex()
    private val withdrawRegex = """$CODE Confirmed\. $AMOUNT withdrawn from (.*?) on $DATE at $TIME""".toRegex()
    
    private val costRegex = """Transaction cost, Ksh([\d,]+\.\d{2})""".toRegex()
    private val balanceRegex = """New M-PESA balance is Ksh([\d,]+\.\d{2})""".toRegex()

    private val dateFormat = SimpleDateFormat("dd/MM/yy h:mm a", Locale.ENGLISH)

    fun parse(message: String, accountId: String = "mpesa"): Transaction? {
        if (!message.contains("Confirmed") || !message.contains("Ksh")) return null

        val cost = parseAmount(costRegex.find(message)?.groupValues?.get(1))
        val balance = parseBalance(message)

        // Try matching different transaction types
        val match = sentRegex.find(message) ?: 
                    receivedRegex.find(message) ?: 
                    paidRegex.find(message) ?: 
                    depositRegex.find(message) ?: 
                    withdrawRegex.find(message)

        if (match != null) {
            val code = match.groupValues[1]
            val amount = parseAmount(match.groupValues[2])
            val party = match.groupValues[3]
            val date = match.groupValues[4]
            val time = match.groupValues[5]
            val dateTime = parseDateTime(date, time)

            val isIncome = message.contains("received") || message.contains("deposited")
            val typePrefix = when {
                message.contains("sent to") -> "Sent to"
                message.contains("received from") -> "Received from"
                message.contains("paid to") -> "Paid to"
                message.contains("deposited") -> "Deposit from"
                message.contains("withdrawn") -> "Withdrawn from"
                else -> "Transaction with"
            }

            return Transaction(
                accountId = accountId,
                isIncome = isIncome,
                amount = amount,
                transactionCost = cost,
                category = if (isIncome) "Income" else inferCategory(party),
                dateTime = dateTime,
                description = "$typePrefix $party (Ref: $code)",
                externalId = code,
                balance = balance
            )
        }

        return null
    }

    fun parseBalance(message: String): Double? {
        return balanceRegex.find(message)?.groupValues?.get(1)?.let { parseAmount(it) }
            .takeIf { it != null && (it > 0 || message.contains("balance is Ksh0.00")) }
    }

    private fun parseAmount(value: String?): Double {
        return value?.replace(",", "")?.toDoubleOrNull() ?: 0.0
    }

    private fun parseDateTime(date: String, time: String): Instant {
        return try {
            val dateStr = "$date $time"
            val parsedDate = dateFormat.parse(dateStr)
            if (parsedDate != null) {
                Instant.fromEpochMilliseconds(parsedDate.time)
            } else {
                Clock.System.now()
            }
        } catch (_: Exception) {
            Clock.System.now()
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
            else -> "Transfer"
        }
    }
}
