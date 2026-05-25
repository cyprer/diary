package com.cypress.diary.github

import com.cypress.diary.model.WeekKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GitHubPathMatcherTest {
    @Test
    fun matchesOnlySummaryWeekMarkdownFiles() {
        assertTrue(isSummaryWeekPath("src/content/posts/summary/25year/1month/4week.md"))
        assertTrue(isSummaryWeekPath("src/content/posts/summary/26year/5month/1week.md"))
        assertFalse(isSummaryWeekPath("src/content/posts/summary/25year/index.md"))
        assertFalse(isSummaryWeekPath("src/content/posts/note/2025/1month/1week.md"))
    }

    @Test
    fun extractsWeekKeyFromSummaryWeekPath() {
        assertEquals(
            WeekKey(2025, 10, 4),
            extractWeekKeyFromSummaryPath("src/content/posts/summary/25year/10month/4week.md"),
        )
    }

    @Test
    fun matchesYearMonthAndWeekSummaryDocuments() {
        assertTrue(isSummaryDocumentPath("src/content/posts/summary/25year/index.md"))
        assertTrue(isSummaryDocumentPath("src/content/posts/summary/25year/10month/index.md"))
        assertTrue(isSummaryDocumentPath("src/content/posts/summary/25year/10month/4week.md"))
        assertFalse(isSummaryDocumentPath("src/content/posts/summary/25year/10month/readme.md"))
        assertFalse(isSummaryDocumentPath("src/content/posts/notes/25year/index.md"))
    }

    @Test
    fun extractsSummaryDocumentPathInfo() {
        assertEquals(
            SummaryDocumentPath.Year(year = 2025),
            extractSummaryDocumentPath("src/content/posts/summary/25year/index.md"),
        )
        assertEquals(
            SummaryDocumentPath.Month(year = 2025, month = 10),
            extractSummaryDocumentPath("src/content/posts/summary/25year/10month/index.md"),
        )
        assertEquals(
            SummaryDocumentPath.Week(key = WeekKey(2025, 10, 4)),
            extractSummaryDocumentPath("src/content/posts/summary/25year/10month/4week.md"),
        )
    }

    @Test
    fun sortsSummaryDocumentsByYearMonthAndWeek() {
        val paths = listOf(
            "src/content/posts/summary/25year/2month/1week.md",
            "src/content/posts/summary/25year/index.md",
            "src/content/posts/summary/25year/1month/2week.md",
            "src/content/posts/summary/25year/1month/index.md",
            "src/content/posts/summary/25year/1month/1week.md",
        )

        assertEquals(
            listOf(
                "src/content/posts/summary/25year/index.md",
                "src/content/posts/summary/25year/1month/index.md",
                "src/content/posts/summary/25year/1month/1week.md",
                "src/content/posts/summary/25year/1month/2week.md",
                "src/content/posts/summary/25year/2month/1week.md",
            ),
            sortSummaryDocumentPaths(paths),
        )
    }

    @Test
    fun buildsPublicSummaryDocumentCandidates() {
        val paths = candidateSummaryDocumentPaths(currentDate = LocalDate.of(2026, 2, 3))

        assertTrue(paths.contains("src/content/posts/summary/25year/index.md"))
        assertTrue(paths.contains("src/content/posts/summary/25year/12month/index.md"))
        assertTrue(paths.contains("src/content/posts/summary/25year/12month/4week.md"))
        assertTrue(paths.contains("src/content/posts/summary/26year/index.md"))
        assertTrue(paths.contains("src/content/posts/summary/26year/2month/index.md"))
        assertTrue(paths.contains("src/content/posts/summary/26year/2month/4week.md"))
        assertFalse(paths.contains("src/content/posts/summary/26year/3month/index.md"))
    }
}
