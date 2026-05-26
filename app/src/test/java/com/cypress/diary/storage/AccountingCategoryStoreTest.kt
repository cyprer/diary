package com.cypress.diary.storage

import com.cypress.diary.model.accounting.AccountingCategory
import com.cypress.diary.model.accounting.AccountingRecordType
import org.junit.Assert.assertEquals
import org.junit.Test

class AccountingCategoryStoreTest {
    @Test
    fun savesAndLoadsCustomCategories() {
        val prefs = InMemoryPreferenceStore()
        val store = AccountingCategoryStore(prefs)
        val categories = listOf(
            AccountingCategory("custom-food", "咖啡", AccountingRecordType.Expense),
            AccountingCategory("custom-income", "兼职", AccountingRecordType.Income),
        )

        store.saveCategories(categories)

        assertEquals(categories, store.loadCategories())
    }

    @Test
    fun skipsDamagedCategoryRows() {
        val prefs = InMemoryPreferenceStore()
        prefs.putString(
            "accounting_categories",
            listOf(
                AccountingCategoryStore.encode(AccountingCategory("good", "咖啡", AccountingRecordType.Expense)),
                "bad-row",
            ).joinToString("\n"),
        )

        assertEquals(
            listOf(AccountingCategory("good", "咖啡", AccountingRecordType.Expense)),
            AccountingCategoryStore(prefs).loadCategories(),
        )
    }

    private class InMemoryPreferenceStore : PreferenceStore {
        private val values = mutableMapOf<String, String>()

        override fun getString(key: String, defaultValue: String?): String? {
            return values[key] ?: defaultValue
        }

        override fun putString(key: String, value: String?) {
            if (value == null) {
                values.remove(key)
            } else {
                values[key] = value
            }
        }

        override fun remove(key: String) {
            values.remove(key)
        }
    }
}
