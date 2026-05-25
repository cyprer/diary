package com.cypress.diary.storage

import android.content.SharedPreferences

class SharedPreferencesPreferenceStore(
    private val prefs: SharedPreferences,
) : PreferenceStore {
    override fun getString(key: String, defaultValue: String?): String? {
        return prefs.getString(key, defaultValue)
    }

    override fun putString(key: String, value: String?) {
        prefs.edit().putString(key, value).apply()
    }

    override fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }
}
