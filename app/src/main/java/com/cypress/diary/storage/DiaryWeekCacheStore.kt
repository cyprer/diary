package com.cypress.diary.storage

import com.cypress.diary.model.DiaryWeek
import com.cypress.diary.parser.DiaryMarkdownCodec
import java.nio.charset.StandardCharsets
import java.util.Base64

class DiaryWeekCacheStore(
    private val preferences: PreferenceStore,
    private val codec: DiaryMarkdownCodec = DiaryMarkdownCodec(),
) {
    fun loadWeeks(): List<DiaryWeek> {
        val index = preferences.getString(KEY_INDEX, "").orEmpty()
        if (index.isBlank()) return emptyList()

        return index
            .split('\n')
            .filter { it.isNotBlank() }
            .mapNotNull { encodedKey ->
                val markdown = preferences.getString(cacheKey(encodedKey), null) ?: return@mapNotNull null
                runCatching { codec.parse(decode(markdown)) }.getOrNull()
            }
            .sortedWith(compareBy({ it.key.year }, { it.key.month }, { it.key.weekIndex }))
    }

    fun saveWeeks(weeks: List<DiaryWeek>) {
        val encodedKeys = weeks.map { week ->
            val encodedPath = encode("${week.key.year}-${week.key.month}-${week.key.weekIndex}")
            preferences.putString(cacheKey(encodedPath), encode(codec.render(week)))
            encodedPath
        }
        preferences.putString(KEY_INDEX, encodedKeys.joinToString("\n"))
    }

    fun clear() {
        loadEncodedKeys().forEach { encodedKey ->
            preferences.remove(cacheKey(encodedKey))
        }
        preferences.remove(KEY_INDEX)
    }

    private fun loadEncodedKeys(): List<String> {
        return preferences.getString(KEY_INDEX, "")
            .orEmpty()
            .split('\n')
            .filter { it.isNotBlank() }
    }

    private fun cacheKey(encodedKey: String): String = "$KEY_PREFIX$encodedKey"

    private fun encode(value: String): String {
        return Base64.getUrlEncoder().encodeToString(value.toByteArray(StandardCharsets.UTF_8))
    }

    private fun decode(value: String): String {
        return String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8)
    }

    companion object {
        private const val KEY_INDEX = "week_cache_index"
        private const val KEY_PREFIX = "week_cache:"
    }
}
