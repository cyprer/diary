package com.cypress.diary.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class AccountingStatsModeTest {
    @Test
    fun ordersModesFromYearToWeek() {
        assertEquals(
            listOf("年账", "月账", "周账"),
            AccountingStatsMode.values().map { it.label },
        )
    }
}
