package com.cypress.diary.parser

import com.cypress.diary.model.DiaryDay
import com.cypress.diary.model.DiaryWeek
import com.cypress.diary.model.WeekKey
import java.time.LocalDate

class DiaryMarkdownCodec {
    fun parse(markdown: String): DiaryWeek {
        val lines = normalize(markdown).lines()
        require(lines.isNotEmpty()) { "markdown is empty" }

        val openingFenceIndex = firstNonBlankIndex(lines)
        require(openingFenceIndex >= 0 && lines[openingFenceIndex].trim() == "---") { "front matter is missing" }
        val frontMatterEnd = findClosingFence(lines, openingFenceIndex)
        require(frontMatterEnd > openingFenceIndex) { "front matter is missing" }

        val frontMatter = parseFrontMatter(lines.subList(openingFenceIndex + 1, frontMatterEnd))
        var index = frontMatterEnd + 1
        while (index < lines.size && lines[index].isBlank()) {
            index++
        }

        require(index < lines.size) { "title heading is missing" }
        val titleMatch = titleHeadingRegex.matchEntire(lines[index])
        require(titleMatch != null) { "title heading is missing" }
        val title = titleMatch.groupValues[1].trim()
        index++
        while (index < lines.size && lines[index].isBlank()) {
            index++
        }

        val weekKey = WeekKey.from(frontMatter.published)

        val days = mutableListOf<DiaryDay>()
        val introLines = mutableListOf<String>()
        var intro = ""
        var introCaptured = false
        var currentDayMonth: Int? = null
        var currentDayOfMonth: Int? = null
        val currentContent = mutableListOf<String>()

        fun finishDay() {
            val month = currentDayMonth ?: return
            val dayOfMonth = currentDayOfMonth ?: return
            val content = trimDayContent(currentContent)
            days += DiaryDay(
                date = LocalDate.of(weekKey.year, month, dayOfMonth),
                content = content,
            )
            currentDayMonth = null
            currentDayOfMonth = null
            currentContent.clear()
        }

        fun captureIntroIfNeeded() {
            if (!introCaptured) {
                intro = trimBlankLines(introLines)
                introLines.clear()
                introCaptured = true
            }
        }

        while (index < lines.size) {
            val line = lines[index]
            val heading = dayHeadingRegex.matchEntire(line)
            if (heading != null) {
                captureIntroIfNeeded()
                finishDay()
                currentDayMonth = heading.groupValues[1].toInt()
                currentDayOfMonth = heading.groupValues[2].toInt()
            } else {
                if (introCaptured) {
                    currentContent += line
                } else {
                    introLines += line
                }
            }
            index++
        }
        captureIntroIfNeeded()
        finishDay()

        return DiaryWeek(
            key = weekKey,
            title = title,
            intro = intro,
            published = frontMatter.published,
            description = frontMatter.description,
            tags = frontMatter.tags,
            category = frontMatter.category,
            draft = frontMatter.draft,
            days = days,
        )
    }

    fun render(week: DiaryWeek): String = buildString {
        appendLine("---")
        appendLine("title: ${quote(week.title)}")
        appendLine("published: ${week.published}")
        appendLine("tags: ${week.tags.joinToString(prefix = "[", postfix = "]") { quote(it) }}")
        appendLine("category: ${quote(week.category)}")
        appendLine("draft: ${week.draft}")
        appendLine("---")
        appendLine()
        appendLine("# ${week.title}")

        if (week.intro.isNotBlank()) {
            appendLine()
            append(week.intro.trimEnd())
        }

        if (week.days.isNotEmpty()) {
            appendLine()
            if (week.intro.isNotBlank()) {
                appendLine()
            }
            append(renderDays(week.days))
        }
    }

    private fun renderDays(days: List<DiaryDay>): String = buildString {
        days.forEachIndexed { index, day ->
            append(renderDay(day))
            if (index != days.lastIndex) {
                append(if (day.content.isBlank()) "\n\n" else "\n")
            }
        }
    }

    private fun renderDay(day: DiaryDay): String = buildString {
        append("## ${day.date.monthValue}.${day.date.dayOfMonth}")
        if (day.content.isNotBlank()) {
            append("\n")
            append(renderDayContent(day.content))
        }
    }

    private fun parseFrontMatter(lines: List<String>): FrontMatter {
        var title: String? = null
        var published: LocalDate? = null
        var description: String? = null
        var tags: List<String>? = null
        var category: String? = null
        var draft: Boolean? = null

        for (line in lines) {
            if (line.isBlank()) continue
            val parts = line.split(":", limit = 2)
            require(parts.size == 2) { "invalid front matter line: $line" }

            val key = parts[0].trim()
            val value = parts[1].trim()
            when (key) {
                "title" -> title = unquote(value)
                "published" -> published = LocalDate.parse(unquote(value))
                "description" -> description = unquote(value)
                "tags" -> tags = parseStringList(value)
                "category" -> category = unquote(value)
                "draft" -> draft = value.toBooleanStrict()
            }
        }

        return FrontMatter(
            title = requireNotNull(title) { "title is missing" },
            published = requireNotNull(published) { "published is missing" },
            description = description ?: requireNotNull(title) { "title is missing" },
            tags = requireNotNull(tags) { "tags is missing" },
            category = requireNotNull(category) { "category is missing" },
            draft = requireNotNull(draft) { "draft is missing" },
        )
    }

    private fun parseStringList(raw: String): List<String> {
        val trimmed = raw.trim()
        require(trimmed.startsWith("[") && trimmed.endsWith("]")) { "invalid list: $raw" }
        val inner = trimmed.substring(1, trimmed.length - 1).trim()
        if (inner.isEmpty()) return emptyList()

        val values = mutableListOf<String>()
        var index = 0
        while (index < inner.length) {
            while (index < inner.length && inner[index].isWhitespace()) {
                index++
            }
            require(index < inner.length && inner[index] == '"') { "invalid list item: $raw" }
            index++

            val value = StringBuilder()
            while (index < inner.length) {
                val ch = inner[index]
                when (ch) {
                    '\\' -> {
                        require(index + 1 < inner.length) { "invalid escape sequence: $raw" }
                        value.append(
                            when (val escaped = inner[index + 1]) {
                                '\\' -> '\\'
                                '"' -> '"'
                                'n' -> '\n'
                                'r' -> '\r'
                                't' -> '\t'
                                else -> escaped
                            }
                        )
                        index += 2
                    }
                    '"' -> {
                        index++
                        break
                    }
                    else -> {
                        value.append(ch)
                        index++
                    }
                }
            }

            values += value.toString()

            while (index < inner.length && inner[index].isWhitespace()) {
                index++
            }
            if (index < inner.length) {
                require(inner[index] == ',') { "invalid list separator: $raw" }
                index++
            }
        }

        return values
    }

    private fun trimBlankLines(lines: List<String>): String {
        var start = 0
        var end = lines.size - 1
        while (start <= end && lines[start].isBlank()) {
            start++
        }
        while (end >= start && lines[end].isBlank()) {
            end--
        }
        if (start > end) return ""
        return lines.subList(start, end + 1).joinToString("\n")
    }

    private fun trimDayContent(lines: List<String>): String {
        var start = 0
        var end = lines.size - 1
        while (start <= end && lines[start].isBlank()) {
            start++
        }
        while (end >= start && lines[end].isBlank()) {
            end--
        }
        if (start > end) return ""
        return lines.subList(start, end + 1)
            .joinToString("\n") { it.trimStart() }
    }

    private fun renderDayContent(content: String): String {
        return content.trim()
            .lines()
            .joinToString("\n") { line ->
                if (line.isBlank()) "" else "  ${line.trimStart()}"
            }
    }

    private fun findClosingFence(lines: List<String>, startIndex: Int): Int {
        for (index in startIndex + 1 until lines.size) {
            if (lines[index].trim() == "---") return index
        }
        return -1
    }

    private fun firstNonBlankIndex(lines: List<String>): Int {
        for (index in lines.indices) {
            if (lines[index].isNotBlank()) return index
        }
        return -1
    }

    private fun normalize(markdown: String): String = markdown
        .removePrefix("\uFEFF")
        .replace("\r\n", "\n")
        .replace('\r', '\n')

    private fun quote(value: String): String = buildString {
        append('"')
        for (ch in value) {
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

    private fun unquote(value: String): String {
        val trimmed = value.trim()
        if (trimmed.length >= 2 && trimmed.first() == '"' && trimmed.last() == '"') {
            val inner = trimmed.substring(1, trimmed.length - 1)
            val result = StringBuilder()
            var index = 0
            while (index < inner.length) {
                val ch = inner[index]
                if (ch == '\\' && index + 1 < inner.length) {
                    result.append(
                        when (val escaped = inner[index + 1]) {
                            '\\' -> '\\'
                            '"' -> '"'
                            'n' -> '\n'
                            'r' -> '\r'
                            't' -> '\t'
                            else -> escaped
                        }
                    )
                    index += 2
                } else {
                    result.append(ch)
                    index++
                }
            }
            return result.toString()
        }
        return trimmed
    }

    private data class FrontMatter(
        val title: String,
        val published: LocalDate,
        val description: String,
        val tags: List<String>,
        val category: String,
        val draft: Boolean,
    )

    private companion object {
        val titleHeadingRegex = Regex("^#\\s+(.+?)\\s*$")
        val dayHeadingRegex = Regex("^##\\s*(\\d{1,2})\\.(\\d{1,2})\\s*$")
    }
}
