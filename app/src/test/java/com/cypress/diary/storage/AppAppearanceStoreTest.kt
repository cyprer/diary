package com.cypress.diary.storage

import org.junit.Assert.assertEquals
import org.junit.Test

class AppAppearanceStoreTest {
    @Test
    fun savesAndLoadsPaletteAndBackgroundUri() {
        val prefs = InMemoryPreferenceStore()
        val store = AppAppearanceStore(prefs)

        store.save("Mint", "content://media/external/images/media/42", 0.62f)

        val loaded = store.load()

        assertEquals("Mint", loaded.paletteName)
        assertEquals("content://media/external/images/media/42", loaded.backgroundUri)
        assertEquals(0.62f, loaded.layoutOpacity, 0.001f)
    }

    @Test
    fun clampsSavedLayoutOpacity() {
        val prefs = InMemoryPreferenceStore()
        val store = AppAppearanceStore(prefs)

        store.save("Mint", null, 0.1f)

        assertEquals(0.35f, store.load().layoutOpacity, 0.001f)
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
