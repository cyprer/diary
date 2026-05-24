package com.cypress.diary.model

import java.time.LocalDate

data class WeekKey(
    val year: Int,
    val month: Int,
    val weekIndex: Int,
) {
    init {
        require(year >= 0) { "year must be non-negative" }
        require(month in 1..12) { "month must be between 1 and 12" }
        require(weekIndex >= 1) { "weekIndex must be positive" }
    }

    companion object {
        fun from(date: LocalDate): WeekKey {
            val weekIndex = (((date.dayOfMonth - 1) / 7) + 1).coerceAtMost(4)
            return WeekKey(date.year, date.monthValue, weekIndex)
        }
    }
}
