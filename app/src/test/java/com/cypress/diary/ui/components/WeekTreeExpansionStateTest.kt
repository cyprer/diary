package com.cypress.diary.ui.components

import org.junit.Assert.assertFalse
import org.junit.Test

class WeekTreeExpansionStateTest {
    @Test
    fun defaultsToCollapsedForAllKeys() {
        val state = WeekTreeExpansionState(mutableMapOf())

        assertFalse(state.isExpanded("year-2025"))
        assertFalse(state.isExpanded("month-2025-1"))
        assertFalse(state.isExpanded("week-2025-1-1"))
    }
}
