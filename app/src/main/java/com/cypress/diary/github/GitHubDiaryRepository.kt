package com.cypress.diary.github

import com.cypress.diary.model.DiaryWeek
import com.cypress.diary.model.WeekKey
import com.cypress.diary.model.DiaryDocument
import com.cypress.diary.parser.DiaryDocumentCodec
import com.cypress.diary.parser.DiaryMarkdownCodec
import com.cypress.diary.parser.WeekPathResolver
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDate
import java.nio.charset.StandardCharsets
import java.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class GitHubDiaryRepository(
    private val apiBaseUrl: String = "https://api.github.com",
    private val codec: DiaryMarkdownCodec = DiaryMarkdownCodec(),
    private val documentCodec: DiaryDocumentCodec = DiaryDocumentCodec(),
) {
    suspend fun testConnection(config: GitHubConfig): GitHubRepoInfo = withContext(Dispatchers.IO) {
        val normalized = config.normalized()
        val repo = requestJson(
            method = "GET",
            url = urlFor("repos/${encodeSegment(normalized.owner)}/${encodeSegment(normalized.repo)}"),
            config = normalized,
        )
        GitHubRepoInfo(
            fullName = repo.optString("full_name", "${normalized.owner}/${normalized.repo}"),
            defaultBranch = repo.optString("default_branch", normalized.branch),
        )
    }

    suspend fun loadWeeks(config: GitHubConfig): List<DiaryWeek> = withContext(Dispatchers.IO) {
        val normalized = config.normalized()
        if (normalized.token.isBlank()) {
            return@withContext loadPublicWeeks(normalized)
        }

        val tree = requestJson(
            method = "GET",
            url = urlFor("repos/${encodeSegment(normalized.owner)}/${encodeSegment(normalized.repo)}/git/trees/${encodeSegment(normalized.branch)}?recursive=1"),
            config = normalized,
        )

        val treeArray = tree.getJSONArray("tree")
        val paths = buildList {
            for (index in 0 until treeArray.length()) {
                val item = treeArray.getJSONObject(index)
                if (item.optString("type") == "blob") {
                    add(item.optString("path"))
                }
            }
        }

        val weekPaths = sortSummaryWeekPaths(paths)
        weekPaths.map { path ->
            val contentJson = requestJson(
                method = "GET",
                url = urlFor(
                    "repos/${encodeSegment(normalized.owner)}/${encodeSegment(normalized.repo)}/contents/${encodePath(path)}?ref=${encodeSegment(normalized.branch)}",
                ),
                config = normalized,
            )
            val encodedContent = contentJson.getString("content").replace("\n", "")
            val markdown = String(Base64.getMimeDecoder().decode(encodedContent), StandardCharsets.UTF_8)
            codec.parse(markdown)
        }
    }

    suspend fun loadDocuments(config: GitHubConfig): List<DiaryDocument> = withContext(Dispatchers.IO) {
        val normalized = config.normalized()
        if (normalized.token.isBlank()) {
            return@withContext loadPublicDocuments(normalized)
        }

        val tree = requestJson(
            method = "GET",
            url = urlFor("repos/${encodeSegment(normalized.owner)}/${encodeSegment(normalized.repo)}/git/trees/${encodeSegment(normalized.branch)}?recursive=1"),
            config = normalized,
        )

        val treeArray = tree.getJSONArray("tree")
        val paths = buildList {
            for (index in 0 until treeArray.length()) {
                val item = treeArray.getJSONObject(index)
                if (item.optString("type") == "blob") {
                    add(item.optString("path"))
                }
            }
        }

        sortSummaryDocumentPaths(paths).map { path ->
            documentCodec.parse(path, requestMarkdownFromContents(normalized, path))
        }
    }

    suspend fun saveWeek(config: GitHubConfig, path: String, markdown: String): Unit = withContext(Dispatchers.IO) {
        val normalized = config.normalized()
        if (normalized.token.isBlank()) {
            throw IOException("GitHub push requires a token")
        }

        val sha = existingSha(normalized, path)
        val payload = GitHubContentsWritePayload.fromMarkdown(
            markdown = markdown,
            branch = normalized.branch,
            message = "update diary ${path.substringAfterLast('/')}",
            sha = sha,
        ).toJson()

        requestJson(
            method = "PUT",
            url = urlFor(
                "repos/${encodeSegment(normalized.owner)}/${encodeSegment(normalized.repo)}/contents/${encodePath(path)}",
            ),
            config = normalized,
            body = payload,
        )
    }

    private suspend fun loadPublicWeeks(config: GitHubConfig): List<DiaryWeek> = coroutineScope {
        val paths = candidatePublicWeekPaths()
        val dispatcher = Dispatchers.IO
        paths.map { path ->
            async(dispatcher) {
                requestPublicMarkdown(path, config)?.let { markdown ->
                    runCatching { codec.parse(markdown) }.getOrNull()
                }
            }
        }.awaitAll().filterNotNull().sortedWith(
            compareBy({ it.key.year }, { it.key.month }, { it.key.weekIndex }),
        )
    }

    private suspend fun loadPublicDocuments(config: GitHubConfig): List<DiaryDocument> = coroutineScope {
        candidateSummaryDocumentPaths().map { path ->
            async(Dispatchers.IO) {
                requestPublicMarkdown(path, config)?.let { markdown ->
                    runCatching { documentCodec.parse(path, markdown) }.getOrNull()
                }
            }
        }.awaitAll().filterNotNull().sortedWith(
            compareBy({ it.year }, { it.month ?: 0 }, { documentTypeOrder(it) }, { it.weekIndex ?: 0 }),
        )
    }

    private fun candidatePublicWeekPaths(): List<String> {
        val resolver = WeekPathResolver()
        val currentYear = LocalDate.now().year
        val currentMonth = LocalDate.now().monthValue
        val paths = mutableListOf<String>()
        for (year in 2025..currentYear) {
            val maxMonth = if (year == currentYear) currentMonth else 12
            for (month in 1..maxMonth) {
                for (weekIndex in 1..4) {
                    paths += resolver.resolve(WeekKey(year, month, weekIndex))
                }
            }
        }
        return paths
    }

    private fun requestPublicMarkdown(path: String, config: GitHubConfig): String? {
        val connection = (URL(rawUrlFor(config, path)).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("User-Agent", "com.cypress.diary")
        }
        return try {
            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                null
            } else {
                readBody(connection.inputStream)
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun existingSha(config: GitHubConfig, path: String): String? {
        return try {
            requestJson(
                method = "GET",
                url = urlFor(
                    "repos/${encodeSegment(config.owner)}/${encodeSegment(config.repo)}/contents/${encodePath(path)}?ref=${encodeSegment(config.branch)}",
                ),
                config = config,
            ).optString("sha").ifBlank { null }
        } catch (error: IOException) {
            if (error.message?.contains(" 404:") == true) {
                null
            } else {
                throw error
            }
        }
    }

    private fun requestMarkdownFromContents(config: GitHubConfig, path: String): String {
        val contentJson = requestJson(
            method = "GET",
            url = urlFor(
                "repos/${encodeSegment(config.owner)}/${encodeSegment(config.repo)}/contents/${encodePath(path)}?ref=${encodeSegment(config.branch)}",
            ),
            config = config,
        )
        val encodedContent = contentJson.getString("content").replace("\n", "")
        return String(Base64.getMimeDecoder().decode(encodedContent), StandardCharsets.UTF_8)
    }

    private fun documentTypeOrder(document: DiaryDocument): Int {
        return when (document.type) {
            com.cypress.diary.model.DiaryDocumentType.Year -> 0
            com.cypress.diary.model.DiaryDocumentType.Month -> 1
            com.cypress.diary.model.DiaryDocumentType.Week -> 2
        }
    }

    private fun requestJson(method: String, url: String, config: GitHubConfig, body: JSONObject? = null): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            setRequestProperty("User-Agent", "com.cypress.diary")
            if (config.token.isNotBlank()) {
                setRequestProperty("Authorization", "Bearer ${config.token}")
            }
            if (body != null) {
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                doOutput = true
                outputStream.use { stream ->
                    stream.write(body.toString().toByteArray(StandardCharsets.UTF_8))
                }
            }
        }

        return try {
            val code = connection.responseCode
            val responseBody = readBody(if (code in 200..299) connection.inputStream else connection.errorStream)
            if (code !in 200..299) {
                throw IOException("GitHub API ${connection.url} failed with $code: $responseBody")
            }
            JSONObject(responseBody)
        } finally {
            connection.disconnect()
        }
    }

    private fun readBody(stream: InputStream?): String {
        if (stream == null) return ""
        return stream.bufferedReader().use { it.readText() }
    }

    private fun urlFor(path: String): String = "${apiBaseUrl.trimEnd('/')}/$path"

    private fun rawUrlFor(config: GitHubConfig, path: String): String {
        return "https://raw.githubusercontent.com/${encodeSegment(config.owner)}/${encodeSegment(config.repo)}/${encodeSegment(config.branch)}/$path"
    }

    private fun encodeSegment(value: String): String {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20")
    }

    private fun encodePath(path: String): String {
        return path.split("/").joinToString("/") { encodeSegment(it) }
    }
}

data class GitHubRepoInfo(
    val fullName: String,
    val defaultBranch: String,
)
