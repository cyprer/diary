package com.cypress.diary.github

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class GitHubContentsWriteRequestTest {
    @Test
    fun buildsPayloadWithOptionalSha() {
        val payload = GitHubContentsWritePayload.fromMarkdown(
            markdown = "# title",
            branch = "main",
            message = "update diary",
            sha = "abc123",
        )

        assertEquals("update diary", payload.message)
        assertEquals("main", payload.branch)
        assertEquals("abc123", payload.sha)
        assertFalse(payload.content.isBlank())
    }
}
