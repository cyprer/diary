package com.cypress.diary.ui.search

import com.cypress.diary.model.DiaryWeek
import java.time.LocalDate

data class DiarySearchResult(
    val date: LocalDate,
    val excerpt: String,
)

fun searchDiaryWeeks(
    weeks: List<DiaryWeek>,
    query: String,
    limit: Int = 8,
): List<DiarySearchResult> {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isBlank()) return emptyList()

    return weeks
        .flatMap { it.days }
        .filter { day -> day.content.contains(normalizedQuery, ignoreCase = true) }
        .sortedByDescending { it.date }
        .take(limit)
        .map { day ->
            DiarySearchResult(
                date = day.date,
                excerpt = day.content
                    .lineSequence()
                    .firstOrNull { it.isNotBlank() }
                    ?.trim()
                    .orEmpty(),
            )
        }
}
