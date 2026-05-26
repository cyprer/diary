package com.cypress.diary.storage

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SharedPreferencesPreferenceStoreTest {
    @Test
    fun putStringCommitsSynchronously() {
        val prefs = CommitTrackingSharedPreferences()
        val store = SharedPreferencesPreferenceStore(prefs)

        store.putString("key", "value")

        assertEquals("value", prefs.values["key"])
        assertTrue(prefs.commitCount > 0)
        assertEquals(0, prefs.applyCount)
    }

    @Test
    fun removeCommitsSynchronously() {
        val prefs = CommitTrackingSharedPreferences()
        prefs.values["key"] = "value"
        val store = SharedPreferencesPreferenceStore(prefs)

        store.remove("key")

        assertTrue(!prefs.values.containsKey("key"))
        assertTrue(prefs.commitCount > 0)
        assertEquals(0, prefs.applyCount)
    }

    private class CommitTrackingSharedPreferences : SharedPreferences {
        val values = mutableMapOf<String, String?>()
        var commitCount = 0
        var applyCount = 0

        override fun getAll(): MutableMap<String, *> = values.toMutableMap()

        override fun getString(key: String?, defValue: String?): String? {
            return values[key] ?: defValue
        }

        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = defValues

        override fun getInt(key: String?, defValue: Int): Int = defValue

        override fun getLong(key: String?, defValue: Long): Long = defValue

        override fun getFloat(key: String?, defValue: Float): Float = defValue

        override fun getBoolean(key: String?, defValue: Boolean): Boolean = defValue

        override fun contains(key: String?): Boolean = values.containsKey(key)

        override fun edit(): SharedPreferences.Editor = Editor()

        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit

        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit

        private inner class Editor : SharedPreferences.Editor {
            override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                if (key != null) values[key] = value
                return this
            }

            override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = this

            override fun putInt(key: String?, value: Int): SharedPreferences.Editor = this

            override fun putLong(key: String?, value: Long): SharedPreferences.Editor = this

            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = this

            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = this

            override fun remove(key: String?): SharedPreferences.Editor {
                if (key != null) values.remove(key)
                return this
            }

            override fun clear(): SharedPreferences.Editor {
                values.clear()
                return this
            }

            override fun commit(): Boolean {
                commitCount += 1
                return true
            }

            override fun apply() {
                applyCount += 1
            }
        }
    }
}
