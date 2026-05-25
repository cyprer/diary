package com.cypress.diary.model

import java.time.LocalDate

enum class DiaryDocumentType {
    Year,
    Month,
    Week,
}

data class DiaryDocument(
    val path: String,
    val type: DiaryDocumentType,
    val year: Int,
    val month: Int?,
    val weekIndex: Int?,
    val title: String,
    val published: LocalDate,
    val markdown: String,
    val body: String,
)
