package com.cypress.diary.storage

import com.cypress.diary.model.DiaryDay
import com.cypress.diary.model.DiaryWeek
import com.cypress.diary.model.WeekKey
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DiaryWeekCacheStoreTest {
    @Test
    fun roundTripsWeeksThroughLocalCache() {
        val prefs = InMemoryPreferenceStore()
        val store = DiaryWeekCacheStore(prefs)
        val weeks = listOf(
            DiaryWeek(
                key = WeekKey(2025, 1, 1),
                title = "2025 1",
                intro = "",
                published = LocalDate.of(2025, 1, 1),
                description = "2025 1",
                tags = emptyList(),
                category = "周报",
                draft = false,
                days = listOf(DiaryDay(LocalDate.of(2025, 1, 1), "alpha")),
            ),
        )

        store.saveWeeks(weeks)

        val loaded = store.loadWeeks()

        assertEquals(1, loaded.size)
        assertEquals("alpha", loaded.first().days.first().content)
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
