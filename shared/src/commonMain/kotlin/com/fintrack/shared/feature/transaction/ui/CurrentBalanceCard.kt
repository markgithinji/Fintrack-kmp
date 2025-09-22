package com.fintrack.shared.feature.transaction.ui


import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fintrack.shared.feature.account.domain.Account
import com.fintrack.shared.feature.account.ui.AccountsViewModel
import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.summary.domain.CategoryComparison
import com.fintrack.shared.feature.summary.domain.DaySummary
import com.fintrack.shared.feature.summary.domain.StatisticsSummary
import com.fintrack.shared.feature.summary.domain.OverviewSummary
import com.fintrack.shared.feature.summary.ui.StatisticsViewModel
import com.fintrack.shared.feature.transaction.model.AccountIcon
import kotlinx.datetime.LocalDate
import network.chaintech.chartsLib.ui.linechart.model.IntersectionPoint
import network.chaintech.cmpcharts.axis.AxisProperties
import network.chaintech.cmpcharts.common.extensions.formatToSinglePrecision
import network.chaintech.cmpcharts.common.model.Point
import network.chaintech.cmpcharts.common.ui.GridLinesUtil
import network.chaintech.cmpcharts.common.ui.SelectionHighlightPoint
import network.chaintech.cmpcharts.common.ui.SelectionHighlightPopUp
import network.chaintech.cmpcharts.common.ui.ShadowUnderLine
import network.chaintech.cmpcharts.ui.linechart.LineChart
import network.chaintech.cmpcharts.ui.linechart.model.Line
import network.chaintech.cmpcharts.ui.linechart.model.LineChartProperties
import network.chaintech.cmpcharts.ui.linechart.model.LinePlotData
import network.chaintech.cmpcharts.ui.linechart.model.LineStyle


@Composable
fun CurrentBalanceCard(
    account: Account,
    onChangeAccountClicked: () -> Unit
) {
    val balance = account.balance ?: 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            LowerRightWavesBackground(modifier = Modifier.matchParentSize())

            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = AccountIcon.fromAccountName(account.name).icon,
                        contentDescription = "Bank",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(account.name, fontSize = 12.sp, color = Color.White)
                }

                Button(
                    onClick = onChangeAccountClicked,
                    colors = ButtonDefaults.buttonColors(containerColor = LightGray),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(26.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(
                        text = "Change Account",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 8.dp)
            ) {
                val formattedBalance = remember(balance) { formatAmount(balance) }
                Text(
                    text = "Current Balance",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "KSh $formattedBalance",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
@Composable
fun AccountSelectionDialog(
    accounts: List<Account>,
    selectedAccountId: Int?,
    onAccountSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Text(
                    "Select Account",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color.LightGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable accounts list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(accounts) { acc ->
                        val isSelected = selectedAccountId == acc.id
                        val backgroundColor by animateColorAsState(
                            if (isSelected) Color(0xFFE0F7FA).copy(alpha = 0.5f)
                            else Color.Transparent
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(backgroundColor)
                                .clickable(
                                    onClick = { onAccountSelected(acc.id); onDismiss() },
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                )
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        color = if (isSelected) Color(0xFF00ACC1).copy(alpha = 0.1f)
                                        else Color(0xFFF0F0F0),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = AccountIcon.fromAccountName(acc.name).icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (isSelected) Color(0xFF00ACC1) else DarkGray
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(acc.name, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color(0xFF212121))
                                Text("KSh ${formatAmount(acc.balance ?: 0.0)}", fontSize = 12.sp, color = Color.Gray)
                            }

                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00ACC1), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        "Close",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@Composable
fun CurrentBalanceCardWrapper(
    accountsResult: Result<List<Account>>?,
    selectedAccountResult: Result<Account>?,
    onAccountSelected: (Int) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    when (accountsResult) {
        null, is Result.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(DarkGray),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        is Result.Error -> {
            val message = accountsResult.exception.message ?: "Unknown error"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: $message", color = Color.Red)
            }
        }

        is Result.Success -> {
            val accounts = accountsResult.data
            if (accounts.isEmpty()) {
                Text(
                    "No accounts found",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(DarkGray),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                return
            }

            val account = (selectedAccountResult as? Result.Success)?.data ?: accounts.first()

            CurrentBalanceCard(
                account = account,
                onChangeAccountClicked = { showDialog = true }
            )

            if (showDialog) {
                AccountSelectionDialog(
                    accounts = accounts,
                    selectedAccountId = account.id,
                    onAccountSelected = onAccountSelected,
                    onDismiss = { showDialog = false }
                )
            }
        }
    }
}
