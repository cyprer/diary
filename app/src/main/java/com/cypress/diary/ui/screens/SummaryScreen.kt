package com.cypress.diary.ui.screens

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cypress.diary.model.DiaryDocument
import com.cypress.diary.model.DiaryDocumentType
import com.cypress.diary.ui.components.MarkdownDocumentView
import com.cypress.diary.ui.components.RefreshableScreen
import com.cypress.diary.ui.components.WeekTree
import com.cypress.diary.ui.summary.SummaryTree
import com.cypress.diary.ui.summary.nextSummaryDocument
import com.cypress.diary.ui.summary.previousSummaryDocument
import com.cypress.diary.ui.summary.weekSummaryDays

@Composable
fun SummaryScreen(
    tree: SummaryTree,
    selectedDocument: DiaryDocument?,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onDocumentSelected: (DiaryDocument) -> Unit,
    onDocumentDismiss: () -> Unit,
    onEditDocument: (DiaryDocument) -> Unit,
    modifier: Modifier = Modifier,
) {
    RefreshableScreen(
        refreshing = refreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "时间总结",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "点击年份、月份或周查看总结，点击箭头展开。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
        ) {
            WeekTree(
                tree = tree,
                onDocumentSelected = onDocumentSelected,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
    }

    if (selectedDocument != null) {
        val previousDocument = tree.previousSummaryDocument(selectedDocument)
        val nextDocument = tree.nextSummaryDocument(selectedDocument)
        AlertDialog(
            onDismissRequest = onDocumentDismiss,
            title = { Text(selectedDocument.title) },
            text = {
                if (selectedDocument.type == DiaryDocumentType.Week) {
                    WeekSummaryDocumentView(
                        document = selectedDocument,
                        modifier = Modifier
                            .heightIn(max = 420.dp)
                            .verticalScroll(rememberScrollState()),
                    )
                } else {
                    MarkdownDocumentView(
                        document = selectedDocument,
                        showTitle = false,
                        modifier = Modifier
                            .heightIn(max = 420.dp)
                            .verticalScroll(rememberScrollState()),
                    )
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(
                        enabled = previousDocument != null,
                        onClick = { previousDocument?.let(onDocumentSelected) },
                    ) {
                        Text("上一个")
                    }
                    TextButton(
                        enabled = nextDocument != null,
                        onClick = { nextDocument?.let(onDocumentSelected) },
                    ) {
                        Text("下一个")
                    }
                    TextButton(onClick = { onEditDocument(selectedDocument) }) {
                        Text("编辑")
                    }
                }
            },
        )
    }
}

@Composable
private fun WeekSummaryDocumentView(
    document: DiaryDocument,
    modifier: Modifier = Modifier,
) {
    val days = remember(document.path, document.markdown) {
        weekSummaryDays(document)
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (document.body.isNotBlank()) {
            MarkdownDocumentView(
                document = document.copy(body = document.body),
                showTitle = false,
            )
        }
        days.forEach { day ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${day.date.monthValue}月${day.date.dayOfMonth}日",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = day.content.ifBlank { "这天还没有内容。" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.76f),
                )
            }
        }
    }
}
