package com.cypress.diary.storage

import com.cypress.diary.model.todo.TodoItem
import com.cypress.diary.model.todo.TodoPriority
import com.cypress.diary.todo.sortTodoItems
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.util.Base64

class TodoItemStore(
    private val preferences: PreferenceStore,
) {
    constructor(prefs: android.content.SharedPreferences) : this(SharedPreferencesPreferenceStore(prefs))

    fun loadItems(): List<TodoItem> {
        return preferences.getString(KEY_ITEMS, "")
            .orEmpty()
            .lineSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { line -> runCatching { decode(line) }.getOrNull() }
            .toList()
            .let(::sortTodoItems)
    }

    fun saveItems(items: List<TodoItem>) {
        preferences.putString(KEY_ITEMS, sortTodoItems(items).joinToString("\n") { encode(it) })
    }

    fun upsert(item: TodoItem) {
        saveItems(loadItems().filterNot { it.id == item.id } + item)
    }

    fun delete(id: String) {
        saveItems(loadItems().filterNot { it.id == id })
    }

    companion object {
        private const val KEY_ITEMS = "todo_items"

        fun encode(item: TodoItem): String {
            return listOf(
                safe(item.id),
                safe(item.title),
                safe(item.note),
                item.dueDate?.toString().orEmpty(),
                item.priority.name,
                item.completed.toString(),
                item.createdAt.toString(),
                item.updatedAt.toString(),
                item.completedAt?.toString().orEmpty(),
                item.reminderAtMillis?.toString().orEmpty(),
            ).joinToString("|")
        }

        private fun decode(value: String): TodoItem {
            val parts = value.split('|')
            require(parts.size == 9 || parts.size == 10) { "invalid todo item" }
            return TodoItem(
                id = unsafe(parts[0]),
                title = unsafe(parts[1]),
                note = unsafe(parts[2]),
                dueDate = parts[3].takeIf { it.isNotBlank() }?.let(LocalDate::parse),
                priority = TodoPriority.valueOf(parts[4]),
                completed = parts[5].toBooleanStrict(),
                createdAt = parts[6].toLong(),
                updatedAt = parts[7].toLong(),
                completedAt = parts[8].takeIf { it.isNotBlank() }?.toLong(),
                reminderAtMillis = parts.getOrNull(9)?.takeIf { it.isNotBlank() }?.toLong(),
            )
        }

        private fun safe(value: String): String {
            return Base64.getUrlEncoder().encodeToString(value.toByteArray(StandardCharsets.UTF_8))
        }

        private fun unsafe(value: String): String {
            return String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8)
        }
    }
}
