package com.cypress.diary.storage

import com.cypress.diary.model.accounting.AccountingCategory
import com.cypress.diary.model.accounting.AccountingRecordType
import java.nio.charset.StandardCharsets
import java.util.Base64

class AccountingCategoryStore(
    private val preferences: PreferenceStore,
) {
    constructor(prefs: android.content.SharedPreferences) : this(SharedPreferencesPreferenceStore(prefs))

    fun loadCategories(): List<AccountingCategory> {
        return preferences.getString(KEY_CATEGORIES, "")
            .orEmpty()
            .lineSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { line -> runCatching { decode(line) }.getOrNull() }
            .toList()
    }

    fun saveCategories(categories: List<AccountingCategory>) {
        preferences.putString(KEY_CATEGORIES, categories.joinToString("\n") { encode(it) })
    }

    companion object {
        private const val KEY_CATEGORIES = "accounting_categories"

        fun encode(category: AccountingCategory): String {
            return listOf(
                safe(category.key),
                safe(category.label),
                category.type.name,
            ).joinToString("|")
        }

        private fun decode(value: String): AccountingCategory {
            val parts = value.split('|')
            require(parts.size == 3) { "invalid accounting category" }
            return AccountingCategory(
                key = unsafe(parts[0]),
                label = unsafe(parts[1]),
                type = AccountingRecordType.valueOf(parts[2]),
            )
        }

        private fun safe(value: String): String {
            return Base64.getUrlEncoder().encodeToString(value.toByteArray(StandardCharsets.UTF_8))
        }

        private fun unsafe(value: String): String {
            return String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8)
        }
    }
}
