package com.fintrack.shared.feature.account.domain.usecase

import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.account.domain.model.AccountType
import com.fintrack.shared.feature.account.domain.repository.AccountRepository
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.core.util.map

class GetAccountsUseCase(
    private val repository: AccountRepository
) {
    suspend operator fun invoke(): Result<List<Account>> {
        return repository.getAccounts().map { accounts ->
            // Priority sorting:
            // 1. System/Default accounts first
            // 2. Within defaults, M-Pesa then Equity first
            // 3. Followed by creation time (if available)
            accounts.sortedWith { a, b ->
                when {
                    a.isDefault != b.isDefault -> if (a.isDefault) -1 else 1
                    a.isDefault && a.type != b.type -> {
                        // M-Pesa first, then Equity, then others
                        when {
                            a.type == AccountType.MPESA -> -1
                            b.type == AccountType.MPESA -> 1
                            a.type == AccountType.EQUITY -> -1
                            b.type == AccountType.EQUITY -> 1
                            else -> 0
                        }
                    }
                    else -> {
                        val timeA = a.createdAt
                        val timeB = b.createdAt
                        if (timeA != null && timeB != null) {
                            timeA.compareTo(timeB)
                        } else {
                            0
                        }
                    }
                }
            }
        }
    }
}
