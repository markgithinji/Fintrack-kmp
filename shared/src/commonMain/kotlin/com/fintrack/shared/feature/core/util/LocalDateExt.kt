package com.fintrack.shared.feature.core.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

fun LocalDate.shortDayName(): String {
    // 0 = Monday ... 6 = Sunday
    return when (this.dayOfWeek.ordinal) {
        0 -> "Mon"; 1 -> "Tue"; 2 -> "Wed"; 3 -> "Thu"
        4 -> "Fri"; 5 -> "Sat"; 6 -> "Sun"
        else -> ""
    }
}

fun LocalDate.formatAsShortDate(): String {
    val month = when (this.month) {
        Month.JANUARY -> "Jan"
        Month.FEBRUARY -> "Feb"
        Month.MARCH -> "Mar"
        Month.APRIL -> "Apr"
        Month.MAY -> "May"
        Month.JUNE -> "Jun"
        Month.JULY -> "Jul"
        Month.AUGUST -> "Aug"
        Month.SEPTEMBER -> "Sep"
        Month.OCTOBER -> "Oct"
        Month.NOVEMBER -> "Nov"
        Month.DECEMBER -> "Dec"
    }
    return "$month ${this.dayOfMonth}"
}