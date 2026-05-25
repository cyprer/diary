package com.cypress.diary.ui.calendar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class CalendarMonthTest {
    @Test
    fun buildsSundayFirstCalendarCells() {
        val cells = calendarMonthCells(YearMonth.of(2026, 5))

        assertEquals(42, cells.size)
        assertEquals(LocalDate.of(2026, 5, 1), cells[5])
        assertEquals(LocalDate.of(2026, 5, 31), cells[35])
        assertNull(cells[0])
    }

    @Test
    fun buildsFourRowsWhenMonthStartsSundayAndHasTwentyEightDays() {
        val cells = calendarMonthCells(YearMonth.of(2026, 2))

        assertEquals(28, cells.size)
        assertEquals(LocalDate.of(2026, 2, 1), cells[0])
        assertEquals(LocalDate.of(2026, 2, 28), cells[27])
    }

    @Test
    fun buildsSundayToSaturdayWeekDates() {
        assertEquals(
            listOf(
                LocalDate.of(2026, 4, 26),
                LocalDate.of(2026, 4, 27),
                LocalDate.of(2026, 4, 28),
                LocalDate.of(2026, 4, 29),
                LocalDate.of(2026, 4, 30),
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 2),
            ),
            calendarWeekDates(LocalDate.of(2026, 5, 1)),
        )
    }

    @Test
    fun calendarWeekNavigationMovesBySevenDays() {
        assertEquals(LocalDate.of(2026, 4, 24), previousCalendarWeekDate(LocalDate.of(2026, 5, 1)))
        assertEquals(LocalDate.of(2026, 5, 8), nextCalendarWeekDate(LocalDate.of(2026, 5, 1)))
    }

    @Test
    fun weekMonthTitleUsesMajorityMonthAtBoundary() {
        assertEquals("2026年4月", calendarWeekMonthTitle(LocalDate.of(2026, 5, 1)))
        assertEquals("2026年6月", calendarWeekMonthTitle(LocalDate.of(2026, 5, 31)))
    }

    @Test
    fun buildsAllMonthsForYear() {
        val months = calendarYearMonths(2026)

        assertEquals(12, months.size)
        assertEquals(YearMonth.of(2026, 1), months.first())
        assertEquals(YearMonth.of(2026, 12), months.last())
    }

    @Test
    fun buildsFiveWeekStartsForMonth() {
        val starts = calendarMonthWeekStarts(YearMonth.of(2026, 5))

        assertEquals(
            listOf(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 8),
                LocalDate.of(2026, 5, 15),
                LocalDate.of(2026, 5, 22),
                LocalDate.of(2026, 5, 29),
            ),
            starts,
        )
    }

    @Test
    fun buildsOnlyFourWeekStartsForTwentyEightDayMonth() {
        val starts = calendarMonthWeekStarts(YearMonth.of(2026, 2))

        assertEquals(
            listOf(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 8),
                LocalDate.of(2026, 2, 15),
                LocalDate.of(2026, 2, 22),
            ),
            starts,
        )
    }

}
