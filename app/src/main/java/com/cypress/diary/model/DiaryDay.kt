package com.cypress.diary.model

import java.time.LocalDate

data class DiaryDay(
    val date: LocalDate,
    val content: String,
)
