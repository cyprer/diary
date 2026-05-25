package com.cypress.diary.ui.summary

import com.cypress.diary.model.DiaryDocument
import com.cypress.diary.model.DiaryDocumentType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class WeekSummaryContentTest {
    @Test
    fun readsOnlyDaysBelongingToTheSelectedWeekDocumentPath() {
        val document = weekDocument(
            path = "src/content/posts/summary/26year/5month/3week.md",
            markdown = """
                ---
                title: "week"
                published: 2026-05-01
                description: "week"
                tags: []
                category: "weekly"
                draft: false
                ---

                # week
                summary

                ## 5.1

                first week diary
                ## 5.15

                third week diary
            """.trimIndent(),
        )

        val days = weekSummaryDays(document)

        assertEquals(
            (15..21).map { LocalDate.of(2026, 5, it) },
            days.map { it.date },
        )
        assertEquals("third week diary", days.first { it.date == LocalDate.of(2026, 5, 15) }.content)
        assertEquals("", days.first { it.date == LocalDate.of(2026, 5, 16) }.content)
    }

    private fun weekDocument(path: String, markdown: String): DiaryDocument {
        return DiaryDocument(
            path = path,
            type = DiaryDocumentType.Week,
            year = 2026,
            month = 5,
            weekIndex = 3,
            title = "week",
            published = LocalDate.of(2026, 5, 1),
            markdown = markdown,
            body = "summary",
        )
    }
}
