package com.cypress.diary.storage

import com.cypress.diary.model.DiaryDocument
import com.cypress.diary.model.DiaryDocumentType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DiaryDocumentCacheStoreTest {
    @Test
    fun roundTripsDocumentsByPath() {
        val prefs = InMemoryPreferenceStore()
        val store = DiaryDocumentCacheStore(prefs)
        val documents = listOf(
            DiaryDocument(
                path = "src/content/posts/summary/25year/index.md",
                type = DiaryDocumentType.Year,
                year = 2025,
                month = null,
                weekIndex = null,
                title = "2025年年度总结",
                published = LocalDate.of(2025, 1, 1),
                markdown = "# 2025年年度总结\n\n年度内容",
                body = "年度内容",
            ),
            DiaryDocument(
                path = "src/content/posts/summary/25year/1month/1week.md",
                type = DiaryDocumentType.Week,
                year = 2025,
                month = 1,
                weekIndex = 1,
                title = "第一周周记",
                published = LocalDate.of(2025, 1, 1),
                markdown = "# 第一周周记\n\n周内容",
                body = "周内容",
            ),
        )

        store.saveDocuments(documents)

        val loaded = store.loadDocuments()

        assertEquals(documents, loaded)
    }

    @Test
    fun replacingDocumentsRemovesOldCachedPaths() {
        val prefs = InMemoryPreferenceStore()
        val store = DiaryDocumentCacheStore(prefs)
        val oldDocument = document("src/content/posts/summary/25year/1month/1week.md", "old")
        val newDocument = document("src/content/posts/summary/25year/1month/2week.md", "new")

        store.saveDocuments(listOf(oldDocument))
        store.saveDocuments(listOf(newDocument))

        store.clear()

        assertEquals(emptyMap<String, String?>(), prefs.snapshot())
    }

    private fun document(path: String, title: String): DiaryDocument {
        return DiaryDocument(
            path = path,
            type = DiaryDocumentType.Week,
            year = 2025,
            month = 1,
            weekIndex = 1,
            title = title,
            published = LocalDate.of(2025, 1, 1),
            markdown = "# $title",
            body = title,
        )
    }

    private class InMemoryPreferenceStore : PreferenceStore {
        private val values = mutableMapOf<String, String?>()

        override fun getString(key: String, defaultValue: String?): String? {
            return values.getOrDefault(key, defaultValue)
        }

        override fun putString(key: String, value: String?) {
            values[key] = value
        }

        override fun remove(key: String) {
            values.remove(key)
        }

        fun snapshot(): Map<String, String?> = values.toMap()
    }
}
