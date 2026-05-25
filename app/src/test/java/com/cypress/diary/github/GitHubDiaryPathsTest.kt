package com.cypress.diary.github

import org.junit.Assert.assertEquals
import org.junit.Test

class GitHubDiaryPathsTest {
    @Test
    fun filtersAndSortsSummaryWeekPaths() {
        val paths = listOf(
            "src/content/posts/summary/26year/5month/index.md",
            "src/content/posts/summary/25year/2month/1week.md",
            "src/content/posts/note/other.md",
            "src/content/posts/summary/25year/1month/4week.md",
            "src/content/posts/summary/25year/1month/index.md",
        )

        assertEquals(
            listOf(
                "src/content/posts/summary/25year/1month/4week.md",
                "src/content/posts/summary/25year/2month/1week.md",
            ),
            sortSummaryWeekPaths(paths),
        )
    }
}
