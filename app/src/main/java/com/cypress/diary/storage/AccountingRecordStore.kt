package com.cypress.diary.storage

import com.cypress.diary.accounting.sortRecordsForLedger
import com.cypress.diary.model.accounting.AccountingRecord
import com.cypress.diary.model.accounting.AccountingRecordType
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.util.Base64

class AccountingRecordStore(
    private val preferences: PreferenceStore,
) {
    constructor(prefs: android.content.SharedPreferences) : this(SharedPreferencesPreferenceStore(prefs))

    fun loadRecords(): List<AccountingRecord> {
        return preferences.getString(KEY_RECORDS, "")
            .orEmpty()
            .lineSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { line -> runCatching { decode(line) }.getOrNull() }
            .toList()
            .let(::sortRecordsForLedger)
    }

    fun saveRecords(records: List<AccountingRecord>) {
        preferences.putString(KEY_RECORDS, records.joinToString("\n") { encode(it) })
    }

    fun upsert(record: AccountingRecord) {
        saveRecords((loadRecords().filterNot { it.id == record.id } + record).let(::sortRecordsForLedger))
    }

    fun delete(id: String) {
        saveRecords(loadRecords().filterNot { it.id == id })
    }

    companion object {
        private const val KEY_RECORDS = "accounting_records"

        fun encode(record: AccountingRecord): String {
            return listOf(
                safe(record.id),
                record.type.name,
                record.amountCents.toString(),
                safe(record.category),
                record.date.toString(),
                safe(record.note),
                record.createdAt.toString(),
                record.updatedAt.toString(),
            ).joinToString("|")
        }

        private fun decode(value: String): AccountingRecord {
            val parts = value.split('|')
            require(parts.size == 8) { "invalid accounting record" }
            return AccountingRecord(
                id = unsafe(parts[0]),
                type = AccountingRecordType.valueOf(parts[1]),
                amountCents = parts[2].toLong(),
                category = unsafe(parts[3]),
                date = LocalDate.parse(parts[4]),
                note = unsafe(parts[5]),
                createdAt = parts[6].toLong(),
                updatedAt = parts[7].toLong(),
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
