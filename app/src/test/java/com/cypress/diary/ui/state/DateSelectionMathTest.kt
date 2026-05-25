package com.cypress.diary.ui.state

import org.junit.Assert.assertEquals
import org.junit.Test

class DateSelectionMathTest {
    @Test
    fun clampsDayToLastDayOfMonth() {
        assertEquals(28, clampDay(2026, 2, 31))
        assertEquals(30, clampDay(2026, 4, 31))
        assertEquals(24, clampDay(2026, 5, 24))
    }

    @Test
    fun yearOptionsStartAt2025() {
        assertEquals(listOf(2025, 2026, 2027), selectableYears(2025, 2027).toList())
    }
}
