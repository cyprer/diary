package com.cypress.diary.ui.summary

import com.cypress.diary.model.DiaryDocument

data class SummaryWordPoint(
    val label: String,
    val wordCount: Int,
)

data class SummaryDayWordCount(
    val label: String,
    val content: String,
    val wordCount: Int,
)

fun countSummaryWords(text: String): Int {
    return text.count { !it.isWhitespace() }
}

fun weeklyWordCountsForMonth(
    tree: SummaryTree,
    year: Int,
    month: Int,
): List<SummaryWordPoint> {
    val weeks = tree.years
        .firstOrNull { it.year == year }
        ?.months
        ?.firstOrNull { it.month == month }
        ?.weeks
        ?.associateBy { it.weekIndex }
        .orEmpty()

    return (1..4).map { weekIndex ->
        SummaryWordPoint(
            label = "第${weekIndex}周",
            wordCount = weeks[weekIndex]?.document?.let(::weekWordCount) ?: 0,
        )
    }
}

fun monthlyWordCountsForYear(
    tree: SummaryTree,
    year: Int,
): List<SummaryWordPoint> {
    val months = tree.years
        .firstOrNull { it.year == year }
        ?.months
        ?.associateBy { it.month }
        .orEmpty()

    return (1..12).map { month ->
        SummaryWordPoint(
            label = "${month}月",
            wordCount = months[month]?.weeks?.sumOf { weekWordCount(it.document) } ?: 0,
        )
    }
}

fun weekDayWordCounts(document: DiaryDocument): List<SummaryDayWordCount> {
    return weekSummaryDays(document).map { day ->
        SummaryDayWordCount(
            label = "${day.date.monthValue}/${day.date.dayOfMonth}",
            content = day.content,
            wordCount = countSummaryWords(day.content),
        )
    }
}

fun weekWordCount(document: DiaryDocument): Int {
    return countSummaryWords(document.body) + weekSummaryDays(document).sumOf { countSummaryWords(it.content) }
}
