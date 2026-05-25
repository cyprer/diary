package com.cypress.diary.github

import com.cypress.diary.model.WeekKey
import java.time.LocalDate

private val summaryWeekPathRegex =
    Regex("^src/content/posts/summary/(\\d{2})year/(\\d{1,2})month/(\\d{1,2})week\\.md$")
private val summaryYearIndexPathRegex =
    Regex("^src/content/posts/summary/(\\d{2})year/index\\.md$")
private val summaryMonthIndexPathRegex =
    Regex("^src/content/posts/summary/(\\d{2})year/(\\d{1,2})month/index\\.md$")

sealed class SummaryDocumentPath {
    abstract val year: Int
    abstract val month: Int?
    abstract val weekIndex: Int?

    data class Year(
        override val year: Int,
    ) : SummaryDocumentPath() {
        override val month: Int? = null
        override val weekIndex: Int? = null
    }

    data class Month(
        override val year: Int,
        override val month: Int,
    ) : SummaryDocumentPath() {
        override val weekIndex: Int? = null
    }

    data class Week(
        val key: WeekKey,
    ) : SummaryDocumentPath() {
        override val year: Int = key.year
        override val month: Int = key.month
        override val weekIndex: Int = key.weekIndex
    }
}

fun isSummaryWeekPath(path: String): Boolean = summaryWeekPathRegex.matches(path)

fun isSummaryDocumentPath(path: String): Boolean = extractSummaryDocumentPath(path) != null

fun extractWeekKeyFromSummaryPath(path: String): WeekKey? {
    val match = summaryWeekPathRegex.matchEntire(path) ?: return null
    val yearFolder = match.groupValues[1].toInt()
    val month = match.groupValues[2].toInt()
    val weekIndex = match.groupValues[3].toInt()
    return WeekKey(2000 + yearFolder, month, weekIndex)
}

fun extractSummaryDocumentPath(path: String): SummaryDocumentPath? {
    summaryYearIndexPathRegex.matchEntire(path)?.let { match ->
        return SummaryDocumentPath.Year(year = 2000 + match.groupValues[1].toInt())
    }
    summaryMonthIndexPathRegex.matchEntire(path)?.let { match ->
        return SummaryDocumentPath.Month(
            year = 2000 + match.groupValues[1].toInt(),
            month = match.groupValues[2].toInt(),
        )
    }
    return extractWeekKeyFromSummaryPath(path)?.let(SummaryDocumentPath::Week)
}

fun sortSummaryWeekPaths(paths: Collection<String>): List<String> {
    return paths
        .filter(::isSummaryWeekPath)
        .sortedWith(
            compareBy<String>(
                { extractWeekKeyFromSummaryPath(it)?.year ?: Int.MAX_VALUE },
                { extractWeekKeyFromSummaryPath(it)?.month ?: Int.MAX_VALUE },
                { extractWeekKeyFromSummaryPath(it)?.weekIndex ?: Int.MAX_VALUE },
            ),
        )
}

fun sortSummaryDocumentPaths(paths: Collection<String>): List<String> {
    return paths
        .filter(::isSummaryDocumentPath)
        .sortedWith(
            compareBy<String>(
                { extractSummaryDocumentPath(it)?.year ?: Int.MAX_VALUE },
                { extractSummaryDocumentPath(it)?.month ?: 0 },
                { documentTypeOrder(extractSummaryDocumentPath(it)) },
                { extractSummaryDocumentPath(it)?.weekIndex ?: 0 },
            ),
        )
}

fun candidateSummaryDocumentPaths(
    currentDate: LocalDate = LocalDate.now(),
    startYear: Int = 2025,
): List<String> {
    val paths = mutableListOf<String>()
    for (year in startYear..currentDate.year) {
        val yearFolder = year % 100
        paths += "src/content/posts/summary/${yearFolder}year/index.md"
        val maxMonth = if (year == currentDate.year) currentDate.monthValue else 12
        for (month in 1..maxMonth) {
            paths += "src/content/posts/summary/${yearFolder}year/${month}month/index.md"
            for (weekIndex in 1..4) {
                paths += "src/content/posts/summary/${yearFolder}year/${month}month/${weekIndex}week.md"
            }
        }
    }
    return paths
}

private fun documentTypeOrder(path: SummaryDocumentPath?): Int {
    return when (path) {
        is SummaryDocumentPath.Year -> 0
        is SummaryDocumentPath.Month -> 1
        is SummaryDocumentPath.Week -> 2
        null -> Int.MAX_VALUE
    }
}
