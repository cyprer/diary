package com.cypress.diary.todo

import com.cypress.diary.model.todo.TodoItem
import com.cypress.diary.model.todo.TodoPriority
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class TodoFiltersTest {
    private val today = LocalDate.of(2026, 5, 26)

    @Test
    fun filtersTodayFutureCompletedAndAll() {
        val todayItem = item("today", dueDate = today)
        val futureItem = item("future", dueDate = today.plusDays(1))
        val unscheduled = item("none", dueDate = null)
        val done = item("done", completed = true, dueDate = today)
        val items = listOf(done, futureItem, unscheduled, todayItem)

        assertEquals(
            listOf("today", "future", "none", "done"),
            filterTodoItems(items, TodoFilter.All, today).map { it.id },
        )
        assertEquals(listOf("today"), filterTodoItems(items, TodoFilter.Today, today).map { it.id })
        assertEquals(listOf("future", "none"), filterTodoItems(items, TodoFilter.Future, today).map { it.id })
        assertEquals(listOf("done"), filterTodoItems(items, TodoFilter.Completed, today).map { it.id })
    }

    @Test
    fun sortsOpenDatedPriorityThenCompletedUpdatedDescending() {
        val lowToday = item("low", dueDate = today, priority = TodoPriority.Low, updatedAt = 1)
        val highToday = item("high", dueDate = today, priority = TodoPriority.High, updatedAt = 2)
        val tomorrow = item("tomorrow", dueDate = today.plusDays(1), priority = TodoPriority.High, updatedAt = 3)
        val noDate = item("none", dueDate = null, priority = TodoPriority.High, updatedAt = 4)
        val doneOld = item("done-old", completed = true, updatedAt = 10)
        val doneNew = item("done-new", completed = true, updatedAt = 20)

        assertEquals(
            listOf("high", "low", "tomorrow", "none", "done-new", "done-old"),
            sortTodoItems(listOf(doneOld, noDate, lowToday, doneNew, tomorrow, highToday)).map { it.id },
        )
    }

    private fun item(
        id: String,
        dueDate: LocalDate? = today,
        priority: TodoPriority = TodoPriority.Medium,
        completed: Boolean = false,
        updatedAt: Long = 1,
    ) = TodoItem(
        id = id,
        title = id,
        note = "",
        dueDate = dueDate,
        priority = priority,
        completed = completed,
        createdAt = 1,
        updatedAt = updatedAt,
        completedAt = if (completed) updatedAt else null,
    )
}
