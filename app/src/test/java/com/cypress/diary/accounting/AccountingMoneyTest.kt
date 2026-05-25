package com.cypress.diary.accounting

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AccountingMoneyTest {
    @Test
    fun parsesYuanInputToCents() {
        assertEquals(12345L, parseAmountCents("123.45"))
        assertEquals(1200L, parseAmountCents("12"))
        assertEquals(1200L, parseAmountCents("12.0"))
        assertEquals(1205L, parseAmountCents("12.05"))
    }

    @Test
    fun rejectsInvalidAmounts() {
        assertNull(parseAmountCents(""))
        assertNull(parseAmountCents("abc"))
        assertNull(parseAmountCents("0"))
        assertNull(parseAmountCents("-1"))
        assertNull(parseAmountCents("12.345"))
    }

    @Test
    fun formatsCentsAsDecimalAmount() {
        assertEquals("0.01", formatAmountCents(1))
        assertEquals("12.00", formatAmountCents(1200))
        assertEquals("123.45", formatAmountCents(12345))
    }
}
