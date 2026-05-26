package com.cypress.diary.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cypress.diary.model.todo.TodoItem
import com.cypress.diary.model.todo.TodoPriority
import com.cypress.diary.todo.formatReminderMillis
import com.cypress.diary.todo.isPastReminder
import com.cypress.diary.todo.parseReminderMillis
import com.cypress.diary.todo.reminderTextFor
import com.cypress.diary.ui.components.RefreshableScreen
import java.time.LocalDate
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TodoEditorScreen(
    item: TodoItem?,
    initialDate: LocalDate,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    onSave: (TodoItem) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var title by rememberSaveable(item?.id) { mutableStateOf(item?.title.orEmpty()) }
    var note by rememberSaveable(item?.id) { mutableStateOf(item?.note.orEmpty()) }
    var priorityName by rememberSaveable(item?.id) { mutableStateOf(item?.priority?.name ?: TodoPriority.Medium.name) }
    var dueDateText by rememberSaveable(item?.id) { mutableStateOf(item?.dueDate?.toString().orEmpty()) }
    var reminderText by rememberSaveable(item?.id) {
        mutableStateOf(item?.reminderAtMillis?.let(::formatReminderMillis).orEmpty())
    }
    var showDeleteConfirm by rememberSaveable(item?.id) { mutableStateOf(false) }
    val dueDate = dueDateText.takeIf { it.isNotBlank() }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    val dueDateInvalid = dueDateText.isNotBlank() && dueDate == null
    val reminderMillis = reminderText.takeIf { it.isNotBlank() }?.let(::parseReminderMillis)
    val reminderInvalid = reminderText.isNotBlank() && reminderMillis == null
    val reminderPast = reminderMillis?.let(::isPastReminder) == true
    val canSave = title.isNotBlank() && !dueDateInvalid && !reminderInvalid && !reminderPast
    val priority = TodoPriority.valueOf(priorityName)

    RefreshableScreen(
        refreshing = refreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = if (item == null) "新建待办" else "编辑待办",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("标题") },
            singleLine = true,
            isError = title.isBlank(),
        )

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("备注") },
            minLines = 3,
        )

        Text("截止日期", fontWeight = FontWeight.SemiBold)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = dueDateText.isBlank(),
                onClick = { dueDateText = "" },
                label = { Text("无日期") },
            )
            FilterChip(
                selected = dueDateText == initialDate.toString(),
                onClick = { dueDateText = initialDate.toString() },
                label = { Text("今天") },
            )
            FilterChip(
                selected = dueDateText == initialDate.plusDays(1).toString(),
                onClick = { dueDateText = initialDate.plusDays(1).toString() },
                label = { Text("明天") },
            )
        }

        OutlinedTextField(
            value = dueDateText,
            onValueChange = { dueDateText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("日期 YYYY-MM-DD") },
            singleLine = true,
            isError = dueDateInvalid,
            supportingText = {
                if (dueDateInvalid) {
                    Text("日期格式示例：2026-05-26")
                }
            },
        )

        Text("提醒时间", fontWeight = FontWeight.SemiBold)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = reminderText.isBlank(),
                onClick = { reminderText = "" },
                label = { Text("无提醒") },
            )
            FilterChip(
                selected = reminderText == reminderTextFor(initialDate, 20, 0),
                onClick = { reminderText = reminderTextFor(initialDate, 20, 0) },
                label = { Text("今天 20:00") },
            )
            FilterChip(
                selected = reminderText == reminderTextFor(initialDate.plusDays(1), 9, 0),
                onClick = { reminderText = reminderTextFor(initialDate.plusDays(1), 9, 0) },
                label = { Text("明天 09:00") },
            )
            if (dueDate != null) {
                FilterChip(
                    selected = reminderText == reminderTextFor(dueDate, 9, 0),
                    onClick = { reminderText = reminderTextFor(dueDate, 9, 0) },
                    label = { Text("截止日 09:00") },
                )
            }
        }

        OutlinedTextField(
            value = reminderText,
            onValueChange = { reminderText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("提醒 YYYY-MM-DD HH:mm") },
            singleLine = true,
            isError = reminderInvalid || reminderPast,
            supportingText = {
                when {
                    reminderInvalid -> Text("提醒格式示例：2026-05-26 20:00")
                    reminderPast -> Text("提醒时间必须晚于当前时间")
                }
            },
        )

        Text("优先级", fontWeight = FontWeight.SemiBold)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TodoPriority.entries.forEach { option ->
                FilterChip(
                    selected = priority == option,
                    onClick = { priorityName = option.name },
                    label = { Text(option.label) },
                )
            }
        }

        Button(
            enabled = canSave,
            onClick = {
                val now = System.currentTimeMillis()
                onSave(
                    TodoItem(
                        id = item?.id ?: UUID.randomUUID().toString(),
                        title = title.trim(),
                        note = note.trim(),
                        dueDate = dueDate,
                        reminderAtMillis = reminderMillis,
                        priority = priority,
                        completed = item?.completed ?: false,
                        createdAt = item?.createdAt ?: now,
                        updatedAt = now,
                        completedAt = item?.completedAt,
                    ),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("保存")
        }

        if (item != null) {
            TextButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("删除这个待办")
            }
        }
    }

    if (showDeleteConfirm && item != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除待办") },
            text = { Text("删除后无法恢复。") },
            confirmButton = {
                TextButton(onClick = { onDelete(item.id) }) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            },
        )
    }
}
