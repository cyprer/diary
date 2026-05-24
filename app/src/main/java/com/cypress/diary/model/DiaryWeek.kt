package com.cypress.diary.model

import java.time.LocalDate

data class DiaryWeek(
    val key: WeekKey,
    val title: String,
    val published: LocalDate,
    val description: String,
    val tags: List<String>,
    val category: String,
    val draft: Boolean,
    val days: List<DiaryDay>,
)
