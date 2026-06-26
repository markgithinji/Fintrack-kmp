package com.fintrack.shared.feature.transaction.ui.home

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.compose.GreenIncome
import com.example.compose.PinkExpense
import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.settings.ui.toCurrencyString
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.core.ui.AnimatedShimmerBox
import com.fintrack.shared.feature.navigation.LocalSharedTransitionScope

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun IncomeExpenseCards(
    accountResult: Result<Account>,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onCardClick: (isIncome: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (accountResult) {
            is Result.Loading -> {
                LoadingInfoCard(modifier = Modifier.weight(1f))
                LoadingInfoCard(modifier = Modifier.weight(1f))
            }

            is Result.Error -> {
                InfoCard(
                    title = "Total Income",
                    amount = "Error",
                    isIncomeCard = true,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onClick = null,
                    modifier = Modifier.weight(1f)
                )
                InfoCard(
                    title = "Total Expense",
                    amount = "Error",
                    isIncomeCard = false,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onClick = null,
                    modifier = Modifier.weight(1f)
                )
            }

            is Result.Success -> {
                val account = accountResult.data
                val totalIncome = account.income ?: 0.0
                val totalExpense = account.expense ?: 0.0

                InfoCard(
                    title = "Total Income",
                    amount = totalIncome.toCurrencyString(),
                    isIncomeCard = true,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onClick = { onCardClick(true) },
                    modifier = Modifier.weight(1f)
                )

                InfoCard(
                    title = "Total Expense",
                    amount = totalExpense.toCurrencyString(),
                    isIncomeCard = false,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onClick = { onCardClick(false) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun LoadingInfoCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(70.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedShimmerBox(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                AnimatedShimmerBox(
                    modifier = Modifier
                        .width(60.dp)
                        .height(12.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                AnimatedShimmerBox(
                    modifier = Modifier
                        .width(80.dp)
                        .height(15.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun InfoCard(
    title: String,
    amount: String,
    isIncomeCard: Boolean,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: ((isIncome: Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current

    Card(
        modifier = modifier
            .height(70.dp)
            .then(
                if (sharedTransitionScope != null) {
                    with(sharedTransitionScope) {
                        Modifier.sharedElement(
                            sharedContentState = rememberSharedContentState(key = if (isIncomeCard) "income_card" else "expense_card"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }
                } else Modifier
            )
            .clickable(enabled = onClick != null) { onClick?.invoke(isIncomeCard) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isIncomeCard) GreenIncome else PinkExpense),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncomeCard) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = if (isIncomeCard) "Income" else "Expense",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp).rotate(if (isIncomeCard) 135f else -135f)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = amount,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

