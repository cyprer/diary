package com.cypress.diary.export

import com.cypress.diary.model.accounting.AccountingRecord
import com.cypress.diary.model.accounting.AccountingRecordType
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.time.Clock
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class AccountingExportArchive(
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    fun write(records: List<AccountingRecord>, output: OutputStream) {
        ZipOutputStream(output).use { zip ->
            zip.writeTextEntry("manifest.json", manifestFor(records))
            zip.writeTextEntry("records.json", recordsJson(records))
        }
    }

    fun read(input: InputStream): List<AccountingRecord> {
        ZipInputStream(input).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name == "records.json") {
                    val text = zip.readBytes().toString(StandardCharsets.UTF_8)
                    return parseRecords(text)
                }
                entry = zip.nextEntry
            }
        }
        return emptyList()
    }

    private fun manifestFor(records: List<AccountingRecord>): String {
        return """
            {
              "formatVersion": 1,
              "exportedAt": ${quoteJson(OffsetDateTime.now(clock).toString())},
              "recordCount": ${records.size}
            }
        """.trimIndent()
    }

    private fun recordsJson(records: List<AccountingRecord>): String {
        return records.joinToString(
            separator = ",\n",
            prefix = "[\n",
            postfix = "\n]",
        ) { record ->
            """
              {
                "id": ${quoteJson(record.id)},
                "type": ${quoteJson(record.type.name)},
                "amountCents": ${record.amountCents},
                "category": ${quoteJson(record.category)},
                "date": ${quoteJson(record.date.toString())},
                "note": ${quoteJson(record.note)},
                "createdAt": ${record.createdAt},
                "updatedAt": ${record.updatedAt}
              }
            """.trimIndent()
        }
    }

    private fun parseRecords(text: String): List<AccountingRecord> {
        return jsonObjects(text).mapNotNull { objectText ->
            runCatching {
                AccountingRecord(
                    id = requireString(objectText, "id"),
                    type = AccountingRecordType.valueOf(requireString(objectText, "type")),
                    amountCents = requireLong(objectText, "amountCents"),
                    category = requireString(objectText, "category"),
                    date = LocalDate.parse(requireString(objectText, "date")),
                    note = requireString(objectText, "note"),
                    createdAt = requireLong(objectText, "createdAt"),
                    updatedAt = requireLong(objectText, "updatedAt"),
                )
            }.getOrNull()
        }
    }

    private fun jsonObjects(text: String): List<String> {
        val objects = mutableListOf<String>()
        var depth = 0
        var start = -1
        var inString = false
        var escaped = false
        text.forEachIndexed { index, ch ->
            if (escaped) {
                escaped = false
                return@forEachIndexed
            }
            if (ch == '\\' && inString) {
                escaped = true
                return@forEachIndexed
            }
            if (ch == '"') {
                inString = !inString
                return@forEachIndexed
            }
            if (inString) return@forEachIndexed
            when (ch) {
                '{' -> {
                    if (depth == 0) start = index
                    depth += 1
                }
                '}' -> {
                    depth -= 1
                    if (depth == 0 && start >= 0) {
                        objects += text.substring(start, index + 1)
                        start = -1
                    }
                }
            }
        }
        return objects
    }

    private fun requireString(objectText: String, key: String): String {
        val match = Regex(""""${Regex.escape(key)}"\s*:\s*"((?:\\.|[^"\\])*)"""")
            .find(objectText)
            ?: error("missing $key")
        return unquoteJson(match.groupValues[1])
    }

    private fun requireLong(objectText: String, key: String): Long {
        val match = Regex(""""${Regex.escape(key)}"\s*:\s*(-?\d+)""")
            .find(objectText)
            ?: error("missing $key")
        return match.groupValues[1].toLong()
    }

    private fun ZipOutputStream.writeTextEntry(name: String, text: String) {
        putNextEntry(ZipEntry(name))
        write(text.toByteArray(StandardCharsets.UTF_8))
        closeEntry()
    }

    private fun quoteJson(value: String): String = buildString {
        append('"')
        value.forEach { ch ->
            when (ch) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(ch)
            }
        }
        append('"')
    }

    private fun unquoteJson(value: String): String = buildString {
        var index = 0
        while (index < value.length) {
            val ch = value[index]
            if (ch == '\\' && index + 1 < value.length) {
                when (val escaped = value[index + 1]) {
                    'n' -> append('\n')
                    'r' -> append('\r')
                    't' -> append('\t')
                    '\\' -> append('\\')
                    '"' -> append('"')
                    else -> append(escaped)
                }
                index += 2
            } else {
                append(ch)
                index += 1
            }
        }
    }
}
