package com.cypress.diary.storage

import com.cypress.diary.model.accounting.AccountingRecord
import com.cypress.diary.model.accounting.AccountingRecordType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class AccountingRecordStoreTest {
    @Test
    fun savesAndLoadsRecords() {
        val store = AccountingRecordStore(InMemoryPreferenceStore())
        val records = listOf(record("a", createdAt = 1), record("b", amountCents = 2500, createdAt = 2))

        store.saveRecords(records)

        assertEquals(listOf(records[1], records[0]), store.loadRecords())
    }

    @Test
    fun upsertReplacesExistingRecordById() {
        val store = AccountingRecordStore(InMemoryPreferenceStore())
        store.saveRecords(listOf(record("a", amountCents = 100)))

        store.upsert(record("a", amountCents = 200))

        assertEquals(listOf(record("a", amountCents = 200)), store.loadRecords())
    }

    @Test
    fun deleteRemovesRecordById() {
        val store = AccountingRecordStore(InMemoryPreferenceStore())
        store.saveRecords(listOf(record("a"), record("b")))

        store.delete("a")

        assertEquals(listOf(record("b")), store.loadRecords())
    }

    @Test
    fun malformedRecordsAreSkipped() {
        val prefs = InMemoryPreferenceStore()
        prefs.putString("accounting_records", "bad-line\n${AccountingRecordStore.encode(record("ok"))}")

        assertEquals(listOf(record("ok")), AccountingRecordStore(prefs).loadRecords())
    }

    private fun record(
        id: String,
        amountCents: Long = 1200,
        createdAt: Long = 10,
    ): AccountingRecord {
        return AccountingRecord(
            id = id,
            type = AccountingRecordType.Expense,
            amountCents = amountCents,
            category = "餐饮",
            date = LocalDate.of(2026, 5, 25),
            note = "午餐",
            createdAt = createdAt,
            updatedAt = 20,
        )
    }

    private class InMemoryPreferenceStore : PreferenceStore {
        private val values = mutableMapOf<String, String?>()

        override fun getString(key: String, defaultValue: String?): String? {
            return values.getOrDefault(key, defaultValue)
        }

        override fun putString(key: String, value: String?) {
            values[key] = value
        }

        override fun remove(key: String) {
            values.remove(key)
        }
    }
}
