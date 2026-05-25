package com.cypress.diary.ui.editor

import com.cypress.diary.model.DiaryDocument

class DraftContentResolver {
    fun resolveDayBody(remoteBody: String?, localDraft: String?): String? {
        return localDraft?.takeIf { it.isNotBlank() } ?: remoteBody
    }

    fun resolveDocument(document: DiaryDocument?, localDraft: String?): DiaryDocument? {
        if (document == null) return null
        if (localDraft.isNullOrBlank()) return document

        return document.withSummaryBody(localDraft)
    }
}
