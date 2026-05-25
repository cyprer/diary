package com.cypress.diary.export

import com.cypress.diary.model.DiaryDocument
import com.cypress.diary.model.DiaryDocumentType
import com.cypress.diary.parser.DiaryDocumentCodec
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.time.Clock
import java.time.OffsetDateTime
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class DiaryExportArchive(
    private val clock: Clock = Clock.systemDefaultZone(),
    private val documentCodec: DiaryDocumentCodec = DiaryDocumentCodec(),
) {
    fun write(documents: List<DiaryDocument>, output: OutputStream) {
        val sortedDocuments = documents
            .distinctBy { it.path }
            .sortedBy { it.path }

        ZipOutputStream(output).use { zip ->
            zip.writeTextEntry("manifest.json", manifestFor(sortedDocuments))
            sortedDocuments.forEach { document ->
                zip.writeTextEntry(documentEntryName(document), document.markdown)
            }
        }
    }

    fun read(input: InputStream): List<DiaryDocument> {
        val documents = mutableListOf<DiaryDocument>()
        ZipInputStream(input).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val name = entry.name
                if (!entry.isDirectory && name.startsWith(DOCUMENTS_PREFIX)) {
                    val path = name.removePrefix(DOCUMENTS_PREFIX)
                    val markdown = zip.readBytes().toString(StandardCharsets.UTF_8)
                    runCatching { documentCodec.parse(path, markdown) }
                        .getOrNull()
                        ?.let(documents::add)
                }
                entry = zip.nextEntry
            }
        }
        return documents.sortedWith(compareBy({ it.year }, { it.month ?: 0 }, { it.type.ordinal }, { it.weekIndex ?: 0 }))
    }

    private fun manifestFor(documents: List<DiaryDocument>): String {
        val documentPaths = documents.joinToString(",\n") { document ->
            "    ${quoteJson(document.path)}"
        }
        return """
            {
              "formatVersion": 1,
              "exportedAt": ${quoteJson(OffsetDateTime.now(clock).toString())},
              "documentCount": ${documents.size},
              "weekCount": ${documents.count { it.type == DiaryDocumentType.Week }},
              "summaryCount": ${documents.count { it.type != DiaryDocumentType.Week }},
              "documents": [
            $documentPaths
              ]
            }
        """.trimIndent()
    }

    private fun documentEntryName(document: DiaryDocument): String {
        return "$DOCUMENTS_PREFIX${safeZipPath(document.path)}"
    }

    private fun safeZipPath(path: String): String {
        val normalized = path
            .replace('\\', '/')
            .split('/')
            .filter { segment -> segment.isNotBlank() && segment != "." && segment != ".." }
            .joinToString("/")

        return normalized.ifBlank { "document.md" }
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

    private companion object {
        const val DOCUMENTS_PREFIX = "documents/"
    }
}
