package com.cypress.diary.quote

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DailyQuoteRepository(
    private val endpoint: String = "https://v1.hitokoto.cn/?encode=json",
) {
    suspend fun fetch(): String = withContext(Dispatchers.IO) {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "com.cypress.diary")
            connectTimeout = 5_000
            readTimeout = 5_000
        }

        try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                error("Hitokoto failed with $code")
            }
            JSONObject(body).optString("hitokoto").trim().ifBlank {
                error("Hitokoto response is empty")
            }
        } finally {
            connection.disconnect()
        }
    }
}
