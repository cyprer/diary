package com.cypress.diary.export

import com.cypress.diary.model.DiaryDocument
import com.cypress.diary.model.DiaryDocumentType
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.zip.ZipInputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiaryExportArchiveTest {
    @Test
    fun writesManifestAndMarkdownDocuments() {
        val output = ByteArrayOutputStream()
        val archive = DiaryExportArchive(
            clock = Clock.fixed(Instant.parse("2026-05-25T10:15:30Z"), ZoneOffset.UTC),
        )

        archive.write(
            listOf(
                document(
                    path = "src/content/posts/summary/26year/5month/1week.md",
                    type = DiaryDocumentType.Week,
                    markdown = "# week",
                ),
                document(
                    path = "src/content/posts/summary/26year/5month/index.md",
                    type = DiaryDocumentType.Month,
                    markdown = "# month",
                ),
            ),
            output,
        )

        val entries = readZipEntries(output.toByteArray())
        val manifest = requireNotNull(entries["manifest.json"])

        assertTrue(manifest.contains("\"formatVersion\": 1"))
        assertTrue(manifest.contains("\"exportedAt\": \"2026-05-25T10:15:30Z\""))
        assertTrue(manifest.contains("\"documentCount\": 2"))
        assertTrue(manifest.contains("\"weekCount\": 1"))
        assertTrue(manifest.contains("\"summaryCount\": 1"))
        assertTrue(manifest.contains("\"src/content/posts/summary/26year/5month/1week.md\""))
        assertTrue(manifest.contains("\"src/content/posts/summary/26year/5month/index.md\""))
        assertEquals(
            "# week",
            entries["documents/src/content/posts/summary/26year/5month/1week.md"],
        )
        assertEquals(
            "# month",
            entries["documents/src/content/posts/summary/26year/5month/index.md"],
        )
    }

    @Test
    fun keepsDocumentEntriesInsideDocumentsFolder() {
        val output = ByteArrayOutputStream()

        DiaryExportArchive().write(
            listOf(
                document(
                    path = "../outside.md",
                    type = DiaryDocumentType.Week,
                    markdown = "# safe",
                ),
            ),
            output,
        )

        val entries = readZipEntries(output.toByteArray())

        assertTrue(entries.containsKey("documents/outside.md"))
        assertEquals("# safe", entries["documents/outside.md"])
    }

    @Test
    fun readsExportedDiaryDocuments() {
        val bytes = ByteArrayOutputStream()
        val archive = DiaryExportArchive()
        val markdown = """
            ---
            title: "week"
            published: 2026-05-25
            ---

            # week

            ## 5.25

            imported
        """.trimIndent()

        archive.write(
            listOf(
                document(
                    path = "src/content/posts/summary/26year/5month/4week.md",
                    type = DiaryDocumentType.Week,
                    markdown = markdown,
                ),
            ),
            bytes,
        )

        val documents = archive.read(ByteArrayInputStream(bytes.toByteArray()))

        assertEquals(1, documents.size)
        assertEquals("src/content/posts/summary/26year/5month/4week.md", documents.first().path)
        assertEquals(DiaryDocumentType.Week, documents.first().type)
        assertEquals(markdown, documents.first().markdown)
    }

    private fun document(
        path: String,
        type: DiaryDocumentType,
        markdown: String,
    ): DiaryDocument {
        return DiaryDocument(
            path = path,
            type = type,
            year = 2026,
            month = 5,
            weekIndex = if (type == DiaryDocumentType.Week) 1 else null,
            title = path,
            published = LocalDate.of(2026, 5, 25),
            markdown = markdown,
            body = markdown,
        )
    }

    private fun readZipEntries(bytes: ByteArray): Map<String, String> {
        val entries = linkedMapOf<String, String>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                entries[entry.name] = zip.readBytes().toString(Charsets.UTF_8)
                entry = zip.nextEntry
            }
        }
        return entries
    }
}
