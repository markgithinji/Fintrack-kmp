package com.fintrack.shared.feature.transaction.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.shared.feature.transaction.data.CategorySummary


@Composable
fun CategoryTotalsCardWithTabs(
    weeklySummary: Map<String, List<CategorySummary>>,
    monthlySummary: Map<String, List<CategorySummary>>,
    title: String = "Spending Distribution"
) {
    var selectedTab by remember { mutableStateOf(TimeSpan.WEEK) }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)) {

        // Title
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // ---- Tabs ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TimeSpan.values().forEach { span ->
                val isSelected = span == selectedTab
                val backgroundColor = if (isSelected) DarkCardBackground else Color.Transparent
                val textColor = if (isSelected) Color.White else Color.Black

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(backgroundColor)
                        .clickable { selectedTab = span }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = span.displayName,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                }
            }
        }

        // ---- Card ----
        val categories: List<CategorySummary> = when (selectedTab) {
            TimeSpan.WEEK -> weeklySummary.values.flatten()
            TimeSpan.MONTH -> monthlySummary.values.flatten()
            TimeSpan.YEAR -> monthlySummary.values.flatten() // TODO: Add year summary
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LightGreenCardBackground)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (categories.isEmpty()) {
                    Text(
                        text = "No data available",
                        fontSize = 14.sp,
                        color = CategoryTextColor
                    )
                } else {
                    categories.forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = category.category,
                                fontSize = 14.sp,
                                color = CategoryTextColor
                            )
                            Text(
                                text = formatCurrencyKmp(category.total),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkGreenSegment
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class TimeSpan(val displayName: String) {
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year")
}
