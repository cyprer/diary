package com.cypress.diary.storage

import org.junit.Assert.assertEquals
import org.junit.Test

class EditorDraftStoreTest {
    @Test
    fun keepsDistinctDraftsForDayAndWeekModes() {
        val prefs = InMemoryPreferenceStore()
        val store = EditorDraftStore(prefs)

        store.save("src/content/posts/summary/25year/5month/4week.md#day-2026-05-24", "day")
        store.save("src/content/posts/summary/25year/5month/4week.md#week", "week")

        assertEquals("day", store.load("src/content/posts/summary/25year/5month/4week.md#day-2026-05-24"))
        assertEquals("week", store.load("src/content/posts/summary/25year/5month/4week.md#week"))
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
    }
}
