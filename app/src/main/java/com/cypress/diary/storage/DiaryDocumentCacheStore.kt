package com.cypress.diary.storage

import com.cypress.diary.model.DiaryDocument
import com.cypress.diary.model.DiaryDocumentType
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.util.Base64

class DiaryDocumentCacheStore(
    private val preferences: PreferenceStore,
) {
    fun loadDocuments(): List<DiaryDocument> {
        return loadEncodedPaths().mapNotNull { encodedPath ->
            val value = preferences.getString(cacheKey(encodedPath), null) ?: return@mapNotNull null
            runCatching { decodeDocument(value) }.getOrNull()
        }.sortedWith(compareBy({ it.year }, { it.month ?: 0 }, { typeOrder(it.type) }, { it.weekIndex ?: 0 }))
    }

    fun saveDocuments(documents: List<DiaryDocument>) {
        val previousEncodedPaths = loadEncodedPaths().toSet()
        val encodedPaths = documents.map { document ->
            val encodedPath = encode(document.path)
            preferences.putString(cacheKey(encodedPath), encodeDocument(document))
            encodedPath
        }
        (previousEncodedPaths - encodedPaths.toSet()).forEach { stalePath ->
            preferences.remove(cacheKey(stalePath))
        }
        preferences.putString(KEY_INDEX, encodedPaths.joinToString("\n"))
    }

    fun clear() {
        loadEncodedPaths().forEach { encodedPath ->
            preferences.remove(cacheKey(encodedPath))
        }
        preferences.remove(KEY_INDEX)
    }

    private fun loadEncodedPaths(): List<String> {
        return preferences.getString(KEY_INDEX, "")
            .orEmpty()
            .split('\n')
            .filter { it.isNotBlank() }
    }

    private fun encodeDocument(document: DiaryDocument): String {
        return listOf(
            encode(document.path),
            document.type.name,
            document.year.toString(),
            document.month?.toString().orEmpty(),
            document.weekIndex?.toString().orEmpty(),
            document.published.toString(),
            encode(document.title),
            encode(document.markdown),
            encode(document.body),
        ).joinToString("\n")
    }

    private fun decodeDocument(value: String): DiaryDocument {
        val lines = value.split('\n')
        require(lines.size == 9) { "invalid cached document" }
        return DiaryDocument(
            path = decode(lines[0]),
            type = DiaryDocumentType.valueOf(lines[1]),
            year = lines[2].toInt(),
            month = lines[3].ifBlank { null }?.toInt(),
            weekIndex = lines[4].ifBlank { null }?.toInt(),
            published = LocalDate.parse(lines[5]),
            title = decode(lines[6]),
            markdown = decode(lines[7]),
            body = decode(lines[8]),
        )
    }

    private fun cacheKey(encodedPath: String): String = "$KEY_PREFIX$encodedPath"

    private fun encode(value: String): String {
        return Base64.getUrlEncoder().encodeToString(value.toByteArray(StandardCharsets.UTF_8))
    }

    private fun decode(value: String): String {
        return String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8)
    }

    private fun typeOrder(type: DiaryDocumentType): Int {
        return when (type) {
            DiaryDocumentType.Year -> 0
            DiaryDocumentType.Month -> 1
            DiaryDocumentType.Week -> 2
        }
    }

    companion object {
        private const val KEY_INDEX = "document_cache_index"
        private const val KEY_PREFIX = "document_cache:"
    }
}
