package com.cypress.diary.parser

import com.cypress.diary.model.DiaryDocumentType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DiaryDocumentCodecTest {
    private val codec = DiaryDocumentCodec()

    @Test
    fun parsesSummaryIndexDocumentForReading() {
        val markdown = """
            ---
            title: "2025年年度总结"
            published: 2025-01-01
            description: "年度总结"
            tags: ["周报", "总结"]
            category: "周报"
            draft: false
            ---

            # 2025年年度总结
              这一年发生了很多事。
        """.trimIndent()

        val document = codec.parse(
            path = "src/content/posts/summary/25year/index.md",
            markdown = markdown,
        )

        assertEquals(DiaryDocumentType.Year, document.type)
        assertEquals(2025, document.year)
        assertEquals(null, document.month)
        assertEquals(null, document.weekIndex)
        assertEquals("2025年年度总结", document.title)
        assertEquals(LocalDate.of(2025, 1, 1), document.published)
        assertEquals("这一年发生了很多事。", document.body.trim())
        assertEquals(markdown, document.markdown)
    }

    @Test
    fun parsesWeekDocumentAsSummaryDocument() {
        val markdown = """
            ---
            title: "第一周周记"
            published: 2025-01-01
            description: "第一周周记"
            tags: ["周报", "总结"]
            category: "周报"
            draft: false
            ---

            # 第一周周记

            周开头。

            ## 1.1

            第一天。
        """.trimIndent()

        val document = codec.parse(
            path = "src/content/posts/summary/25year/1month/1week.md",
            markdown = markdown,
        )

        assertEquals(DiaryDocumentType.Week, document.type)
        assertEquals(2025, document.year)
        assertEquals(1, document.month)
        assertEquals(1, document.weekIndex)
        assertEquals("第一周周记", document.title)
        assertEquals("周开头。\n\n## 1.1\n\n第一天。", document.body.trim())
    }
}
