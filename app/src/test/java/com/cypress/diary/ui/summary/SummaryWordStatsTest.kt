package com.cypress.diary.ui.summary

import com.cypress.diary.model.DiaryDocument
import com.cypress.diary.model.DiaryDocumentType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class SummaryWordStatsTest {
    @Test
    fun fourthWeekRunsToMonthEnd() {
        assertEquals(
            (22..31).map { LocalDate.of(2026, 5, it) },
            monthLocalWeekDates(year = 2026, month = 5, weekIndex = 4),
        )
    }

    @Test
    fun countsSummaryWordsWithoutWhitespace() {
        assertEquals(6, countSummaryWords("今天 很好\n继续"))
    }

    @Test
    fun returnsFourWeeklyWordCountsForMonth() {
        val tree = SummaryTreeBuilder().build(
            listOf(
                weekDocument(2026, 5, 1, body = "一二", dayContent = "三四五"),
                weekDocument(2026, 5, 4, body = "最后", dayContent = "一二三四"),
                weekDocument(2026, 6, 1, body = "别的月", dayContent = "不算"),
            ),
        )

        val points = weeklyWordCountsForMonth(tree, year = 2026, month = 5)

        assertEquals(listOf("第1周", "第2周", "第3周", "第4周"), points.map { it.label })
        assertEquals(listOf(5, 0, 0, 6), points.map { it.wordCount })
    }

    @Test
    fun returnsTwelveMonthlyWordCountsForYear() {
        val tree = SummaryTreeBuilder().build(
            listOf(
                weekDocument(2026, 1, 1, body = "一月", dayContent = "内容"),
                weekDocument(2026, 12, 4, body = "十二月", dayContent = "最后"),
                weekDocument(2025, 1, 1, body = "旧年", dayContent = "不算"),
            ),
        )

        val points = monthlyWordCountsForYear(tree, year = 2026)

        assertEquals((1..12).map { "${it}月" }, points.map { it.label })
        assertEquals(4, points[0].wordCount)
        assertEquals(0, points[1].wordCount)
        assertEquals(5, points[11].wordCount)
    }

    private fun weekDocument(
        year: Int,
        month: Int,
        weekIndex: Int,
        body: String,
        dayContent: String,
    ): DiaryDocument {
        val day = ((weekIndex - 1) * 7) + 1
        return DiaryDocument(
            path = "src/content/posts/summary/${year % 100}year/${month}month/${weekIndex}week.md",
            type = DiaryDocumentType.Week,
            year = year,
            month = month,
            weekIndex = weekIndex,
            title = "第${weekIndex}周",
            published = LocalDate.of(year, month, day),
            markdown = """
                ---
                title: "第${weekIndex}周"
                published: ${LocalDate.of(year, month, day)}
                description: "第${weekIndex}周"
                tags: []
                category: "weekly"
                draft: false
                ---

                # 第${weekIndex}周
                $body

                ## ${month}.${day}

                $dayContent
            """.trimIndent(),
            body = body,
        )
    }
}
