package com.cypress.diary.storage

data class AppAppearanceState(
    val paletteName: String,
    val backgroundUri: String?,
    val layoutOpacity: Float,
)

class AppAppearanceStore(
    private val preferences: PreferenceStore,
) {
    constructor(prefs: android.content.SharedPreferences) : this(SharedPreferencesPreferenceStore(prefs))

    fun load(): AppAppearanceState {
        return AppAppearanceState(
            paletteName = preferences.getString(KEY_PALETTE, DEFAULT_PALETTE).orEmpty().ifBlank { DEFAULT_PALETTE },
            backgroundUri = preferences.getString(KEY_BACKGROUND_URI, null)?.trim()?.ifBlank { null },
            layoutOpacity = clampLayoutOpacity(
                preferences.getString(KEY_LAYOUT_OPACITY, DEFAULT_LAYOUT_OPACITY.toString())
                    ?.toFloatOrNull()
                    ?: DEFAULT_LAYOUT_OPACITY,
            ),
        )
    }

    fun save(
        paletteName: String,
        backgroundUri: String?,
        layoutOpacity: Float = DEFAULT_LAYOUT_OPACITY,
    ) {
        preferences.putString(KEY_PALETTE, paletteName.trim().ifBlank { DEFAULT_PALETTE })
        preferences.putString(KEY_LAYOUT_OPACITY, clampLayoutOpacity(layoutOpacity).toString())
        if (backgroundUri.isNullOrBlank()) {
            preferences.remove(KEY_BACKGROUND_URI)
        } else {
            preferences.putString(KEY_BACKGROUND_URI, backgroundUri.trim())
        }
    }

    private fun clampLayoutOpacity(value: Float): Float {
        return value.coerceIn(MIN_LAYOUT_OPACITY, MAX_LAYOUT_OPACITY)
    }

    companion object {
        const val DEFAULT_PALETTE = "BlueGray"
        const val DEFAULT_LAYOUT_OPACITY = 1f
        const val MIN_LAYOUT_OPACITY = 0.35f
        const val MAX_LAYOUT_OPACITY = 1f
        private const val KEY_PALETTE = "palette_name"
        private const val KEY_BACKGROUND_URI = "background_uri"
        private const val KEY_LAYOUT_OPACITY = "layout_opacity"
    }
}
