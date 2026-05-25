package com.cypress.diary.ui.editor

import com.cypress.diary.model.DiaryDay
import com.cypress.diary.model.DiaryWeek
import com.cypress.diary.model.WeekKey
import com.cypress.diary.parser.DiaryMarkdownCodec
import java.time.LocalDate

class DiaryEditContentBuilder(
    private val codec: DiaryMarkdownCodec = DiaryMarkdownCodec(),
) {
    fun updateDayContent(week: DiaryWeek, date: LocalDate, content: String): DiaryWeek {
        val updatedDays = week.days.map { day ->
            if (day.date == date) day.copy(content = content) else day
        }.let { days ->
            if (days.any { it.date == date }) days else days + DiaryDay(date, content)
        }

        return week.copy(days = updatedDays)
    }

    fun newWeek(date: LocalDate): DiaryWeek {
        val key = WeekKey.from(date)
        val title = "第${key.weekIndex}周周记"
        return DiaryWeek(
            key = key,
            title = title,
            intro = "",
            published = date,
            description = title,
            tags = listOf("周报", "总结"),
            category = "周报",
            draft = false,
            days = listOf(DiaryDay(date, "")),
        )
    }

    fun render(week: DiaryWeek): String = codec.render(week)
}
