package com.cypress.diary.ui.editor

import com.cypress.diary.model.DiaryDay
import com.cypress.diary.model.DiaryWeek
import com.cypress.diary.model.WeekKey
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DiaryEditContentBuilderTest {
    private val builder = DiaryEditContentBuilder()

    @Test
    fun updatesOnlyTheSelectedDayWhenEditingDayMode() {
        val original = DiaryWeek(
            key = WeekKey(2026, 5, 4),
            title = "第4周周记",
            intro = "",
            published = LocalDate.of(2026, 5, 22),
            description = "第4周周记",
            tags = emptyList(),
            category = "周报",
            draft = false,
            days = listOf(
                DiaryDay(LocalDate.of(2026, 5, 22), "old-a"),
                DiaryDay(LocalDate.of(2026, 5, 24), "old-b"),
            ),
        )

        val updated = builder.updateDayContent(original, LocalDate.of(2026, 5, 24), "new text")

        assertEquals("old-a", updated.days[0].content)
        assertEquals("new text", updated.days[1].content)
    }
}
