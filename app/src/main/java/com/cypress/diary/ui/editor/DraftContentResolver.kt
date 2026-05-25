package com.cypress.diary.ui.editor

import com.cypress.diary.model.DiaryDocument
import com.cypress.diary.parser.DiaryDocumentCodec

class DraftContentResolver(
    private val documentCodec: DiaryDocumentCodec = DiaryDocumentCodec(),
) {
    fun resolveDayBody(remoteBody: String?, localDraft: String?): String? {
        return localDraft?.takeIf { it.isNotBlank() } ?: remoteBody
    }

    fun resolveDocument(document: DiaryDocument?, localDraft: String?): DiaryDocument? {
        if (document == null) return null
        if (localDraft.isNullOrBlank()) return document

        return runCatching {
            documentCodec.parse(document.path, localDraft)
        }.getOrElse {
            document.copy(
                markdown = localDraft,
                body = localDraft,
            )
        }
    }
}
