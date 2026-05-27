package com.cypress.diary.todo

import com.cypress.diary.model.todo.TodoItem
import com.cypress.diary.model.todo.TodoPriority
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class TodoDayItemsTest {
    @Test
    fun returnsItemsForSelectedDateOnly() {
        val selectedDate = LocalDate.of(2026, 5, 27)
        val today = item("today", selectedDate)
        val other = item("other", selectedDate.plusDays(1))
        val none = item("none", null)

        assertEquals(listOf(today), todoItemsForDate(listOf(other, none, today), selectedDate))
    }

    private fun item(id: String, date: LocalDate?): TodoItem {
        return TodoItem(
            id = id,
            title = id,
            note = "",
            dueDate = date,
            priority = TodoPriority.Medium,
            completed = false,
            createdAt = 1,
            updatedAt = 1,
            completedAt = null,
        )
    }
}
