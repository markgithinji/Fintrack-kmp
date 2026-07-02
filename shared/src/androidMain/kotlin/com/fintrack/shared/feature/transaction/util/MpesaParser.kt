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
    
    // M-Pesa amount format: Optional space/dot after Ksh, e.g., "Ksh1,000.00" or "Ksh. 1,000.00"
    private const val AMOUNT = """[Kk][Ss][Hh][\.\s]*$AMOUNT_VAL"""

    private const val DATE_TIME = """\s+on $DATE at $TIME"""
    private const val FOOTER = """(?:\.|\s+)(?:New M-PESA|Transaction cost|Amount you can transact|Your new M-PESA)"""
    private const val PARTY_END = """(?:$DATE_TIME|$FOOTER|$)"""

    // 1. Withdrawal with Date first: Confirmed.on 6/3/26 at 1:56 PMWithdraw Ksh800.00 from...
    private val withdrawDateFirstRegex = """$CODE\s+Confirmed[\.\s,]*on $DATE at $TIME\s?(?:Withdraw|Withdrawn) $AMOUNT from (.+?)$PARTY_END""".toRegex(RegexOption.IGNORE_CASE)
    
    // 2. Standard transactions (Sent, Received, Paid, Deposit, Withdrawal-standard)
    private val sentRegex = """$CODE\s+Confirmed[\.\s,]+$AMOUNT sent to (.+?)$PARTY_END""".toRegex(RegexOption.IGNORE_CASE)
    
    private val receivedRegex = """$CODE\s+Confirmed[\.\s,]+(?:You have received |Received |)$AMOUNT (?:from |received from )(.+?)$PARTY_END""".toRegex(RegexOption.IGNORE_CASE)
    
    private val paidRegex = """$CODE\s+Confirmed[\.\s,]+$AMOUNT paid to (.+?)$PARTY_END""".toRegex(RegexOption.IGNORE_CASE)
    
    private val depositRegex = """$CODE\s+Confirmed[\.\s,]+$AMOUNT deposited to your M-PESA account by (.+?)$PARTY_END""".toRegex(RegexOption.IGNORE_CASE)
    private val sentToYouRegex = """$CODE\s+Confirmed[\.\s,]+$AMOUNT was sent to you by (.+?)$PARTY_END""".toRegex(RegexOption.IGNORE_CASE)
    private val withdrawRegex = """$CODE\s+Confirmed[\.\s,]+$AMOUNT withdrawn from (.+?)$PARTY_END""".toRegex(RegexOption.IGNORE_CASE)
    
    // 3. Transfers (M-Shwari / Bank)
    private val transferFromRegex = """$CODE\s+Confirmed[\.\s,]+$AMOUNT transferred from (.+?)$PARTY_END""".toRegex(RegexOption.IGNORE_CASE)
    
    private val transferToRegex = """$CODE\s+Confirmed[\.\s,]+$AMOUNT transferred to (.+?)$PARTY_END""".toRegex(RegexOption.IGNORE_CASE)
    
    // 4. Loans
    private val loanRepayRegex = """$CODE\s+Confirmed[\.\s,]+Loan of $AMOUNT repaid from (M-PESA) on $DATE at $TIME""".toRegex(RegexOption.IGNORE_CASE)
    private val loanApprovedRegex = """$CODE\s+Confirmed[\.\s,]+Your M-Shwari loan has been approved .*? and $AMOUNT .*? deposited to your M-PESA account(?: on $DATE at $TIME)?""".toRegex(RegexOption.IGNORE_CASE)
    
    // 5. Fuliza Repayment
    private val fulizaRepayRegex = """$CODE\s+Confirmed[\.\s,]+$AMOUNT from your M-PESA has been used to (?:fully|partially) pay your outstanding Fuliza M-PESA(?: on $DATE at $TIME)?""".toRegex(RegexOption.IGNORE_CASE)
    
    // Auxiliary data regexes
    private val costRegex = """Transaction cost[\s,]+[Kk][Ss][Hh][\.\s]*$AMOUNT_VAL""".toRegex(RegexOption.IGNORE_CASE)

    private val dateFormat = SimpleDateFormat("d/M/yy h:mm a", Locale.ENGLISH)

    fun parse(message: String, accountId: String = "mpesa", smsTimestamp: Instant? = null): Transaction? {
        if (!message.contains("Confirmed", ignoreCase = true)) return null

        val cost = parseAmountFromMatch(costRegex.find(message))
        val balance = parseBalance(message)

        // Try Withdraw (Date First)
        withdrawDateFirstRegex.find(message)?.let {
            val code = it.groupValues[1]
            val date = it.groupValues[2]
            val time = it.groupValues[3]
            val amount = parseAmount(it.groupValues[4])
            val party = cleanPartyName(it.groupValues[5])
            return createTransactionModel(code, amount, cost, balance, "Transport", parseDateTime(date, time, smsTimestamp), "Withdrawn from $party", accountId, false)
        }

        // M-Shwari / Bank Transfers
        transferFromRegex.find(message)?.let { 
            val party = cleanPartyName(it.groupValues[3])
            val isMshwari = party.lowercase().contains("m-shwari")
            val date = it.groupValues.getOrNull(4) ?: ""
            val time = it.groupValues.getOrNull(5) ?: ""
            return createTransactionModel(
                it.groupValues[1], 
                parseAmount(it.groupValues[2]), 
                cost, 
                balance, 
                if (isMshwari) "Loans" else "Income", 
                parseDateTime(date, time, smsTimestamp), 
                "Transferred from $party", 
                accountId, 
                true
            )
        }
        transferToRegex.find(message)?.let { 
            val party = cleanPartyName(it.groupValues[3])
            val isMshwari = party.lowercase().contains("m-shwari")
            val date = it.groupValues.getOrNull(4) ?: ""
            val time = it.groupValues.getOrNull(5) ?: ""
            return createTransactionModel(
                it.groupValues[1], 
                parseAmount(it.groupValues[2]), 
                cost, 
                balance, 
                if (isMshwari) "Loans" else inferCategory(party), 
                parseDateTime(date, time, smsTimestamp), 
                "Transferred to $party", 
                accountId, 
                false
            )
        }
        
        // Loans
        loanApprovedRegex.find(message)?.let { 
            val code = it.groupValues[1]
            val amount = parseAmount(it.groupValues[2])
            val date = it.groupValues.getOrNull(3) ?: ""
            val time = it.groupValues.getOrNull(4) ?: ""
            return createTransactionModel(code, amount, cost, balance, "Loans", parseDateTime(date, time, smsTimestamp), "M-Shwari Loan Approved", accountId, true)
        }
        loanRepayRegex.find(message)?.let { return createFromMatch(it, false, "Loan Repaid from", "Loans", accountId, cost, balance, smsTimestamp) }
        
        // Fuliza
        fulizaRepayRegex.find(message)?.let {
            val code = it.groupValues[1]
            val amount = parseAmount(it.groupValues[2])
            val date = it.groupValues.getOrNull(3) ?: ""
            val time = it.groupValues.getOrNull(4) ?: ""
            return createTransactionModel(code, amount, cost, balance, "Loans", parseDateTime(date, time, smsTimestamp), "Fuliza M-PESA Repayment", accountId, false)
        }

        // Standard transactions
        sentRegex.find(message)?.let { return createFromMatch(it, false, "Sent to", null, accountId, cost, balance, smsTimestamp) }
        
        receivedRegex.find(message)?.let { return createFromMatch(it, true, "Received from", "Income", accountId, cost, balance, smsTimestamp) }
        sentToYouRegex.find(message)?.let { return createFromMatch(it, true, "Sent by", "Income", accountId, cost, balance, smsTimestamp) }
        
        paidRegex.find(message)?.let { return createFromMatch(it, false, "Paid to", null, accountId, cost, balance, smsTimestamp) }

        depositRegex.find(message)?.let { return createFromMatch(it, true, "Deposit from", "Income", accountId, cost, balance, smsTimestamp) }
        withdrawRegex.find(message)?.let { return createFromMatch(it, false, "Withdrawn from", "Transport", accountId, cost, balance, smsTimestamp) }

        return null
    }

    private fun createFromMatch(
        match: MatchResult, 
        isIncome: Boolean, 
        prefix: String, 
        fixedCategory: String?,
        accountId: String,
        cost: Double,
        balance: Double?,
        smsTimestamp: Instant? = null
    ): Transaction {
        val code = match.groupValues[1]
        val amount = parseAmount(match.groupValues[2])
        val rawParty = if (match.groupValues.size > 3) match.groupValues[3] else "M-PESA"
        val party = cleanPartyName(rawParty)
        val date = if (match.groupValues.size > 4) match.groupValues[4] else ""
        val time = if (match.groupValues.size > 5) match.groupValues[5] else ""
        
        return createTransactionModel(code, amount, cost, balance, fixedCategory ?: inferCategory(party), parseDateTime(date, time, smsTimestamp), "$prefix $party", accountId, isIncome)
    }

    private fun cleanPartyName(name: String): String {
        return name.split(Regex("(?i)New M-PESA|Transaction cost|Amount you can transact"))[0]
            .trim()
            .removeSuffix(".")
            .trim()
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
        // 1. Look for explicit M-PESA balance (highest priority)
        // Format examples: 
        // "M-PESA balance is Ksh1,234.56"
        // "New M-PESA balance is Ksh1,234.56"
        // "M-PESA balance is Ksh. 1,234.56"
        // "balance is KSH 1,234.56" (if clearly M-PESA)
        val mpesaRegex = """(?:New\s+)?M-?PESA\s+balance\s+is\s+(?:Ksh\.?\s*|KSH\s*)?([\d,]+\.\d{2})""".toRegex(RegexOption.IGNORE_CASE)
        mpesaRegex.find(message)?.let {
            return parseAmount(it.groupValues[1])
        }

        // 2. Look for "balance is" but ONLY if it's not an M-Shwari balance
        // We look for "balance is" and ensure "M-Shwari" or "MShwari" isn't in the immediate preceding text.
        val genericRegex = """balance\s+is\s+(?:Ksh\.?\s*|KSH\s*)?([\d,]+\.\d{2})""".toRegex(RegexOption.IGNORE_CASE)
        val allMatches = genericRegex.findAll(message)
        
        for (match in allMatches) {
            val startIdx = match.range.first
            // Look back to see if this balance is qualified by M-Shwari
            val lookbackStart = maxOf(0, startIdx - 50)
            val context = message.substring(lookbackStart, startIdx).lowercase()
            
            // If the balance is explicitly labeled as M-Shwari, skip it.
            if (context.contains("m-shwari") || context.contains("mshwari")) {
                continue
            }
            
            // If we found a generic balance and it's not M-Shwari, it's likely M-PESA
            return parseAmount(match.groupValues[1])
        }

        return null
    }

    private fun parseAmountFromMatch(match: MatchResult?): Double {
        return match?.groupValues?.get(1)?.let { parseAmount(it) } ?: 0.0
    }

    private fun parseAmount(value: String?): Double {
        return value?.replace(",", "")?.toDoubleOrNull() ?: 0.0
    }

    private fun parseDateTime(date: String, time: String, smsTimestamp: Instant? = null): Instant {
        if (date.isBlank() || time.isBlank()) return smsTimestamp ?: Clock.System.now()
        return try {
            val dateStr = "$date $time"
            val parsedDate = dateFormat.parse(dateStr)
            if (parsedDate != null) {
                val parsedInstant = Instant.fromEpochMilliseconds(parsedDate.time)
                
                // Use arrival time seconds/millis if within the same minute as the text
                if (smsTimestamp != null) {
                    val diff = kotlin.math.abs(parsedInstant.toEpochMilliseconds() - smsTimestamp.toEpochMilliseconds())
                    if (diff < 60000) smsTimestamp else parsedInstant
                } else {
                    parsedInstant
                }
            } else {
                smsTimestamp ?: Clock.System.now()
            }
        } catch (_: Exception) {
            smsTimestamp ?: Clock.System.now()
        }
    }

    private fun inferCategory(recipient: String): String {
        val r = recipient.lowercase(Locale.ENGLISH)
        return when {
            r.contains("kplc") || r.contains("tokens") || r.contains("power") -> "Utilities"
            r.contains("zuku") || r.contains("safaricom home") || r.contains("poa internet") -> "Internet"
            r.contains("airtime") || r.contains("safaricom data") || r.contains("bundles") || r.contains("tunukiwa") -> "Airtime"
            r.contains("supermarket") || r.contains("naivas") || r.contains("carrefour") || r.contains("quickmart") || r.contains("butchery") || r.contains("quick mart") || r.contains("friendly 5") -> "Groceries"
            r.contains("restaurant") || r.contains("cafe") || r.contains("kfc") || r.contains("java") || Regex("""\bbar\b""").containsMatchIn(r) || r.contains("lounge") || r.contains("chicken inn") || r.contains("pizza inn") || r.contains("creamy inn") || r.contains("choma place") -> "Dining Out"
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
