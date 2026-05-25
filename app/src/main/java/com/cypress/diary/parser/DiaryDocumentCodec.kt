package com.cypress.diary.parser

import com.cypress.diary.github.SummaryDocumentPath
import com.cypress.diary.github.extractSummaryDocumentPath
import com.cypress.diary.model.DiaryDocument
import com.cypress.diary.model.DiaryDocumentType
import java.time.LocalDate

class DiaryDocumentCodec {
    fun parse(path: String, markdown: String): DiaryDocument {
        val documentPath = requireNotNull(extractSummaryDocumentPath(path)) {
            "unsupported summary document path: $path"
        }
        val normalized = markdown.removePrefix("\uFEFF").replace("\r\n", "\n").replace('\r', '\n')
        val lines = normalized.lines()
        val frontMatter = parseFrontMatter(lines)
        val body = extractBody(lines, frontMatter.endIndex, frontMatter.title)

        return DiaryDocument(
            path = path,
            type = when (documentPath) {
                is SummaryDocumentPath.Year -> DiaryDocumentType.Year
                is SummaryDocumentPath.Month -> DiaryDocumentType.Month
                is SummaryDocumentPath.Week -> DiaryDocumentType.Week
            },
            year = documentPath.year,
            month = documentPath.month,
            weekIndex = documentPath.weekIndex,
            title = frontMatter.title,
            published = frontMatter.published,
            markdown = markdown,
            body = body,
        )
    }

    private fun parseFrontMatter(lines: List<String>): FrontMatter {
        val start = lines.indexOfFirst { it.isNotBlank() }
        require(start >= 0 && lines[start].trim() == "---") { "front matter is missing" }
        val end = ((start + 1) until lines.size).firstOrNull { lines[it].trim() == "---" } ?: -1
        require(end > start) { "front matter is missing" }

        var title: String? = null
        var published: LocalDate? = null
        for (index in start + 1 until end) {
            val line = lines[index]
            if (line.isBlank()) continue
            val parts = line.split(":", limit = 2)
            if (parts.size != 2) continue
            when (parts[0].trim()) {
                "title" -> title = unquote(parts[1].trim())
                "published" -> published = LocalDate.parse(unquote(parts[1].trim()))
            }
        }

        return FrontMatter(
            title = requireNotNull(title) { "title is missing" },
            published = requireNotNull(published) { "published is missing" },
            endIndex = end,
        )
    }

    private fun extractBody(lines: List<String>, frontMatterEndIndex: Int, title: String): String {
        val bodyLines = lines.drop(frontMatterEndIndex + 1).dropWhile { it.isBlank() }.toMutableList()
        if (bodyLines.isNotEmpty()) {
            val heading = bodyLines.first().trim()
            if (heading == "# $title") {
                bodyLines.removeAt(0)
            }
        }
        return trimBlankLines(bodyLines).joinToString("\n")
    }

    private fun trimBlankLines(lines: List<String>): List<String> {
        var start = 0
        var end = lines.size - 1
        while (start <= end && lines[start].isBlank()) start++
        while (end >= start && lines[end].isBlank()) end--
        return if (start > end) emptyList() else lines.subList(start, end + 1)
    }

    private fun unquote(value: String): String {
        val trimmed = value.trim()
        if (trimmed.length >= 2 && trimmed.first() == '"' && trimmed.last() == '"') {
            return trimmed.substring(1, trimmed.length - 1)
                .replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\\", "\\")
        }
        return trimmed
    }

    private data class FrontMatter(
        val title: String,
        val published: LocalDate,
        val endIndex: Int,
    )
}
