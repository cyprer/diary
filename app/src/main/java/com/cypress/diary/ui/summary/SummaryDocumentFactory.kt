package com.cypress.diary.ui.summary

import com.cypress.diary.model.DiaryDay
import com.cypress.diary.model.DiaryDocument
import com.cypress.diary.model.DiaryDocumentType
import com.cypress.diary.model.DiaryWeek
import com.cypress.diary.model.WeekKey
import com.cypress.diary.parser.DiaryMarkdownCodec
import com.cypress.diary.parser.WeekPathResolver
import java.time.LocalDate

fun newYearSummaryDocument(year: Int): DiaryDocument {
    val title = "${year}年年度总结"
    val published = LocalDate.of(year, 1, 1)
    return newSummaryDocument(
        path = "src/content/posts/summary/${year % 100}year/index.md",
        type = DiaryDocumentType.Year,
        year = year,
        month = null,
        weekIndex = null,
        title = title,
        published = published,
        markdown = renderSummaryMarkdown(title, published),
    )
}

fun newMonthSummaryDocument(year: Int, month: Int): DiaryDocument {
    val title = "${year}年${month}月总结"
    val published = LocalDate.of(year, month, 1)
    return newSummaryDocument(
        path = "src/content/posts/summary/${year % 100}year/${month}month/index.md",
        type = DiaryDocumentType.Month,
        year = year,
        month = month,
        weekIndex = null,
        title = title,
        published = published,
        markdown = renderSummaryMarkdown(title, published),
    )
}

fun newWeekSummaryDocument(key: WeekKey): DiaryDocument {
    val dates = monthLocalWeekDates(key.year, key.month, key.weekIndex)
    val published = dates.firstOrNull() ?: LocalDate.of(key.year, key.month, 1)
    val title = "${key.month}月第${key.weekIndex}周周记"
    val week = DiaryWeek(
        key = key,
        title = title,
        intro = "",
        published = published,
        description = title,
        tags = listOf("周报", "总结"),
        category = "周报",
        draft = false,
        days = dates.map { date -> DiaryDay(date = date, content = "") },
    )
    return newSummaryDocument(
        path = WeekPathResolver().resolve(key),
        type = DiaryDocumentType.Week,
        year = key.year,
        month = key.month,
        weekIndex = key.weekIndex,
        title = title,
        published = published,
        markdown = DiaryMarkdownCodec().render(week),
    )
}

private fun newSummaryDocument(
    path: String,
    type: DiaryDocumentType,
    year: Int,
    month: Int?,
    weekIndex: Int?,
    title: String,
    published: LocalDate,
    markdown: String,
): DiaryDocument {
    return DiaryDocument(
        path = path,
        type = type,
        year = year,
        month = month,
        weekIndex = weekIndex,
        title = title,
        published = published,
        markdown = markdown,
        body = "",
    )
}

private fun renderSummaryMarkdown(title: String, published: LocalDate): String {
    return """
        ---
        title: "${title}"
        published: ${published}
        tags: ["周报", "总结"]
        category: "周报"
        draft: false
        ---

        # ${title}
    """.trimIndent()
}
