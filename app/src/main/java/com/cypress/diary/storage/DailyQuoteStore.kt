package com.cypress.diary.storage

import java.time.LocalDate

class DailyQuoteStore(
    private val preferences: PreferenceStore,
) {
    constructor(prefs: android.content.SharedPreferences) : this(SharedPreferencesPreferenceStore(prefs))

    fun load(date: LocalDate): String? {
        return preferences.getString(key(date), null)?.trim()?.ifBlank { null }
    }

    fun save(date: LocalDate, quote: String) {
        if (quote.isBlank()) return
        preferences.putString(key(date), quote.trim())
    }

    private fun key(date: LocalDate): String = "daily_quote:$date"
}
