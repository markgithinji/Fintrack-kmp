package com.fintrack.shared.feature.transaction.util

import com.fintrack.shared.feature.transaction.domain.model.Transaction
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.time.Clock
import kotlin.time.Instant
import java.text.SimpleDateFormat
import java.util.Locale

object EquityParser {
    // Regex components
    private const val AMOUNT_VAL = """([\d,]+\.\d{1,2})"""
    private const val DATE_DASH = """(\d{2}-\d{2}-\d{4})"""
    private const val DATE_TEXT = """(\d{1,2}\s+\w{3}\s+\d{4})"""
    private const val TIME = """(\d{1,2}:\d{2}(?::\d{2})?)"""
    private const val TIME_AMPM = """(\d{1,2}:\d{2}(?::\d{2})?\s+[AP]M)"""

    // 1. Card Auth (Expense) - Explicitly Bank related
    private val cardAuthRegex = """CONFIRMED\.\s+(?:KES|USD)\s+$AMOUNT_VAL,\s+Auth for card\s+.*?\s+at\s+(.*?)\s+on\s+(\d{4}-\d{2}-\d{2}\s+$TIME)\s+Ref:(\w+)""".toRegex(RegexOption.IGNORE_CASE)
    
    // 2. Sent Money (Expense) - Explicitly from an account number
    private val sentRegex = """$AMOUNT_VAL\s+KES\s+has been successfully sent from\s+(\d+)\*+\s+to\s+.*?\.\s+Ref\.\s+(\w+)\s+on\s+$DATE_TEXT\s+at\s+$TIME\s+EAT""".toRegex(RegexOption.IGNORE_CASE)
    
    // 3. Drawn / Withdrawal (Expense) - Mentions account
    private val drawnRegex = """Dear (.*?)\s+KES\s+$AMOUNT_VAL\s+has been drawn from your account\s+(\d+)\.*?\s+Ref:(\w+)\s+on\s+(\d{2}-\w{3}-\d{4})\s+$TIME_AMPM""".toRegex(RegexOption.IGNORE_CASE)
    
    // 4. Bill Payment (Expense) - If it looks like a bank bill payment
    private val billPaymentRegex = """Confirmed,\s+Bill payment to\s+(.*?)\s+of (?:KES\.|Ksh\.)\s*$AMOUNT_VAL\s+for account\s+(\d+)\s+and Ref\.\s*(\w+)\s+on\s+$DATE_DASH\s+at\s+$TIME""".toRegex(RegexOption.IGNORE_CASE)
    
    // 5. Loan Approved (Income) - Credited to bank account
    private val loanApprovedRegex = """Confirmed\.\s+Your application is approved and credited to your account\s+(\d+)\.*?\.\s+Repayment amount of KES\s+$AMOUNT_VAL\s+will be due on\s+.*?\.\s+Reference\s+(\w+)""".toRegex(RegexOption.IGNORE_CASE)

    // 6. Deposited to Equity Account (Income)
    private val depositedRegex = """(?:Dear (.*?), )?Your transaction of (?:KES\.|Ksh\.|KShs\.|Ksh)\s*$AMOUNT_VAL\s+has successfully been deposited to Equity Account in favor of (.*?)\s+Ref\.\s+Number\s+(\w+)\s+on\s+$DATE_DASH\s+at\s+$TIME""".toRegex(RegexOption.IGNORE_CASE)

    // 7. Generic Transfer (Expense/Income) - Only if it has an Equity Ref (usually starts with EQ or is long)
    private val transferRegex = """Your transaction of (?:Kshs\.|KES\.|Ksh)\s*$AMOUNT_VAL\s+has been credited to\s+.*?\.\s+Ref\.\s+(EQ\w+)\.(?:\s+MPESA Ref\.\s+\w+)?\s+on\s+(\d{1,2}/\d{1,2}/\d{4})\s+at\s+$TIME""".toRegex(RegexOption.IGNORE_CASE)

    private val formats = listOf(
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH),
        SimpleDateFormat("d MMM yyyy HH:mm", Locale.ENGLISH),
        SimpleDateFormat("d/M/yyyy HH:mm:ss", Locale.ENGLISH),
        SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH),
        SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a", Locale.ENGLISH)
    )

    fun parse(message: String, accountId: String = "equity", smsTimestamp: Instant? = null): Transaction? {
        // Skip purely M-Pesa style messages that don't mention Equity account/card/ref
        // These are often just duplicate notifications from the Equitel sim
        if (message.contains("to your MPESA", ignoreCase = true) && !message.contains("Equity", ignoreCase = true)) return null
        if (message.contains("Till No.", ignoreCase = true) && !message.contains("Equity", ignoreCase = true)) return null
        if (message.contains("failed due to insufficient funds", ignoreCase = true)) return null
        if (message.contains("is due in", ignoreCase = true)) return null

        // 1. Card Auth
        cardAuthRegex.find(message)?.let {
            val amount = parseAmount(it.groupValues[1])
            val merchant = it.groupValues[2].trim()
            val dateTime = parseDateTime(it.groupValues[3], smsTimestamp)
            val code = it.groupValues[5]
            return createTransactionModel(code, amount, BigDecimal.ZERO, null, inferCategory(merchant), dateTime, "Card payment at $merchant", accountId, false)
        }

        // 2. Sent Money
        sentRegex.find(message)?.let {
            val amount = parseAmount(it.groupValues[1])
            val code = it.groupValues[2]
            val dateStr = "${it.groupValues[3]} ${it.groupValues[4]}"
            val dateTime = parseDateTime(dateStr, smsTimestamp)
            return createTransactionModel(code, amount, BigDecimal.ZERO, null, "Transfer", dateTime, "Bank Transfer", accountId, false)
        }

        // 3. Drawn
        drawnRegex.find(message)?.let {
            val amount = parseAmount(it.groupValues[2])
            val code = it.groupValues[4]
            val dateStr = "${it.groupValues[5]} ${it.groupValues[6]}"
            val dateTime = parseDateTime(dateStr, smsTimestamp)
            return createTransactionModel(code, amount, BigDecimal.ZERO, null, "Transfer", dateTime, "Withdrawal / Drawn", accountId, false)
        }

        // 4. Bill Payment
        billPaymentRegex.find(message)?.let {
            val merchant = it.groupValues[1].trim()
            val amount = parseAmount(it.groupValues[2])
            val code = it.groupValues[4]
            val dateStr = "${it.groupValues[5]} ${it.groupValues[6]}"
            val dateTime = parseDateTime(dateStr, smsTimestamp)
            return createTransactionModel(code, amount, BigDecimal.ZERO, null, inferCategory(merchant), dateTime, "Bill payment to $merchant", accountId, false)
        }

        // 5. Loan Approved
        loanApprovedRegex.find(message)?.let {
            val amount = parseAmount(it.groupValues[2])
            val code = it.groupValues[3]
            return createTransactionModel(code, amount, BigDecimal.ZERO, null, "Loans", smsTimestamp ?: Clock.System.now(), "Loan Approved", accountId, true)
        }

        // 6. Deposited
        depositedRegex.find(message)?.let {
            val amount = parseAmount(it.groupValues[2])
            val recipient = it.groupValues[3].trim()
            val code = it.groupValues[4]
            val dateStr = "${it.groupValues[5]} ${it.groupValues[6]}"
            val dateTime = parseDateTime(dateStr, smsTimestamp)
            
            val isMySelf = recipient.contains("MARK", ignoreCase = true)
            val description = if (isMySelf) "Cash Deposit" else "Paid to $recipient"
            
            return createTransactionModel(code, amount, BigDecimal.ZERO, null, if (isMySelf) "Other Income" else inferCategory(recipient), dateTime, description, accountId, isMySelf)
        }

        // 7. Generic Equity Transfer
        transferRegex.find(message)?.let {
            val amount = parseAmount(it.groupValues[1])
            val code = it.groupValues[2]
            val dateStr = "${it.groupValues[3]} ${it.groupValues[4]}"
            val dateTime = parseDateTime(dateStr, smsTimestamp)
            return createTransactionModel(code, amount, BigDecimal.ZERO, null, "Transfer", dateTime, "Bank Transaction", accountId, false)
        }

        return null
    }

    private fun parseAmount(value: String?): BigDecimal {
        return try {
            value?.replace(",", "")?.let { BigDecimal.parseString(it) } ?: BigDecimal.ZERO
        } catch (e: Exception) {
            BigDecimal.ZERO
        }
    }

    private fun parseDateTime(dateStr: String, smsTimestamp: Instant? = null): Instant {
        for (format in formats) {
            try {
                val date = format.parse(dateStr)
                if (date != null) return Instant.fromEpochMilliseconds(date.time)
            } catch (_: Exception) {}
        }
        return smsTimestamp ?: Clock.System.now()
    }

    private fun inferCategory(description: String): String {
        val d = description.lowercase()
        return when {
            d.contains("netflix") || d.contains("google") || d.contains("youtube") || d.contains("spotify") || d.contains("openai") || d.contains("chatgpt") -> "Subscriptions"
            d.contains("pharmacy") || d.contains("chemist") || d.contains("hospital") || d.contains("clinic") -> "Health"
            d.contains("kplc") || d.contains("token") || d.contains("power") -> "Utilities"
            d.contains("supermarket") || d.contains("groceries") || d.contains("naivas") || d.contains("carrefour") || d.contains("quickmart") -> "Groceries"
            d.contains("restaurant") || d.contains("cafe") || d.contains("dining") || d.contains("bar") || d.contains("inn") || d.contains("dishes") -> "Dining Out"
            d.contains("zimele") || d.contains("etica") -> "Savings"
            d.contains("loan") -> "Loans"
            else -> "Transfer"
        }
    }

    private fun createTransactionModel(
        code: String,
        amount: BigDecimal,
        cost: BigDecimal,
        balance: BigDecimal?,
        category: String,
        dateTime: Instant,
        description: String,
        accountId: String,
        isIncome: Boolean
    ): Transaction = Transaction(
        accountId = accountId,
        isIncome = isIncome,
        amount = amount,
        transactionCost = cost,
        category = category,
        dateTime = dateTime,
        description = "$description (Ref: $code)",
        externalId = code,
        balance = balance
    )

    fun parseBalance(message: String): BigDecimal? {
        // Look for common bank balance patterns
        // "Account balance is KES 1,234.56"
        // "available balance is KES 1,234.56"
        // "balance was KES 1,234.56"
        val balanceRegex = """(?:(?:account|available)\s+)?balance\s+(?:is|was)\s+(?:KES|KSH|Ksh\.?)\s*([\d,]+\.\d{2})""".toRegex(RegexOption.IGNORE_CASE)
        balanceRegex.find(message)?.let {
            return parseAmount(it.groupValues[1])
        }
        return null
    }
}
