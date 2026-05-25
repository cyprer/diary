package com.cypress.diary.ui.state

import java.time.LocalDate
import java.time.YearMonth

data class DateSelectionState(
    val currentDate: LocalDate,
) {
    fun withYear(year: Int): DateSelectionState {
        val day = clampDay(year, currentDate.monthValue, currentDate.dayOfMonth)
        return copy(currentDate = LocalDate.of(year, currentDate.monthValue, day))
    }

    fun withMonth(month: Int): DateSelectionState {
        val day = clampDay(currentDate.year, month, currentDate.dayOfMonth)
        return copy(currentDate = LocalDate.of(currentDate.year, month, day))
    }

    fun withDay(day: Int): DateSelectionState {
        val clampedDay = clampDay(currentDate.year, currentDate.monthValue, day)
        return copy(currentDate = LocalDate.of(currentDate.year, currentDate.monthValue, clampedDay))
    }
}

fun clampDay(year: Int, month: Int, day: Int): Int {
    val maxDay = YearMonth.of(year, month).lengthOfMonth()
    return minOf(day, maxDay)
}

fun selectableYears(startYear: Int = 2025, endYear: Int = 2035): IntRange {
    return startYear..endYear
}
