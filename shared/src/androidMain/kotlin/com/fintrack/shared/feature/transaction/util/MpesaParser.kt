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
    private const val AMOUNT_VAL = """([\d,]+\.\d{2})"""
    private const val DATE = """(\d{1,2}/\d{1,2}/\d{2})"""
    private const val TIME = """(\d{1,2}:\d{2} [AP]M)"""
    
    // M-Pesa amount format: Optional space after Ksh, e.g., "Ksh1,000.00" or "Ksh 1,000.00"
    private const val AMOUNT = """Ksh\s?$AMOUNT_VAL"""

    // 1. Withdrawal with Date first: Confirmed.on 6/3/26 at 1:56 PMWithdraw Ksh800.00 from...
    private val withdrawDateFirstRegex = """$CODE Confirmed\.on $DATE at $TIME\s?Withdraw $AMOUNT from (.*)""".toRegex()
    
    // 2. Standard transactions (Sent, Received, Paid, Deposit, Withdrawal-standard)
    private val sentRegex = """$CODE Confirmed\.\s?$AMOUNT sent to (.*?) on $DATE at $TIME""".toRegex()
    private val receivedRegex = """$CODE Confirmed\.\s?You have received $AMOUNT from (.*?) on $DATE at $TIME""".toRegex()
    private val paidRegex = """$CODE Confirmed\.\s?$AMOUNT paid to (.*?) on $DATE at $TIME""".toRegex()
    private val depositRegex = """$CODE Confirmed\.\s?$AMOUNT deposited to your M-PESA account by (.*?) on $DATE at $TIME""".toRegex()
    private val withdrawRegex = """$CODE Confirmed\.\s?$AMOUNT withdrawn from (.*?) on $DATE at $TIME""".toRegex()
    
    // 3. Transfers (M-Shwari / Bank)
    private val shwariFromRegex = """$CODE Confirmed\.\s?$AMOUNT transferred from (M-Shwari account) on $DATE at $TIME""".toRegex()
    private val shwariToRegex = """$CODE Confirmed\.\s?$AMOUNT transferred to (M-Shwari account) on $DATE at $TIME""".toRegex()
    
    // 4. Loans
    private val loanRepayRegex = """$CODE Confirmed\.\s?Loan of $AMOUNT repaid from (M-PESA) on $DATE at $TIME""".toRegex()
    private val loanApprovedRegex = """$CODE Confirmed\.\s?Your M-Shwari loan has been approved .*? and $AMOUNT .*? deposited to your M-PESA account""".toRegex()
    
    // 5. Fuliza Repayment
    private val fulizaRepayRegex = """$CODE\s?Confirmed\.\s?Ksh\s?$AMOUNT_VAL from your M-PESA has been used to (?:fully|partially) pay your outstanding Fuliza M-PESA""".toRegex()
    
    // Auxiliary data regexes
    private val costRegex = """Transaction cost(?:,?\s?Ksh\.?\s?)$AMOUNT_VAL""".toRegex()
    private val balanceRegex = """(?:M-PESA balance is|balance is|balance is KSH)\s?(?:Ksh\s?)?$AMOUNT_VAL""".toRegex()

    private val dateFormat = SimpleDateFormat("d/M/yy h:mm a", Locale.ENGLISH)

    fun parse(message: String, accountId: String = "mpesa"): Transaction? {
        if (!message.contains("Confirmed")) return null

        val cost = parseAmountFromMatch(costRegex.find(message))
        val balance = parseBalance(message)

        // Try Withdraw (Date First)
        withdrawDateFirstRegex.find(message)?.let {
            val code = it.groupValues[1]
            val date = it.groupValues[2]
            val time = it.groupValues[3]
            val amount = parseAmount(it.groupValues[4])
            val party = it.groupValues[5].trim().removeSuffix(".")
            return createTransactionModel(code, amount, cost, balance, "Transport", parseDateTime(date, time), "Withdrawn from $party", accountId, false)
        }

        // M-Shwari / Bank Transfers
        shwariFromRegex.find(message)?.let { return createFromMatch(it, true, "Transferred from", "Loans", accountId, cost, balance) }
        shwariToRegex.find(message)?.let { return createFromMatch(it, false, "Transferred to", "Loans", accountId, cost, balance) }
        
        // Loans
        loanApprovedRegex.find(message)?.let { 
            val amount = parseAmount(it.groupValues[2])
            return createTransactionModel(it.groupValues[1], amount, cost, balance, "Loans", Clock.System.now(), "M-Shwari Loan Approved", accountId, true)
        }
        loanRepayRegex.find(message)?.let { return createFromMatch(it, false, "Loan Repaid from", "Loans", accountId, cost, balance) }
        
        // Fuliza
        fulizaRepayRegex.find(message)?.let {
            val code = it.groupValues[1]
            val amount = parseAmount(it.groupValues[2])
            return createTransactionModel(code, amount, cost, balance, "Loans", Clock.System.now(), "Fuliza M-PESA Repayment", accountId, false)
        }

        // Standard transactions
        sentRegex.find(message)?.let { return createFromMatch(it, false, "Sent to", null, accountId, cost, balance) }
        receivedRegex.find(message)?.let { return createFromMatch(it, true, "Received from", "Income", accountId, cost, balance) }
        paidRegex.find(message)?.let { return createFromMatch(it, false, "Paid to", null, accountId, cost, balance) }
        depositRegex.find(message)?.let { return createFromMatch(it, true, "Deposit from", "Income", accountId, cost, balance) }
        withdrawRegex.find(message)?.let { return createFromMatch(it, false, "Withdrawn from", "Transport", accountId, cost, balance) }

        return null
    }

    private fun createFromMatch(
        match: MatchResult, 
        isIncome: Boolean, 
        prefix: String, 
        fixedCategory: String?,
        accountId: String,
        cost: Double,
        balance: Double?
    ): Transaction {
        val code = match.groupValues[1]
        val amount = parseAmount(match.groupValues[2])
        val party = if (match.groupValues.size > 3) match.groupValues[3].trim().removeSuffix(".") else "M-PESA"
        val date = if (match.groupValues.size > 4) match.groupValues[4] else ""
        val time = if (match.groupValues.size > 5) match.groupValues[5] else ""
        
        return createTransactionModel(code, amount, cost, balance, fixedCategory ?: inferCategory(party), parseDateTime(date, time), "$prefix $party", accountId, isIncome)
    }

    private fun createTransactionModel(
        code: String,
        amount: Double,
        cost: Double,
        balance: Double?,
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

    fun parseBalance(message: String): Double? {
        val match = balanceRegex.find(message)
        return match?.groupValues?.get(1)?.let { parseAmount(it) }
    }

    private fun parseAmountFromMatch(match: MatchResult?): Double {
        return match?.groupValues?.get(1)?.let { parseAmount(it) } ?: 0.0
    }

    private fun parseAmount(value: String?): Double {
        return value?.replace(",", "")?.toDoubleOrNull() ?: 0.0
    }

    private fun parseDateTime(date: String, time: String): Instant {
        if (date.isBlank() || time.isBlank()) return Clock.System.now()
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
            r.contains("kplc") || r.contains("tokens") || r.contains("power") -> "Utilities"
            r.contains("zuku") || r.contains("safaricom home") || r.contains("poa internet") -> "Internet"
            r.contains("airtime") || r.contains("safaricom data") || r.contains("bundles") || r.contains("tunukiwa") -> "Airtime"
            r.contains("supermarket") || r.contains("naivas") || r.contains("carrefour") || r.contains("quickmart") || r.contains("butchery") || r.contains("quick mart") || r.contains("friendly 5") -> "Groceries"
            r.contains("restaurant") || r.contains("cafe") || r.contains("kfc") || r.contains("java") || r.contains("bar") || r.contains("lounge") || r.contains("chicken inn") || r.contains("pizza inn") || r.contains("creamy inn") || r.contains("choma place") -> "Dining Out"
            r.contains("equity") || r.contains("co-operative") || r.contains("kcb") || r.contains("bank") || r.contains("i&m") || r.contains("ncba") || r.contains("boa") || r.contains("family bank") -> "Bank"
            r.contains("loan") || r.contains("fuliza") || r.contains("m-shwari") || r.contains("tala") || r.contains("branch") -> "Loans"
            r.contains("tithe") || r.contains("offering") || r.contains("citam") || r.contains("church") || r.contains("charity") || r.contains("mosque") -> "Charity"
            r.contains("parking") || r.contains("kaps") || r.contains("bolt") || r.contains("uber") || r.contains("taxi") -> "Transport"
            r.contains("chemist") || r.contains("pharmacy") || r.contains("hospital") || r.contains("health") || r.contains("clinic") || r.contains("meds") -> "Health"
            r.contains("netflix") || r.contains("spotify") || r.contains("showmax") || r.contains("youtube") -> "Subscriptions"
            r.contains("jumia") || r.contains("leather") || r.contains("watches") || r.contains("perfume") || r.contains("clothes") || r.contains("fashion") -> "Shopping"
            r.contains("hardware") || r.contains("timber") || r.contains("maintenance") || r.contains("repair") -> "Maintenance"
            else -> "Transfer"
        }
    }
}
