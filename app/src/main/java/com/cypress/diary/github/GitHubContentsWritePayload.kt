package com.cypress.diary.github

import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.Base64

data class GitHubContentsWritePayload(
    val message: String,
    val branch: String,
    val content: String,
    val sha: String? = null,
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("message", message)
            put("branch", branch)
            put("content", content)
            if (!sha.isNullOrBlank()) {
                put("sha", sha)
            }
        }
    }

    companion object {
        fun fromMarkdown(markdown: String, branch: String, message: String, sha: String? = null): GitHubContentsWritePayload {
            return GitHubContentsWritePayload(
                message = message,
                branch = branch,
                content = Base64.getEncoder().encodeToString(markdown.toByteArray(StandardCharsets.UTF_8)),
                sha = sha?.trim()?.ifBlank { null },
            )
        }
    }
}
