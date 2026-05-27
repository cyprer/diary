package com.cypress.diary.todo

import com.cypress.diary.model.todo.TodoItem

fun shouldScheduleReminder(item: TodoItem, nowMillis: Long = System.currentTimeMillis()): Boolean {
    val reminderAt = item.reminderAtMillis ?: return false
    return !item.completed && reminderAt > nowMillis
}

fun hasFutureTodoReminders(items: List<TodoItem>, nowMillis: Long = System.currentTimeMillis()): Boolean {
    return items.any { item -> shouldScheduleReminder(item, nowMillis) }
}
