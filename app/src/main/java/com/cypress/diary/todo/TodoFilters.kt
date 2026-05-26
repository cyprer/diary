package com.cypress.diary.todo

import com.cypress.diary.model.todo.TodoItem
import java.time.LocalDate

enum class TodoFilter(val label: String) {
    All("全部"),
    Today("今天"),
    Future("未来"),
    Completed("已完成"),
}

fun filterTodoItems(items: List<TodoItem>, filter: TodoFilter, selectedDate: LocalDate): List<TodoItem> {
    return when (filter) {
        TodoFilter.All -> items
        TodoFilter.Today -> items.filter { !it.completed && it.dueDate == selectedDate }
        TodoFilter.Future -> items.filter { !it.completed && (it.dueDate == null || it.dueDate > selectedDate) }
        TodoFilter.Completed -> items.filter { it.completed }
    }.let(::sortTodoItems)
}

fun sortTodoItems(items: List<TodoItem>): List<TodoItem> {
    return items.sortedWith(
        compareBy<TodoItem> { it.completed }
            .thenBy { if (it.completed) 1 else if (it.dueDate == null) 1 else 0 }
            .thenBy { it.dueDate ?: LocalDate.MAX }
            .thenByDescending { it.priority.rank }
            .thenByDescending { it.updatedAt },
    )
}
