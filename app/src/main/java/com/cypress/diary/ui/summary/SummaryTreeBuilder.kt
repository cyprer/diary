package com.cypress.diary.ui.summary

import com.cypress.diary.model.DiaryDocument
import com.cypress.diary.model.DiaryDocumentType

data class SummaryTree(
    val years: List<YearSummary>,
)

data class YearSummary(
    val year: Int,
    val document: DiaryDocument?,
    val months: List<MonthSummary>,
)

data class MonthSummary(
    val year: Int,
    val month: Int,
    val document: DiaryDocument?,
    val weeks: List<WeekSummary>,
)

data class WeekSummary(
    val year: Int,
    val month: Int,
    val weekIndex: Int,
    val document: DiaryDocument,
)

class SummaryTreeBuilder {
    fun build(documents: List<DiaryDocument>): SummaryTree {
        val years = documents
            .groupBy { it.year }
            .toSortedMap()
            .map { (year, yearDocuments) ->
                val yearDocument = yearDocuments.firstOrNull { it.type == DiaryDocumentType.Year }
                val monthNumbers = yearDocuments
                    .mapNotNull { it.month }
                    .distinct()
                    .sorted()

                YearSummary(
                    year = year,
                    document = yearDocument,
                    months = monthNumbers.map { month ->
                        val monthDocuments = yearDocuments.filter { it.month == month }
                        MonthSummary(
                            year = year,
                            month = month,
                            document = monthDocuments.firstOrNull { it.type == DiaryDocumentType.Month },
                            weeks = monthDocuments
                                .filter { it.type == DiaryDocumentType.Week && it.weekIndex != null }
                                .sortedBy { it.weekIndex }
                                .map { week ->
                                    WeekSummary(
                                        year = year,
                                        month = month,
                                        weekIndex = requireNotNull(week.weekIndex),
                                        document = week,
                                    )
                                },
                        )
                    },
                )
            }

        return SummaryTree(years = years)
    }
}

fun SummaryTree.previousSummaryDocument(selected: DiaryDocument): DiaryDocument? {
    return adjacentSummaryDocument(selected, offset = -1)
}

fun SummaryTree.nextSummaryDocument(selected: DiaryDocument): DiaryDocument? {
    return adjacentSummaryDocument(selected, offset = 1)
}

private fun SummaryTree.adjacentSummaryDocument(
    selected: DiaryDocument,
    offset: Int,
): DiaryDocument? {
    val documents = documentsOfType(selected.type)
    val selectedIndex = documents.indexOfFirst { it.path == selected.path }
    if (selectedIndex == -1) return null
    return documents.getOrNull(selectedIndex + offset)
}

private fun SummaryTree.documentsOfType(type: DiaryDocumentType): List<DiaryDocument> {
    return when (type) {
        DiaryDocumentType.Year -> years.mapNotNull { it.document }
        DiaryDocumentType.Month -> years.flatMap { year -> year.months.mapNotNull { it.document } }
        DiaryDocumentType.Week -> years.flatMap { year ->
            year.months.flatMap { month -> month.weeks.map { it.document } }
        }
    }
}
