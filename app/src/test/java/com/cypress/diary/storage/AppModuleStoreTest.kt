package com.cypress.diary.storage

import com.cypress.diary.ui.navigation.AppModule
import org.junit.Assert.assertEquals
import org.junit.Test

class AppModuleStoreTest {
    @Test
    fun defaultsToDiaryWhenNoValueIsSaved() {
        val store = AppModuleStore(InMemoryPreferenceStore())

        assertEquals(AppModule.Diary, store.load())
    }

    @Test
    fun savesAndLoadsAccountingModule() {
        val store = AppModuleStore(InMemoryPreferenceStore())

        store.save(AppModule.Accounting)

        assertEquals(AppModule.Accounting, store.load())
    }

    @Test
    fun savesAndLoadsTodoModule() {
        val store = AppModuleStore(InMemoryPreferenceStore())

        store.save(AppModule.Todo)

        assertEquals(AppModule.Todo, store.load())
    }

    @Test
    fun malformedValueFallsBackToDiary() {
        val prefs = InMemoryPreferenceStore()
        prefs.putString("app_module", "unknown")

        assertEquals(AppModule.Diary, AppModuleStore(prefs).load())
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
