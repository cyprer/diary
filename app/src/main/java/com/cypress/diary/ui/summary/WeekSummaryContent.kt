package com.cypress.diary.ui.summary

import com.cypress.diary.model.DiaryDay
import com.cypress.diary.model.DiaryDocument
import com.cypress.diary.model.DiaryDocumentType
import com.cypress.diary.parser.DiaryMarkdownCodec
import java.time.LocalDate
import java.time.YearMonth

fun weekSummaryDays(document: DiaryDocument): List<DiaryDay> {
    if (document.type != DiaryDocumentType.Week) return emptyList()
    val dates = monthLocalWeekDates(
        year = document.year,
        month = document.month ?: return emptyList(),
        weekIndex = document.weekIndex ?: return emptyList(),
    )
    val parsedDays = runCatching { DiaryMarkdownCodec().parse(document.markdown).days }
        .getOrDefault(emptyList())
        .associateBy { it.date }
    return dates.map { date ->
        DiaryDay(
            date = date,
            content = parsedDays[date]?.content.orEmpty(),
        )
    }
}

fun monthLocalWeekDates(
    year: Int,
    month: Int,
    weekIndex: Int,
): List<LocalDate> {
    val yearMonth = YearMonth.of(year, month)
    val startDay = ((weekIndex - 1) * 7) + 1
    if (startDay > yearMonth.lengthOfMonth()) return emptyList()
    val endDay = if (weekIndex >= 4) yearMonth.lengthOfMonth() else minOf(startDay + 6, yearMonth.lengthOfMonth())
    return (startDay..endDay).map { day -> yearMonth.atDay(day) }
}
