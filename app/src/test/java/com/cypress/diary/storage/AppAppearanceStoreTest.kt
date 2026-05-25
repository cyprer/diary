package com.cypress.diary.storage

import org.junit.Assert.assertEquals
import org.junit.Test

class AppAppearanceStoreTest {
    @Test
    fun savesAndLoadsPaletteAndBackgroundUri() {
        val prefs = InMemoryPreferenceStore()
        val store = AppAppearanceStore(prefs)

        store.save("Mint", "content://media/external/images/media/42")

        val loaded = store.load()

        assertEquals("Mint", loaded.paletteName)
        assertEquals("content://media/external/images/media/42", loaded.backgroundUri)
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
