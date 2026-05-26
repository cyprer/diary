package com.cypress.diary.todo

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val ReminderFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

fun parseReminderMillis(text: String, zoneId: ZoneId = ZoneId.systemDefault()): Long? {
    return runCatching {
        LocalDateTime.parse(text.trim(), ReminderFormatter)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()
    }.getOrNull()
}

fun formatReminderMillis(millis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String {
    return java.time.Instant.ofEpochMilli(millis)
        .atZone(zoneId)
        .toLocalDateTime()
        .format(ReminderFormatter)
}

fun reminderTextFor(date: LocalDate, hour: Int, minute: Int): String {
    return date.atTime(hour, minute).format(ReminderFormatter)
}

fun isPastReminder(reminderAtMillis: Long, nowMillis: Long = System.currentTimeMillis()): Boolean {
    return reminderAtMillis <= nowMillis
}
