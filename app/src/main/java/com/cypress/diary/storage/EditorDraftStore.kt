package com.cypress.diary.storage

class EditorDraftStore(
    private val preferences: PreferenceStore,
) {
    constructor(prefs: android.content.SharedPreferences) : this(SharedPreferencesPreferenceStore(prefs))

    fun load(key: String): String? {
        return preferences.getString(storageKey(key), null)
    }

    fun loadAll(): Map<String, String> {
        return loadKeys().mapNotNull { key ->
            val value = load(key) ?: return@mapNotNull null
            key to value
        }.toMap()
    }

    fun save(key: String, value: String) {
        preferences.putString(storageKey(key), value)
        saveKeys((loadKeys() + key).distinct())
    }

    fun clear(key: String) {
        preferences.remove(storageKey(key))
        saveKeys(loadKeys() - key)
    }

    fun clearAll() {
        loadKeys().forEach { key ->
            preferences.remove(storageKey(key))
        }
        preferences.remove(KEY_INDEX)
    }

    private fun storageKey(key: String): String = "editor_draft:$key"

    private fun loadKeys(): List<String> {
        return preferences.getString(KEY_INDEX, "")
            .orEmpty()
            .split('\n')
            .filter { it.isNotBlank() }
    }

    private fun saveKeys(keys: List<String>) {
        if (keys.isEmpty()) {
            preferences.remove(KEY_INDEX)
        } else {
            preferences.putString(KEY_INDEX, keys.joinToString("\n"))
        }
    }

    private companion object {
        const val KEY_INDEX = "editor_draft_index"
    }
}
