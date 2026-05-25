package com.cypress.diary.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cypress.diary.model.DiaryDocument
import com.cypress.diary.model.DiaryDocumentType
import com.cypress.diary.ui.summary.MonthSummary
import com.cypress.diary.ui.summary.SummaryTree
import com.cypress.diary.ui.summary.YearSummary
import java.time.LocalDate

@Composable
fun WeekTree(
    tree: SummaryTree,
    onDocumentSelected: (DiaryDocument) -> Unit,
    modifier: Modifier = Modifier,
) {
    val expansionState = remember { WeekTreeExpansionState() }

    Column(modifier = modifier.fillMaxWidth()) {
        tree.years.forEach { year ->
            val yearKey = "year-${year.year}"
            val yearExpanded = expansionState.isExpanded(yearKey)
            TreeRow(
                title = "${year.year}年",
                subtitle = year.document?.title ?: "点击创建年度总结",
                expanded = yearExpanded,
                level = 0,
                selectable = true,
                onToggle = { expansionState.toggle(yearKey) },
                onSelect = { onDocumentSelected(year.document ?: newYearDocument(year.year)) },
            )
            if (yearExpanded) {
                YearContent(
                    year = year,
                    expansionState = expansionState,
                    onDocumentSelected = onDocumentSelected,
                )
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun YearContent(
    year: YearSummary,
    expansionState: WeekTreeExpansionState,
    onDocumentSelected: (DiaryDocument) -> Unit,
) {
    year.months.forEach { month ->
        val monthKey = "month-${year.year}-${month.month}"
        val monthExpanded = expansionState.isExpanded(monthKey)
        TreeRow(
            title = "${month.month}月",
            subtitle = month.document?.title ?: "点击创建月总结",
            expanded = monthExpanded,
            level = 1,
            selectable = true,
            onToggle = { expansionState.toggle(monthKey) },
            onSelect = { onDocumentSelected(month.document ?: newMonthDocument(month.year, month.month)) },
        )
        if (monthExpanded) {
            MonthContent(month = month, onDocumentSelected = onDocumentSelected)
        }
        HorizontalDivider(modifier = Modifier.padding(start = 24.dp))
    }
}

@Composable
private fun MonthContent(
    month: MonthSummary,
    onDocumentSelected: (DiaryDocument) -> Unit,
) {
    month.weeks.forEach { week ->
        WeekRow(
            title = "第${week.weekIndex}周",
            subtitle = week.document.title,
            level = 2,
            onSelect = { onDocumentSelected(week.document) },
        )
    }
}

@Composable
private fun TreeRow(
    title: String,
    subtitle: String,
    expanded: Boolean,
    level: Int,
    selectable: Boolean,
    onToggle: () -> Unit,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (8 + level * 22).dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onToggle) {
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = if (expanded) "收起" else "展开",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(enabled = selectable, onClick = onSelect)
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        IconButton(onClick = onToggle) {
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = if (expanded) "收起" else "展开",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun WeekRow(
    title: String,
    subtitle: String,
    level: Int,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(start = (18 + level * 22).dp, end = 18.dp, top = 12.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Description,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun newYearDocument(year: Int): DiaryDocument {
    val title = "${year}年总结"
    val path = "src/content/posts/summary/${year % 100}year/index.md"
    val published = LocalDate.of(year, 1, 1)
    return newSummaryDocument(
        path = path,
        type = DiaryDocumentType.Year,
        year = year,
        month = null,
        weekIndex = null,
        title = title,
        published = published,
    )
}

private fun newMonthDocument(year: Int, month: Int): DiaryDocument {
    val title = "${year}年${month}月总结"
    val path = "src/content/posts/summary/${year % 100}year/${month}month/index.md"
    val published = LocalDate.of(year, month, 1)
    return newSummaryDocument(
        path = path,
        type = DiaryDocumentType.Month,
        year = year,
        month = month,
        weekIndex = null,
        title = title,
        published = published,
    )
}

private fun newSummaryDocument(
    path: String,
    type: DiaryDocumentType,
    year: Int,
    month: Int?,
    weekIndex: Int?,
    title: String,
    published: LocalDate,
): DiaryDocument {
    val markdown = """
        ---
        title: "${title}"
        published: ${published}
        ---

        # ${title}
    """.trimIndent()
    return DiaryDocument(
        path = path,
        type = type,
        year = year,
        month = month,
        weekIndex = weekIndex,
        title = title,
        published = published,
        markdown = markdown,
        body = "",
    )
}
