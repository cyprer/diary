package com.cypress.diary.ui.editor

import com.cypress.diary.model.DiaryDocument
import com.cypress.diary.model.DiaryDocumentType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DraftContentResolverTest {
    private val resolver = DraftContentResolver()

    @Test
    fun dayBodyUsesLocalDraftBeforeRemoteBody() {
        assertEquals(
            "本地今天写的内容",
            resolver.resolveDayBody(
                remoteBody = "远程昨天的内容",
                localDraft = "本地今天写的内容",
            ),
        )
    }

    @Test
    fun dayBodyFallsBackToRemoteWhenDraftIsBlank() {
        assertEquals(
            "远程内容",
            resolver.resolveDayBody(
                remoteBody = "远程内容",
                localDraft = "",
            ),
        )
    }

    @Test
    fun documentUsesLocalMarkdownDraftBeforeRemoteDocument() {
        val remote = document(title = "远程标题", body = "远程内容")
        val draft = """
            ---
            title: "本地标题"
            published: 2025-01-01
            description: "本地标题"
            tags: ["周报", "总结"]
            category: "周报"
            draft: false
            ---

            # 本地标题

            本地草稿内容
        """.trimIndent()

        val resolved = requireNotNull(resolver.resolveDocument(remote, draft))

        assertEquals("本地标题", resolved.title)
        assertEquals("本地草稿内容", resolved.body.trim())
        assertEquals(draft, resolved.markdown)
    }

    private fun document(title: String, body: String): DiaryDocument {
        return DiaryDocument(
            path = "src/content/posts/summary/25year/index.md",
            type = DiaryDocumentType.Year,
            year = 2025,
            month = null,
            weekIndex = null,
            title = title,
            published = LocalDate.of(2025, 1, 1),
            markdown = "# $title\n\n$body",
            body = body,
        )
    }
}
