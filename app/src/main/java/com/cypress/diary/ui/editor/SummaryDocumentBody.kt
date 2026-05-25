package com.cypress.diary.ui.editor

import com.cypress.diary.model.DiaryDocument

fun DiaryDocument.withSummaryBody(body: String): DiaryDocument {
    val markdown = replaceSummaryBody(markdown = markdown, title = title, body = body)
    return copy(markdown = markdown, body = body)
}

private fun replaceSummaryBody(markdown: String, title: String, body: String): String {
    val normalized = markdown
        .removePrefix("\uFEFF")
        .replace("\r\n", "\n")
        .replace('\r', '\n')
    val lines = normalized.lines()
    val frontMatterEnd = findFrontMatterEnd(lines)
    val titleIndex = findTitleIndex(lines, title, frontMatterEnd + 1)
    val prefix = if (titleIndex >= 0) {
        lines.take(titleIndex + 1).joinToString("\n").trimEnd()
    } else {
        val base = lines.take(frontMatterEnd + 1).joinToString("\n").trimEnd()
        if (base.isBlank()) "# $title" else "$base\n\n# $title"
    }
    val suffix = if (titleIndex >= 0) {
        daySectionsAfterTitle(lines, titleIndex)
    } else {
        ""
    }

    val trimmedBody = body.trim()
    val summaryMarkdown = if (trimmedBody.isBlank()) {
        prefix
    } else {
        "$prefix\n\n$trimmedBody"
    }
    return if (suffix.isBlank()) summaryMarkdown else "$summaryMarkdown\n\n$suffix"
}

private fun findFrontMatterEnd(lines: List<String>): Int {
    val start = lines.indexOfFirst { it.isNotBlank() }
    if (start < 0 || lines[start].trim() != "---") return -1
    return ((start + 1) until lines.size).firstOrNull { lines[it].trim() == "---" } ?: -1
}

private fun findTitleIndex(lines: List<String>, title: String, startIndex: Int): Int {
    val expectedTitle = "# $title"
    val start = startIndex.coerceAtLeast(0)
    return (start until lines.size).firstOrNull { index ->
        val line = lines[index].trim()
        line == expectedTitle || line.startsWith("# ")
    } ?: -1
}

private fun daySectionsAfterTitle(lines: List<String>, titleIndex: Int): String {
    val sectionIndex = ((titleIndex + 1) until lines.size).firstOrNull { index ->
        lines[index].trimStart().startsWith("## ")
    } ?: return ""
    return lines.drop(sectionIndex).joinToString("\n").trim()
}
