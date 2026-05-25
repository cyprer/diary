package com.cypress.diary.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
    date: LocalDate,
    mode: EditMode,
    draft: String,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    onModeChange: (EditMode) -> Unit,
    onDraftChange: (String) -> Unit,
    onPush: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "编辑日记",
    pathText: String = "路径：${date.year} / ${date.monthValue} / ${date.dayOfMonth}",
    showModeSelector: Boolean = true,
    contentLabel: String = if (mode == EditMode.Day) "当天内容" else "整周 Markdown",
) {
    RefreshableScreen(
        refreshing = refreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        androidx.compose.foundation.layout.Row(
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

        Text(
            text = pathText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
        )

        OutlinedTextField(
            value = draft,
            onValueChange = onDraftChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 360.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            label = { Text(contentLabel) },
        )

        Button(
            onClick = onPush,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Filled.Check, contentDescription = null)
            Text(text = "推送")
        }
    }
}
