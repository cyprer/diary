package com.cypress.diary.todo

import com.cypress.diary.model.todo.TodoItem
import com.cypress.diary.model.todo.TodoPriority
import com.cypress.diary.model.todo.TodoReminderMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TodoReminderSchedulingTest {
    @Test
    fun schedulesOnlyIncompleteFutureReminders() {
        val now = 100L

        assertTrue(shouldScheduleReminder(item(reminderAtMillis = 101L), now))
        assertFalse(shouldScheduleReminder(item(reminderAtMillis = 100L), now))
        assertFalse(shouldScheduleReminder(item(reminderAtMillis = null), now))
        assertFalse(shouldScheduleReminder(item(reminderAtMillis = 101L, completed = true), now))
    }

    @Test
    fun detectsPendingFutureReminders() {
        val now = 100L

        assertTrue(
            hasFutureTodoReminders(
                listOf(
                    item(reminderAtMillis = null),
                    item(reminderAtMillis = 101L),
                ),
                now,
            ),
        )
        assertFalse(
            hasFutureTodoReminders(
                listOf(
                    item(reminderAtMillis = 100L),
                    item(reminderAtMillis = 101L, completed = true),
                ),
                now,
            ),
        )
    }

    @Test
    fun detectsFutureAlarmModeReminders() {
        val now = 100L

        assertTrue(
            hasFutureAlarmModeReminders(
                listOf(
                    item(reminderAtMillis = 101L, reminderMode = TodoReminderMode.Notification),
                    item(reminderAtMillis = 101L, reminderMode = TodoReminderMode.Alarm),
                ),
                now,
            ),
        )
        assertFalse(
            hasFutureAlarmModeReminders(
                listOf(
                    item(reminderAtMillis = 101L, reminderMode = TodoReminderMode.Notification),
                    item(reminderAtMillis = 101L, reminderMode = TodoReminderMode.Vibration),
                ),
                now,
            ),
        )
    }

    private fun item(
        reminderAtMillis: Long?,
        completed: Boolean = false,
        reminderMode: TodoReminderMode = TodoReminderMode.Alarm,
    ): TodoItem {
        return TodoItem(
            id = "id",
            title = "title",
            note = "",
            dueDate = null,
            priority = TodoPriority.Medium,
            completed = completed,
            createdAt = 1,
            updatedAt = 2,
            completedAt = null,
            reminderAtMillis = reminderAtMillis,
            reminderMode = reminderMode,
        )
    }
}
