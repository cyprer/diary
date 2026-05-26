package com.cypress.diary.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cypress.diary.ui.components.RefreshableScreen
import java.time.LocalDate

enum class EditMode(val label: String) {
    Day("当天"),
    Week("整周"),
}

@Composable
fun EditorScreen(
    mode: EditMode,
    draft: String,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    onModeChange: (EditMode) -> Unit,
    onDraftChange: (String) -> Unit,
    onSave: () -> Unit,
    onPush: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "编辑日记",
    pathText: String? = null,
    showModeSelector: Boolean = true,
    showPushButton: Boolean = false,
    contentLabel: String = if (mode == EditMode.Day) "当天内容" else "整周 Markdown",
    weekDayDrafts: List<Pair<LocalDate, String>> = emptyList(),
    onWeekDayDraftChange: (LocalDate, String) -> Unit = { _, _ -> },
) {
    RefreshableScreen(
        refreshing = refreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "返回")
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        if (showModeSelector) {
            androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                EditMode.entries.forEach { item ->
                    FilterChip(
                        selected = mode == item,
                        onClick = { onModeChange(item) },
                        label = { Text(item.label) },
                    )
                }
            }
        }

        pathText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
            )
        }

        OutlinedTextField(
            value = draft,
            onValueChange = onDraftChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 360.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            label = { Text(contentLabel) },
        )

        if (weekDayDrafts.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "本周日记",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                weekDayDrafts.forEach { (date, content) ->
                    OutlinedTextField(
                        value = content,
                        onValueChange = { onWeekDayDraftChange(date, it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 110.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        label = { Text("${date.monthValue}月${date.dayOfMonth}日") },
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("取消")
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Text(if (showPushButton) "本地保存" else "保存")
                }
            }
            if (showPushButton) {
                Button(
                    onClick = onPush,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("推送到 GitHub")
                }
            }
        }
    }
}
