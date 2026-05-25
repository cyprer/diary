package com.cypress.diary.ui.summary

import com.cypress.diary.model.DiaryDocument
import com.cypress.diary.model.DiaryDocumentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class SummaryTreeBuilderTest {
    private val builder = SummaryTreeBuilder()

    @Test
    fun buildsYearMonthAndWeekDocumentTree() {
        val documents = listOf(
            document(
                path = "src/content/posts/summary/25year/1month/2week.md",
                type = DiaryDocumentType.Week,
                year = 2025,
                month = 1,
                weekIndex = 2,
                title = "第二周周记",
            ),
            document(
                path = "src/content/posts/summary/25year/index.md",
                type = DiaryDocumentType.Year,
                year = 2025,
                month = null,
                weekIndex = null,
                title = "2025年年度总结",
            ),
            document(
                path = "src/content/posts/summary/25year/1month/index.md",
                type = DiaryDocumentType.Month,
                year = 2025,
                month = 1,
                weekIndex = null,
                title = "2025年1月份summary",
            ),
            document(
                path = "src/content/posts/summary/25year/1month/1week.md",
                type = DiaryDocumentType.Week,
                year = 2025,
                month = 1,
                weekIndex = 1,
                title = "第一周周记",
            ),
        )

        val tree = builder.build(documents)

        assertEquals(listOf(2025), tree.years.map { it.year })
        assertEquals("2025年年度总结", tree.years.first().document?.title)
        assertEquals(listOf(1), tree.years.first().months.map { it.month })
        assertEquals("2025年1月份summary", tree.years.first().months.first().document?.title)
        assertEquals(listOf(1, 2), tree.years.first().months.first().weeks.map { it.weekIndex })
        assertEquals("第一周周记", tree.years.first().months.first().weeks.first().document.title)
    }

    @Test
    fun handlesMissingYearOrMonthIndexDocuments() {
        val documents = listOf(
            document(
                path = "src/content/posts/summary/25year/2month/1week.md",
                type = DiaryDocumentType.Week,
                year = 2025,
                month = 2,
                weekIndex = 1,
                title = "第一周周记",
            ),
        )

        val tree = builder.build(documents)

        assertNull(tree.years.first().document)
        assertNull(tree.years.first().months.first().document)
        assertEquals(1, tree.years.first().months.first().weeks.size)
    }

    @Test
    fun findsAdjacentWeekSummaryDocuments() {
        val documents = listOf(
            document("week-1.md", DiaryDocumentType.Week, 2026, 5, 1, "第一周"),
            document("week-2.md", DiaryDocumentType.Week, 2026, 5, 2, "第二周"),
            document("week-3.md", DiaryDocumentType.Week, 2026, 5, 3, "第三周"),
            document("month.md", DiaryDocumentType.Month, 2026, 5, null, "五月"),
        )
        val tree = builder.build(documents)

        assertEquals("week-1.md", tree.previousSummaryDocument(documents[1])?.path)
        assertEquals("week-3.md", tree.nextSummaryDocument(documents[1])?.path)
    }

    @Test
    fun findsAdjacentMonthAndYearSummaryDocumentsOnlyWithinSameType() {
        val documents = listOf(
            document("2025.md", DiaryDocumentType.Year, 2025, null, null, "2025"),
            document("2026.md", DiaryDocumentType.Year, 2026, null, null, "2026"),
            document("2026-01.md", DiaryDocumentType.Month, 2026, 1, null, "一月"),
            document("2026-02.md", DiaryDocumentType.Month, 2026, 2, null, "二月"),
            document("2026-w1.md", DiaryDocumentType.Week, 2026, 1, 1, "第一周"),
        )
        val tree = builder.build(documents)

        assertEquals("2025.md", tree.previousSummaryDocument(documents[1])?.path)
        assertEquals("2026-02.md", tree.nextSummaryDocument(documents[2])?.path)
        assertNull(tree.nextSummaryDocument(documents[1]))
    }

    private fun document(
        path: String,
        type: DiaryDocumentType,
        year: Int,
        month: Int?,
        weekIndex: Int?,
        title: String,
    ): DiaryDocument {
        return DiaryDocument(
            path = path,
            type = type,
            year = year,
            month = month,
            weekIndex = weekIndex,
            title = title,
            published = LocalDate.of(year, month ?: 1, 1),
            markdown = "# $title",
            body = "$title body",
        )
    }
}
