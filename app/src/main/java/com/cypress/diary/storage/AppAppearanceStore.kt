package com.cypress.diary.storage

data class AppAppearanceState(
    val paletteName: String,
    val backgroundUri: String?,
)

class AppAppearanceStore(
    private val preferences: PreferenceStore,
) {
    constructor(prefs: android.content.SharedPreferences) : this(SharedPreferencesPreferenceStore(prefs))

    fun load(): AppAppearanceState {
        return AppAppearanceState(
            paletteName = preferences.getString(KEY_PALETTE, DEFAULT_PALETTE).orEmpty().ifBlank { DEFAULT_PALETTE },
            backgroundUri = preferences.getString(KEY_BACKGROUND_URI, null)?.trim()?.ifBlank { null },
        )
    }

    fun save(paletteName: String, backgroundUri: String?) {
        preferences.putString(KEY_PALETTE, paletteName.trim().ifBlank { DEFAULT_PALETTE })
        if (backgroundUri.isNullOrBlank()) {
            preferences.remove(KEY_BACKGROUND_URI)
        } else {
            preferences.putString(KEY_BACKGROUND_URI, backgroundUri.trim())
        }
    }

    companion object {
        const val DEFAULT_PALETTE = "BlueGray"
        private const val KEY_PALETTE = "palette_name"
        private const val KEY_BACKGROUND_URI = "background_uri"
    }
}
