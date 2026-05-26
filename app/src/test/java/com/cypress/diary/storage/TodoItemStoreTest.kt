package com.cypress.diary.storage

import com.cypress.diary.model.todo.TodoItem
import com.cypress.diary.model.todo.TodoPriority
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class TodoItemStoreTest {
    @Test
    fun savesAndLoadsItemsSorted() {
        val store = TodoItemStore(InMemoryPreferenceStore())
        val low = item("low", priority = TodoPriority.Low)
        val high = item("high", priority = TodoPriority.High)

        store.saveItems(listOf(low, high))

        assertEquals(listOf(high, low), store.loadItems())
    }

    @Test
    fun upsertReplacesExistingItemById() {
        val store = TodoItemStore(InMemoryPreferenceStore())
        store.saveItems(listOf(item("a", title = "old")))

        store.upsert(item("a", title = "new"))

        assertEquals(listOf(item("a", title = "new")), store.loadItems())
    }

    @Test
    fun deleteRemovesItemById() {
        val store = TodoItemStore(InMemoryPreferenceStore())
        store.saveItems(listOf(item("a"), item("b")))

        store.delete("a")

        assertEquals(listOf(item("b")), store.loadItems())
    }

    @Test
    fun malformedItemsAreSkipped() {
        val prefs = InMemoryPreferenceStore()
        prefs.putString("todo_items", "bad-line\n${TodoItemStore.encode(item("ok"))}")

        assertEquals(listOf(item("ok")), TodoItemStore(prefs).loadItems())
    }

    @Test
    fun nullableDatesRoundTrip() {
        val store = TodoItemStore(InMemoryPreferenceStore())
        val item = item("none", dueDate = null, completed = true, completedAt = 30)

        store.saveItems(listOf(item))

        assertEquals(listOf(item), store.loadItems())
    }

    @Test
    fun reminderTimeRoundTrips() {
        val store = TodoItemStore(InMemoryPreferenceStore())
        val item = item("reminder", reminderAtMillis = 1_779_811_200_000)

        store.saveItems(listOf(item))

        assertEquals(listOf(item), store.loadItems())
    }

    @Test
    fun legacyItemsWithoutReminderStillLoad() {
        val prefs = InMemoryPreferenceStore()
        val legacy = listOf(
            "bGVnYWN5",
            "TGVnYWN5",
            "",
            "2026-05-26",
            "Medium",
            "false",
            "10",
            "20",
            "",
        ).joinToString("|")
        prefs.putString("todo_items", legacy)

        assertEquals(listOf(item("legacy", title = "Legacy", note = "")), TodoItemStore(prefs).loadItems())
    }

    private fun item(
        id: String,
        title: String = id,
        note: String = "note | with separator",
        dueDate: LocalDate? = LocalDate.of(2026, 5, 26),
        priority: TodoPriority = TodoPriority.Medium,
        completed: Boolean = false,
        createdAt: Long = 10,
        updatedAt: Long = 20,
        completedAt: Long? = if (completed) updatedAt else null,
        reminderAtMillis: Long? = null,
    ): TodoItem {
        return TodoItem(
            id = id,
            title = title,
            note = note,
            dueDate = dueDate,
            priority = priority,
            completed = completed,
            createdAt = createdAt,
            updatedAt = updatedAt,
            completedAt = completedAt,
            reminderAtMillis = reminderAtMillis,
        )
    }

    private class InMemoryPreferenceStore : PreferenceStore {
        private val values = mutableMapOf<String, String?>()

        override fun getString(key: String, defaultValue: String?): String? {
            return values.getOrDefault(key, defaultValue)
        }

        override fun putString(key: String, value: String?) {
            values[key] = value
        }

        override fun remove(key: String) {
            values.remove(key)
        }
    }
}
