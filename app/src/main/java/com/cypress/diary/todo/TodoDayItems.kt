package com.cypress.diary.todo

import com.cypress.diary.model.todo.TodoItem
import java.time.LocalDate

fun todoItemsForDate(items: List<TodoItem>, date: LocalDate): List<TodoItem> {
    return sortTodoItems(items.filter { item -> item.dueDate == date })
}
