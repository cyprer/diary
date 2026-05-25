package com.cypress.diary.storage

interface PreferenceStore {
    fun getString(key: String, defaultValue: String? = null): String?
    fun putString(key: String, value: String?)
    fun remove(key: String)
}
