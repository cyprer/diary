package com.cypress.diary.model.todo

import java.time.LocalDate

enum class TodoPriority(val label: String, val rank: Int) {
    Low("低", 0),
    Medium("中", 1),
    High("高", 2),
}

data class TodoItem(
    val id: String,
    val title: String,
    val note: String,
    val dueDate: LocalDate?,
    val priority: TodoPriority,
    val completed: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long?,
)
