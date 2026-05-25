package com.cypress.diary

import com.cypress.diary.model.DiaryDocument
import com.cypress.diary.model.DiaryDocumentType
import com.cypress.diary.model.DiaryDay
import com.cypress.diary.model.DiaryWeek
import com.cypress.diary.model.WeekKey
import com.cypress.diary.parser.DiaryMarkdownCodec
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiaryAppRefreshTest {
    @Test
    fun doesNotFetchRemoteOnInitialComposition() {
        assertFalse(shouldFetchRemoteDiary(refreshVersion = 0))
    }

    @Test
    fun fetchesRemoteAfterUserRefresh() {
        assertTrue(shouldFetchRemoteDiary(refreshVersion = 1))
    }

    @Test
    fun updatesDraftSnapshotAfterLocalEdit() {
        assertEquals(
            mapOf("draft-key" to "新的本地草稿"),
            updateDraftSnapshot(emptyMap(), "draft-key", "新的本地草稿"),
        )
    }

    @Test
    fun clearsDraftSnapshotAfterPush() {
        assertEquals(
            emptyMap<String, String>(),
            clearDraftSnapshot(mapOf("draft-key" to "已经推送的草稿"), "draft-key"),
        )
    }

    @Test
    fun appliesDayDraftsToExportedWeekDocuments() {
        val path = "src/content/posts/summary/26year/5month/4week.md"
        val codec = DiaryMarkdownCodec()

        val exported = documentsWithDraftsForExport(
            documents = listOf(
                document(
                    path = path,
                    type = DiaryDocumentType.Week,
                    markdown = """
                        ---
                        title: "week"
                        published: 2026-05-24
                        description: "week"
                        tags: []
                        category: "周报"
                        draft: false
                        ---

                        # week

                        ## 5.24

                        old
                    """.trimIndent(),
                ),
            ),
            drafts = mapOf(
                "$path#day-2026-05-24" to "new",
                "$path#day-2026-05-25" to "added",
            ),
            codec = codec,
        )

        val week = codec.parse(exported.first().markdown)

        assertEquals("new", week.days.first { it.date == LocalDate.of(2026, 5, 24) }.content)
        assertEquals("added", week.days.first { it.date == LocalDate.of(2026, 5, 25) }.content)
    }

    @Test
    fun appliesWholeDocumentDraftsToExportedSummaryDocuments() {
        val path = "src/content/posts/summary/26year/5month/index.md"

        val exported = documentsWithDraftsForExport(
            documents = listOf(
                document(
                    path = path,
                    type = DiaryDocumentType.Month,
                    markdown = "# remote",
                ),
            ),
            drafts = mapOf("document:$path" to "local body"),
            codec = DiaryMarkdownCodec(),
        )

        assertEquals("# remote\n\nlocal body", exported.first().markdown)
        assertEquals("local body", exported.first().body)
    }

    @Test
    fun upsertsSavedDocumentsByPath() {
        val original = document(
            path = "src/content/posts/summary/26year/5month/4week.md",
            type = DiaryDocumentType.Week,
            markdown = "# old",
        )
        val replacement = original.copy(markdown = "# new", body = "# new")

        val documents = upsertDiaryDocument(
            documents = listOf(original),
            document = replacement,
        )

        assertEquals(1, documents.size)
        assertEquals("# new", documents.first().markdown)
    }

    @Test
    fun doesNotUseAnotherWeekWhenSelectedDateHasNoMatchingWeek() {
        val firstWeek = diaryWeek(
            key = WeekKey(2026, 5, 1),
            date = LocalDate.of(2026, 5, 1),
            content = "first week",
        )

        val selected = findDiaryWeekByDate(
            weeks = listOf(firstWeek),
            date = LocalDate.of(2026, 5, 8),
        )

        assertEquals(null, selected)
    }

    private fun document(
        path: String,
        type: DiaryDocumentType,
        markdown: String,
    ): DiaryDocument {
        return DiaryDocument(
            path = path,
            type = type,
            year = 2026,
            month = 5,
            weekIndex = if (type == DiaryDocumentType.Week) 4 else null,
            title = path,
            published = LocalDate.of(2026, 5, 24),
            markdown = markdown,
            body = markdown,
        )
    }

    private fun diaryWeek(
        key: WeekKey,
        date: LocalDate,
        content: String,
    ): DiaryWeek {
        return DiaryWeek(
            key = key,
            title = "week",
            intro = "",
            published = date,
            description = "week",
            tags = emptyList(),
            category = "week",
            draft = false,
            days = listOf(DiaryDay(date, content)),
        )
    }
}
