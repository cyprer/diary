package com.cypress.diary.ui.screens

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SummaryScreenScrollTest {
    @Test
    fun doesNotScrollSummaryContentWhenWeekTabIsOpened() {
        assertFalse(shouldScrollSummaryContent(scrollRequest = 0))
    }

    @Test
    fun scrollsSummaryContentAfterTimelineDateClick() {
        assertTrue(shouldScrollSummaryContent(scrollRequest = 1))
    }
}
