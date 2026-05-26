package com.cypress.diary.parser

import com.cypress.diary.model.DiaryDay
import com.cypress.diary.model.DiaryWeek
import com.cypress.diary.model.WeekKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalDate

class DiaryMarkdownCodecTest {
    private val codec = DiaryMarkdownCodec()

    @Test
    fun parsesAndRendersCanonicalWeeklyMarkdown() {
        val markdown = """
            ---
            title: "第四周周记"
            published: 2026-05-22
            description: "第四周周记"
            tags: ["周报", "总结"]
            category: "周报"
            draft: false
            ---

            # 第四周周记

            本周先写一段简介。

            ## 5.22
              第一段内容
            ## 5.23
              第二段内容
        """.trimIndent()

        val parsed = codec.parse(markdown)

        assertEquals(WeekKey(2026, 5, 4), parsed.key)
        assertEquals("第四周周记", parsed.title)
        assertEquals("本周先写一段简介。", parsed.intro)
        assertEquals(LocalDate.of(2026, 5, 22), parsed.published)
        assertEquals("第四周周记", parsed.description)
        assertEquals(listOf("周报", "总结"), parsed.tags)
        assertEquals("周报", parsed.category)
        assertEquals(false, parsed.draft)
        assertEquals(2, parsed.days.size)
        assertEquals(LocalDate.of(2026, 5, 22), parsed.days[0].date)
        assertEquals("第一段内容", parsed.days[0].content)

        assertEquals(markdown.withoutDescriptionLine(), codec.render(parsed))
    }

    @Test
    fun renderAndParseRoundTripPreservesWeekData() {
        val week = DiaryWeek(
            key = WeekKey(2026, 2, 1),
            title = "第一周周记",
            intro = "本周简介第一行。\n  本周简介第二行。",
            published = LocalDate.of(2026, 2, 1),
            description = "第一周周记",
            tags = listOf("周报", "总结"),
            category = "周报",
            draft = false,
            days = listOf(
                DiaryDay(
                    date = LocalDate.of(2026, 2, 1),
                content = "周日内容\n继续写",
                ),
                DiaryDay(
                    date = LocalDate.of(2026, 2, 2),
                    content = "",
                ),
            ),
        )

        val markdown = codec.render(week)
        val reparsed = codec.parse(markdown)

        assertEquals(week, reparsed)
    }

    @Test
    fun rendersEmptyDaySectionsWithBlogSpacing() {
        val week = DiaryWeek(
            key = WeekKey(2026, 5, 4),
            title = "第四周周记",
            published = LocalDate.of(2026, 5, 22),
            description = "第四周周记",
            tags = listOf("周报", "总结"),
            category = "周报",
            draft = false,
            days = listOf(
                DiaryDay(LocalDate.of(2026, 5, 22), "第一段内容"),
                DiaryDay(LocalDate.of(2026, 5, 23), ""),
                DiaryDay(LocalDate.of(2026, 5, 24), "第二段内容"),
            ),
        )
        val expected = """
            ---
            title: "第四周周记"
            published: 2026-05-22
            tags: ["周报", "总结"]
            category: "周报"
            draft: false
            ---

            # 第四周周记

            ## 5.22
              第一段内容
            ## 5.23

            ## 5.24
              第二段内容
        """.trimIndent()

        val rendered = codec.render(week)

        assertEquals(expected, rendered)
        assertEquals(week, codec.parse(rendered))
    }

    @Test
    fun parsesFrontMatterAfterLeadingBlankLines() {
        val markdown = """


            ---
            title: "第一周周记"
            published: 2026-02-01
            description: "第一周周记"
            tags: ["周报", "总结"]
            category: "周报"
            draft: false
            ---

            # 第一周周记
        """.trimIndent()

        val parsed = codec.parse(markdown)

        assertEquals("第一周周记", parsed.title)
    }

    @Test
    fun usesTitleAsDescriptionWhenDescriptionIsMissing() {
        val markdown = """
            ---
            title: "week title"
            published: 2026-05-22
            tags: ["weekly"]
            category: "weekly"
            draft: false
            ---

            # week title

            ## 5.22
              diary body
        """.trimIndent()

        val parsed = codec.parse(markdown)

        assertEquals("week title", parsed.description)
        assertEquals("diary body", parsed.days.single().content)
    }

    @Test
    fun renderOmitsDescriptionFromFrontMatter() {
        val week = DiaryWeek(
            key = WeekKey(2026, 5, 4),
            title = "week title",
            intro = "",
            published = LocalDate.of(2026, 5, 22),
            description = "week title",
            tags = listOf("weekly"),
            category = "weekly",
            draft = false,
            days = listOf(DiaryDay(LocalDate.of(2026, 5, 22), "diary body")),
        )
        val expected = """
            ---
            title: "week title"
            published: 2026-05-22
            tags: ["weekly"]
            category: "weekly"
            draft: false
            ---

            # week title

            ## 5.22
              diary body
        """.trimIndent()

        assertEquals(expected, codec.render(week))
    }

    @Test
    fun rejectsMissingFrontMatterFence() {
        val markdown = """
            # 第一周周记
        """.trimIndent()

        assertThrows(IllegalArgumentException::class.java) {
            codec.parse(markdown)
        }
    }

    @Test
    fun rejectsDoubleHashTitleHeading() {
        val markdown = """
            ---
            title: "第一周周记"
            published: 2026-02-01
            description: "第一周周记"
            tags: ["周报", "总结"]
            category: "周报"
            draft: false
            ---

            ## 第一周周记
        """.trimIndent()

        assertThrows(IllegalArgumentException::class.java) {
            codec.parse(markdown)
        }
    }

    private fun String.withoutDescriptionLine(): String {
        return lines()
            .filterNot { it.trimStart().startsWith("description:") }
            .joinToString("\n")
    }
}
