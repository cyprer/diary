package com.cypress.diary.todo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class TodoReminderTimeTest {
    private val zone = ZoneId.of("Asia/Shanghai")

    @Test
    fun parsesAndFormatsReminderText() {
        val millis = parseReminderMillis("2026-05-26 20:30", zone)

        assertEquals("2026-05-26 20:30", formatReminderMillis(requireNotNull(millis), zone))
    }

    @Test
    fun rejectsMalformedReminderText() {
        assertNull(parseReminderMillis("2026/05/26 20:30", zone))
        assertNull(parseReminderMillis("2026-05-26", zone))
    }

    @Test
    fun buildsQuickReminderText() {
        val date = LocalDate.of(2026, 5, 26)

        assertEquals("2026-05-26 20:00", reminderTextFor(date, hour = 20, minute = 0))
    }

    @Test
    fun detectsPastReminders() {
        val now = requireNotNull(parseReminderMillis("2026-05-26 20:30", zone))
        val past = requireNotNull(parseReminderMillis("2026-05-26 20:29", zone))

        assertTrue(isPastReminder(past, now))
    }
}
