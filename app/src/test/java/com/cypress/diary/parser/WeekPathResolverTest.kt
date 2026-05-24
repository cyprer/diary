package com.cypress.diary.parser

import com.cypress.diary.model.WeekKey
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class WeekPathResolverTest {
    private val resolver = WeekPathResolver()

    @Test
    fun resolvesMonthLocalWeekPaths() {
        assertEquals(
            "src/content/posts/summary/26year/5month/4week.md",
            resolver.resolve(LocalDate.of(2026, 5, 24)),
        )
        assertEquals(
            "src/content/posts/summary/26year/2month/1week.md",
            resolver.resolve(LocalDate.of(2026, 2, 1)),
        )
        assertEquals(
            "src/content/posts/summary/25year/10month/5week.md",
            resolver.resolve(LocalDate.of(2025, 10, 31)),
        )
        assertEquals(
            "src/content/posts/summary/25year/10month/4week.md",
            resolver.resolve(WeekKey(2025, 10, 4)),
        )
    }
}
