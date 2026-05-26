package com.cypress.diary.export

import com.cypress.diary.model.accounting.AccountingRecord
import com.cypress.diary.model.accounting.AccountingCategory
import com.cypress.diary.model.accounting.AccountingRecordType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class AccountingExportArchiveTest {
    @Test
    fun writesManifestAndRecords() {
        val archive = AccountingExportArchive(
            clock = Clock.fixed(Instant.parse("2026-05-26T10:15:30Z"), ZoneOffset.UTC),
        )
        val output = ByteArrayOutputStream()

        archive.write(
            records = listOf(record("expense-1", AccountingRecordType.Expense)),
            output = output,
        )

        val entries = unzip(output.toByteArray())
        assertTrue(entries.containsKey("manifest.json"))
        assertTrue(entries.containsKey("records.json"))
        assertTrue(entries.getValue("manifest.json").contains("\"formatVersion\": 1"))
        assertTrue(entries.getValue("manifest.json").contains("\"recordCount\": 1"))
        assertTrue(entries.getValue("records.json").contains("\"id\": \"expense-1\""))
    }

    @Test
    fun readsExportedAccountingRecords() {
        val archive = AccountingExportArchive()
        val output = ByteArrayOutputStream()
        val records = listOf(
            record("income-1", AccountingRecordType.Income, amountCents = 12345, note = "bonus"),
            record("expense-1", AccountingRecordType.Expense, amountCents = 678, note = "lunch"),
        )

        archive.write(records, output)
        val parsed = archive.read(ByteArrayInputStream(output.toByteArray()))

        assertEquals(listOf("income-1", "expense-1"), parsed.map { it.id })
        assertEquals(AccountingRecordType.Income, parsed[0].type)
        assertEquals(12345L, parsed[0].amountCents)
        assertEquals("bonus", parsed[0].note)
    }

    @Test
    fun writesAndReadsCustomCategories() {
        val archive = AccountingExportArchive()
        val output = ByteArrayOutputStream()
        val categories = listOf(
            AccountingCategory("custom-coffee", "咖啡", AccountingRecordType.Expense),
            AccountingCategory("custom-part-time", "兼职", AccountingRecordType.Income),
        )

        archive.write(
            data = AccountingArchiveData(
                records = listOf(record("expense-1", AccountingRecordType.Expense)),
                customCategories = categories,
            ),
            output = output,
        )

        val entries = unzip(output.toByteArray())
        assertTrue(entries.containsKey("categories.json"))
        assertTrue(entries.getValue("categories.json").contains("\"label\": \"咖啡\""))
        val parsed = archive.readData(ByteArrayInputStream(output.toByteArray()))
        assertEquals(categories, parsed.customCategories)
        assertEquals(listOf("expense-1"), parsed.records.map { it.id })
    }

    @Test
    fun readsOldArchivesWithoutCustomCategories() {
        val output = ByteArrayOutputStream()
        AccountingExportArchive().write(
            records = listOf(record("old", AccountingRecordType.Expense)),
            output = output,
        )

        val parsed = AccountingExportArchive().readData(ByteArrayInputStream(output.toByteArray()))

        assertEquals(listOf("old"), parsed.records.map { it.id })
        assertEquals(emptyList<AccountingCategory>(), parsed.customCategories)
    }

    @Test
    fun skipsDamagedRecordsWhenReading() {
        val input = ByteArrayOutputStream()
        ZipOutputStream(input).use { zip ->
            zip.putNextEntry(ZipEntry("records.json"))
            zip.write(
                """
                    [
                      {"id":"good","type":"Income","amountCents":100,"category":"salary","date":"2026-05-01","note":"","createdAt":1,"updatedAt":2},
                      {"id":"bad","type":"Expense","amountCents":"oops","category":"food","date":"2026-05-02","note":"","createdAt":1,"updatedAt":2}
                    ]
                """.trimIndent().toByteArray(StandardCharsets.UTF_8),
            )
            zip.closeEntry()
        }

        val parsed = AccountingExportArchive().read(ByteArrayInputStream(input.toByteArray()))

        assertEquals(listOf("good"), parsed.map { it.id })
    }

    private fun record(
        id: String,
        type: AccountingRecordType,
        amountCents: Long = 100,
        note: String = "",
    ): AccountingRecord {
        return AccountingRecord(
            id = id,
            type = type,
            amountCents = amountCents,
            category = "category",
            date = LocalDate.of(2026, 5, 26),
            note = note,
            createdAt = 1,
            updatedAt = 2,
        )
    }

    private fun unzip(bytes: ByteArray): Map<String, String> {
        val entries = mutableMapOf<String, String>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    entries[entry.name] = zip.readBytes().toString(StandardCharsets.UTF_8)
                }
                entry = zip.nextEntry
            }
        }
        return entries
    }
}
