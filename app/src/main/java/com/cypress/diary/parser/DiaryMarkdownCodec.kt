package com.cypress.diary.parser

import com.cypress.diary.model.DiaryDay
import com.cypress.diary.model.DiaryWeek
import com.cypress.diary.model.WeekKey
import java.time.LocalDate

class DiaryMarkdownCodec {
    fun parse(markdown: String): DiaryWeek {
        val lines = normalize(markdown).lines()
        require(lines.isNotEmpty()) { "markdown is empty" }

        val frontMatterEnd = findClosingFence(lines, 0)
        require(frontMatterEnd > 0) { "front matter is missing" }

        val frontMatter = parseFrontMatter(lines.subList(1, frontMatterEnd))
        var index = frontMatterEnd + 1
        while (index < lines.size && lines[index].isBlank()) {
            index++
        }

        require(index < lines.size && lines[index].trimStart().startsWith("#")) { "title heading is missing" }
        val title = lines[index].removePrefix("#").trim()
        index++
        while (index < lines.size && lines[index].isBlank()) {
            index++
        }

        val weekKey = WeekKey.from(frontMatter.published)

        val days = mutableListOf<DiaryDay>()
        var currentDayMonth: Int? = null
        var currentDayOfMonth: Int? = null
        val currentContent = mutableListOf<String>()

        fun finishDay() {
            val month = currentDayMonth ?: return
            val dayOfMonth = currentDayOfMonth ?: return
            val content = trimBlankLines(currentContent)
            days += DiaryDay(
                date = LocalDate.of(weekKey.year, month, dayOfMonth),
                content = content,
            )
            currentDayMonth = null
            currentDayOfMonth = null
            currentContent.clear()
        }

        while (index < lines.size) {
            val line = lines[index]
            val heading = dayHeadingRegex.matchEntire(line)
            if (heading != null) {
                finishDay()
                currentDayMonth = heading.groupValues[1].toInt()
                currentDayOfMonth = heading.groupValues[2].toInt()
            } else {
                currentContent += line
            }
            index++
        }
        finishDay()

        return DiaryWeek(
            key = weekKey,
            title = title,
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
        appendLine("description: ${quote(week.description)}")
        appendLine("tags: ${week.tags.joinToString(prefix = "[", postfix = "]") { quote(it) }}")
        appendLine("category: ${quote(week.category)}")
        appendLine("draft: ${week.draft}")
        appendLine("---")
        appendLine()
        appendLine("# ${week.title}")

        if (week.days.isNotEmpty()) {
            appendLine()
            append(week.days.joinToString("\n\n") { renderDay(it) })
        }
    }

    private fun renderDay(day: DiaryDay): String = buildString {
        append("## ${day.date.monthValue}.${day.date.dayOfMonth}")
        if (day.content.isNotBlank()) {
            append("\n\n")
            append(day.content.trimEnd())
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
            description = requireNotNull(description) { "description is missing" },
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

    private fun findClosingFence(lines: List<String>, startIndex: Int): Int {
        for (index in startIndex + 1 until lines.size) {
            if (lines[index].trim() == "---") return index
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
        val dayHeadingRegex = Regex("^##\\s*(\\d{1,2})\\.(\\d{1,2})\\s*$")
    }
}
