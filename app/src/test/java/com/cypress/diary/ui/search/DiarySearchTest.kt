package com.cypress.diary.ui.search

import com.cypress.diary.model.DiaryDay
import com.cypress.diary.model.DiaryWeek
import com.cypress.diary.model.WeekKey
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DiarySearchTest {
    @Test
    fun returnsMatchingDaysWithExcerpt() {
        val weeks = listOf(
            week(
                DiaryDay(LocalDate.of(2026, 5, 24), "今天学了 Kotlin 和 Compose。"),
                DiaryDay(LocalDate.of(2026, 5, 25), "整理生活，写日记。"),
            ),
        )

        val results = searchDiaryWeeks(weeks, "compose")

        assertEquals(1, results.size)
        assertEquals(LocalDate.of(2026, 5, 24), results.first().date)
        assertEquals("今天学了 Kotlin 和 Compose。", results.first().excerpt)
    }

    @Test
    fun returnsEmptyResultsForBlankQuery() {
        val results = searchDiaryWeeks(
            weeks = listOf(week(DiaryDay(LocalDate.of(2026, 5, 24), "alpha"))),
            query = " ",
        )

        assertEquals(emptyList<DiarySearchResult>(), results)
    }

    private fun week(vararg days: DiaryDay): DiaryWeek {
        return DiaryWeek(
            key = WeekKey(2026, 5, 4),
            title = "第四周周记",
            intro = "",
            published = LocalDate.of(2026, 5, 24),
            description = "第四周周记",
            tags = emptyList(),
            category = "周报",
            draft = false,
            days = days.toList(),
        )
    }
}
