package com.fintrack.shared.feature.transaction.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.fintrack.shared.feature.transaction.ui.home.GreenIncome
import com.fintrack.shared.feature.transaction.ui.home.PinkExpense
import network.chaintech.cmpcharts.axis.AxisProperties
import network.chaintech.cmpcharts.common.components.Legends
import network.chaintech.cmpcharts.common.extensions.formatNumber
import network.chaintech.cmpcharts.common.extensions.getMaxElementInYAxis
import network.chaintech.cmpcharts.common.model.LegendLabel
import network.chaintech.cmpcharts.common.model.LegendsConfig
import network.chaintech.cmpcharts.common.model.Point
import network.chaintech.cmpcharts.ui.barchart.StackedBarChart
import network.chaintech.cmpcharts.ui.barchart.config.BarChartStyle
import network.chaintech.cmpcharts.ui.barchart.config.BarData
import network.chaintech.cmpcharts.ui.barchart.config.BarPlotData
import network.chaintech.cmpcharts.ui.barchart.config.GroupBar
import network.chaintech.cmpcharts.ui.barchart.config.GroupBarChartData
import network.chaintech.cmpcharts.ui.barchart.config.SelectionHighlightData

@Composable
fun DummyVerticalStackedBarChart() {
    // create dummy group bar data
    val groupBarData = listOf(
        GroupBar(
            label = "Mon",
            barList = listOf(
                BarData(Point(0f, 30f)), // Income
                BarData(Point(0f, 20f))  // Expense
            )
        ),
        GroupBar(
            label = "Tue",
            barList = listOf(
                BarData(Point(0f, 40f)),
                BarData(Point(0f, 25f))
            )
        ),
        GroupBar(
            label = "Wed",
            barList = listOf(
                BarData(Point(0f, 35f)),
                BarData(Point(0f, 30f))
            )
        ),
        GroupBar(
            label = "Thu",
            barList = listOf(
                BarData(Point(0f, 50f)),
                BarData(Point(0f, 20f))
            )
        ),
        GroupBar(
            label = "Fri",
            barList = listOf(
                BarData(Point(0f, 45f)),
                BarData(Point(0f, 25f))
            )
        ),
        GroupBar(
            label = "Sat",
            barList = listOf(
                BarData(Point(0f, 60f)),
                BarData(Point(0f, 30f))
            )
        ),
        GroupBar(
            label = "Sun",
            barList = listOf(
                BarData(Point(0f, 55f)),
                BarData(Point(0f, 35f))
            )
        )
    )

    // x-axis
    val xAxis = AxisProperties(
        font = FontFamily.Default,
        stepSize = 30.dp,
        stepCount = groupBarData.size - 1,
        initialDrawPadding = 48.dp,
        labelColor = GreenIncome,
        lineColor = GreenIncome,
        labelFormatter = { index -> groupBarData[index].label }
    )

    val yAxis = AxisProperties(
        font = FontFamily.Default,
        stepCount = 5,
        labelPadding = 20.dp,
        offset = 20.dp,
        labelColor = PinkExpense,
        lineColor = PinkExpense,
        labelFormatter = { index ->
            val valueList = groupBarData.map { groupBar ->
                groupBar.barList.sumOf { bar -> bar.point.y.toDouble() }.toFloat()
            }
            val maxElement = getMaxElementInYAxis(valueList.maxOrNull() ?: 0f, 5)
            (index * (maxElement / 5)).toInt().toString()
        },
        topPadding = 36.dp
    )

    // colors & legends
    val colorPaletteList = listOf(GreenIncome, PinkExpense)
    val legendsConfig = LegendsConfig(
        legendLabelList = listOf(
            LegendLabel(color = GreenIncome, name = "Income"),
            LegendLabel(color = PinkExpense, name = "Expense")
        ),
        gridColumnCount = 2
    )


    // bar plot data
    val groupBarPlotData = BarPlotData(
        groupBarList = groupBarData,
        barStyle = BarChartStyle(
            barWidth = 35.dp,
            selectionHighlightData = SelectionHighlightData(
                isHighlightFullBar = true,
                groupBarPopUpLabel = { name, value -> "$name: ${value.formatNumber()}" }
            )
        ),
        barColorPaletteList = colorPaletteList
    )

    // full chart data
    val groupBarChartData = GroupBarChartData(
        barPlotData = groupBarPlotData,
        xAxisProperty = xAxis,
        yAxisProperty = yAxis,
        paddingBetweenStackedBars = 4.dp
    )

    // render chart
    Column(Modifier.height(500.dp)) {
        StackedBarChart(
            modifier = Modifier.height(400.dp),
            groupBarChartData = groupBarChartData
        )
        Legends(legendsConfig = legendsConfig)
    }
}