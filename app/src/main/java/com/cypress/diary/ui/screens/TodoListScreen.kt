package com.cypress.diary.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.cypress.diary.model.todo.TodoItem
import com.cypress.diary.todo.TodoFilter
import com.cypress.diary.todo.filterTodoItems
import com.cypress.diary.todo.formatReminderMillis
import com.cypress.diary.ui.components.RefreshableScreen
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TodoListScreen(
    items: List<TodoItem>,
    selectedDate: LocalDate,
    selectedFilter: TodoFilter,
    onFilterChange: (TodoFilter) -> Unit,
    onItemSelected: (TodoItem) -> Unit,
    onToggleCompleted: (TodoItem) -> Unit,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val visibleItems = filterTodoItems(items, selectedFilter, selectedDate)

    RefreshableScreen(
        refreshing = refreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "待办",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TodoFilter.entries.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterChange(filter) },
                    label = { Text(filter.label) },
                )
            }
        }

        if (visibleItems.isEmpty()) {
            Text(
                text = emptyText(selectedFilter),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
            )
        } else {
            visibleItems.forEach { item ->
                TodoItemRow(
                    item = item,
                    onClick = { onItemSelected(item) },
                    onToggleCompleted = { onToggleCompleted(item) },
                )
            }
        }
    }
}

@Composable
private fun TodoItemRow(
    item: TodoItem,
    onClick: () -> Unit,
    onToggleCompleted: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Checkbox(
                checked = item.completed,
                onCheckedChange = { onToggleCompleted() },
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (item.completed) TextDecoration.LineThrough else TextDecoration.None,
                )
                if (item.note.isNotBlank()) {
                    Text(
                        text = item.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
                    )
                }
                Text(
                    text = itemMetaText(item),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                )
            }
        }
    }
}

private fun itemMetaText(item: TodoItem): String {
    val due = item.dueDate?.let { "${it.monthValue}月${it.dayOfMonth}日" } ?: "无截止日期"
    val reminder = item.reminderAtMillis
        ?.let { " · ${item.reminderMode.label} ${formatReminderMillis(it)}" }
        .orEmpty()
    return "$due · ${item.priority.label}$reminder"
}

private fun emptyText(filter: TodoFilter): String {
    return when (filter) {
        TodoFilter.All -> "还没有待办"
        TodoFilter.Today -> "今天没有待办"
        TodoFilter.Future -> "未来没有待办"
        TodoFilter.Completed -> "还没有完成的待办"
    }
}
