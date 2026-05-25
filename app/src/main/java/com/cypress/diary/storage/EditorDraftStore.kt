package com.cypress.diary.storage

class EditorDraftStore(
    private val preferences: PreferenceStore,
) {
    constructor(prefs: android.content.SharedPreferences) : this(SharedPreferencesPreferenceStore(prefs))

    fun load(key: String): String? {
        return preferences.getString(storageKey(key), null)
    }

    fun save(key: String, value: String) {
        preferences.putString(storageKey(key), value)
    }

    fun clear(key: String) {
        preferences.remove(storageKey(key))
    }

    private fun storageKey(key: String): String = "editor_draft:$key"
}
