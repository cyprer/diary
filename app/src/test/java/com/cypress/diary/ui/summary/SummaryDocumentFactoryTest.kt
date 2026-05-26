package com.cypress.diary.ui.summary

import com.cypress.diary.model.DiaryDocumentType
import com.cypress.diary.model.WeekKey
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SummaryDocumentFactoryTest {
    @Test
    fun createsMissingMonthSummaryDocument() {
        val document = newMonthSummaryDocument(year = 2026, month = 5)

        assertEquals("src/content/posts/summary/26year/5month/index.md", document.path)
        assertEquals(DiaryDocumentType.Month, document.type)
        assertEquals(2026, document.year)
        assertEquals(5, document.month)
        assertEquals(null, document.weekIndex)
        assertEquals(LocalDate.of(2026, 5, 1), document.published)
        assertEquals("", document.body)
        assertTrue(document.markdown.contains("published: 2026-05-01"))
    }

    @Test
    fun createsMissingWeekSummaryDocumentWithMonthLocalDates() {
        val document = newWeekSummaryDocument(WeekKey(2026, 5, 4))

        assertEquals("src/content/posts/summary/26year/5month/4week.md", document.path)
        assertEquals(DiaryDocumentType.Week, document.type)
        assertEquals(LocalDate.of(2026, 5, 22), document.published)
        assertEquals((22..31).map { LocalDate.of(2026, 5, it) }, weekSummaryDays(document).map { it.date })
    }
}
