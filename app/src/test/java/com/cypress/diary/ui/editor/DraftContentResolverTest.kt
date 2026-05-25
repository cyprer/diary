package com.cypress.diary.ui.editor

import com.cypress.diary.model.DiaryDocument
import com.cypress.diary.model.DiaryDocumentType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

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
    fun documentUsesLocalBodyDraftBeforeRemoteDocument() {
        val remote = document(title = "远程标题", body = "远程内容")

        val resolved = requireNotNull(resolver.resolveDocument(remote, "本地总结内容"))

        assertEquals("远程标题", resolved.title)
        assertEquals("本地总结内容", resolved.body.trim())
        assertEquals(
            """
                # 远程标题

                本地总结内容
            """.trimIndent(),
            resolved.markdown,
        )
    }

    @Test
    fun documentBodyDraftKeepsWeeklyDaySectionsHiddenFromEditor() {
        val remote = document(title = "第一周周记", body = "周开头。").copy(
            markdown = """
                ---
                title: "第一周周记"
                published: 2025-01-01
                ---

                # 第一周周记

                周开头。

                ## 1.1

                第一天。
            """.trimIndent(),
        )

        val resolved = requireNotNull(resolver.resolveDocument(remote, "新的周结正文"))

        assertEquals("新的周结正文", resolved.body)
        assertEquals(
            """
                ---
                title: "第一周周记"
                published: 2025-01-01
                ---

                # 第一周周记

                新的周结正文

                ## 1.1

                第一天。
            """.trimIndent(),
            resolved.markdown,
        )
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
