package com.cypress.diary.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cypress.diary.model.DiaryDocument

@Composable
fun MarkdownDocumentView(
    document: DiaryDocument,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (showTitle) {
            Text(
                text = document.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        document.body.lines().forEach { line ->
            MarkdownLine(line = line)
        }
    }
}

@Composable
private fun MarkdownLine(line: String) {
    when {
        line.startsWith("### ") -> Text(
            text = line.removePrefix("### ").trim(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        line.startsWith("## ") -> Text(
            text = line.removePrefix("## ").trim(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        line.startsWith("# ") -> Text(
            text = line.removePrefix("# ").trim(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        line.isBlank() -> Text(text = "")
        else -> Text(
            text = line,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
