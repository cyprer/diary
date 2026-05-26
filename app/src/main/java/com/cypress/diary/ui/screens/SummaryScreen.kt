package com.cypress.diary.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cypress.diary.model.DiaryDocument
import com.cypress.diary.model.DiaryDocumentType
import com.cypress.diary.model.WeekKey
import com.cypress.diary.ui.components.MarkdownDocumentView
import com.cypress.diary.ui.components.RefreshableScreen
import com.cypress.diary.ui.components.WeekTree
import com.cypress.diary.ui.summary.SummaryTree
import com.cypress.diary.ui.summary.SummaryWordPoint
import com.cypress.diary.ui.summary.monthLocalWeekDates
import com.cypress.diary.ui.summary.monthlyWordCountsForYear
import com.cypress.diary.ui.summary.nextSummaryDocument
import com.cypress.diary.ui.summary.previousSummaryDocument
import com.cypress.diary.ui.summary.weekDayWordCounts
import com.cypress.diary.ui.summary.weekSummaryDays
import com.cypress.diary.ui.summary.weekWordCount
import com.cypress.diary.ui.summary.weeklyWordCountsForMonth
import java.time.LocalDate
import java.time.YearMonth

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
    val statsModeName = rememberSaveable { mutableStateOf(DiarySummaryStatsMode.Year.name) }
    val statsDateValue = rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    val statsMode = DiarySummaryStatsMode.valueOf(statsModeName.value)
    val statsDate = LocalDate.parse(statsDateValue.value)

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

        DiarySummaryStatsPanel(
            tree = tree,
            selectedMode = statsMode,
            onModeChange = { statsModeName.value = it.name },
            selectedDate = statsDate,
            onDateChange = { statsDateValue.value = it.toString() },
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

private enum class DiarySummaryStatsMode(val label: String) {
    Year("年结"),
    Month("月结"),
    Week("周结"),
}

@Composable
private fun DiarySummaryStatsPanel(
    tree: SummaryTree,
    selectedMode: DiarySummaryStatsMode,
    onModeChange: (DiarySummaryStatsMode) -> Unit,
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DiarySummaryStatsModeSelector(selectedMode, onModeChange)
            when (selectedMode) {
                DiarySummaryStatsMode.Year -> DiaryYearStatsContent(tree, selectedDate, onDateChange)
                DiarySummaryStatsMode.Month -> DiaryMonthStatsContent(tree, selectedDate, onDateChange)
                DiarySummaryStatsMode.Week -> DiaryWeekStatsContent(tree, selectedDate, onDateChange)
            }
        }
    }
}

@Composable
private fun DiarySummaryStatsModeSelector(
    selectedMode: DiarySummaryStatsMode,
    onModeChange: (DiarySummaryStatsMode) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DiarySummaryStatsMode.values().forEach { mode ->
            if (mode == selectedMode) {
                Button(onClick = { onModeChange(mode) }, modifier = Modifier.weight(1f)) {
                    Text(mode.label)
                }
            } else {
                OutlinedButton(onClick = { onModeChange(mode) }, modifier = Modifier.weight(1f)) {
                    Text(mode.label)
                }
            }
        }
    }
}

@Composable
private fun DiaryYearStatsContent(
    tree: SummaryTree,
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
) {
    val year = selectedDate.year
    SummaryStatsPeriodHeader(
        title = "${year}年",
        previousLabel = "上一年",
        nextLabel = "下一年",
        onPrevious = { onDateChange(selectedDate.minusYears(1)) },
        onNext = { onDateChange(selectedDate.plusYears(1)) },
    )
    SummaryWordLineChartSection(
        title = "字数折线",
        points = monthlyWordCountsForYear(tree, year),
        chartWidth = 720.dp,
        horizontallyScrollable = true,
    )
}

@Composable
private fun DiaryMonthStatsContent(
    tree: SummaryTree,
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
) {
    val month = YearMonth.from(selectedDate)
    SummaryStatsPeriodHeader(
        title = "${month.year}年${month.monthValue}月",
        previousLabel = "上个月",
        nextLabel = "下个月",
        onPrevious = { onDateChange(selectedDate.plusMonthsClamped(-1)) },
        onNext = { onDateChange(selectedDate.plusMonthsClamped(1)) },
    )
    SummaryWordLineChartSection(
        title = "字数折线",
        points = weeklyWordCountsForMonth(tree, month.year, month.monthValue),
    )
}

@Composable
private fun DiaryWeekStatsContent(
    tree: SummaryTree,
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
) {
    val key = WeekKey.from(selectedDate)
    val dates = monthLocalWeekDates(key.year, key.month, key.weekIndex)
    val document = findWeekSummaryDocument(tree, key)
    val dayCounts = document?.let(::weekDayWordCounts).orEmpty()
    val totalWordCount = document?.let(::weekWordCount) ?: 0
    val title = if (dates.isEmpty()) {
        "${key.year}年${key.month}月第${key.weekIndex}周"
    } else {
        "${dates.first().monthValue}月${dates.first().dayOfMonth}日 - ${dates.last().monthValue}月${dates.last().dayOfMonth}日"
    }

    SummaryStatsPeriodHeader(
        title = title,
        previousLabel = "上一周",
        nextLabel = "下一周",
        onPrevious = { onDateChange(previousDiarySummaryWeekDate(selectedDate)) },
        onNext = { onDateChange(nextDiarySummaryWeekDate(selectedDate)) },
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("本周字数 $totalWordCount", fontWeight = FontWeight.SemiBold)
        if (dayCounts.isEmpty()) {
            Text(
                "暂无周记内容",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
            )
        } else {
            dayCounts.forEach { day ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(day.label)
                    Text("${day.wordCount}字")
                }
            }
        }
    }
}

@Composable
private fun SummaryStatsPeriodHeader(
    title: String,
    previousLabel: String,
    nextLabel: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onPrevious) {
            Text(previousLabel)
        }
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        TextButton(onClick = onNext) {
            Text(nextLabel)
        }
    }
}

@Composable
private fun SummaryWordLineChartSection(
    title: String,
    points: List<SummaryWordPoint>,
    chartWidth: Dp = 320.dp,
    horizontallyScrollable: Boolean = false,
) {
    val scrollState = rememberScrollState()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold)
        Text(
            "最高字数 ${points.maxOfOrNull { it.wordCount } ?: 0}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
        )
        val chartModifier = if (horizontallyScrollable) {
            Modifier
                .horizontalScroll(scrollState)
                .width(chartWidth)
        } else {
            Modifier.fillMaxWidth()
        }
        SummaryWordLineChart(points = points, modifier = chartModifier)
    }
}

@Composable
private fun SummaryWordLineChart(points: List<SummaryWordPoint>, modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
    val axisColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
    val labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
    val maxWordCount = points.maxOfOrNull { it.wordCount } ?: 0
    val midWordCount = maxWordCount / 2

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(
                modifier = Modifier.height(150.dp).width(58.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End,
            ) {
                Text(formatCompactCount(maxWordCount), style = MaterialTheme.typography.bodySmall, color = labelColor)
                Text(formatCompactCount(midWordCount), style = MaterialTheme.typography.bodySmall, color = labelColor)
                Text("0", style = MaterialTheme.typography.bodySmall, color = labelColor)
            }
            Canvas(modifier = Modifier.weight(1f).height(150.dp)) {
                val graphTop = 8.dp.toPx()
                val graphBottom = size.height - 12.dp.toPx()
                val graphHeight = graphBottom - graphTop
                val stepX = if (points.size <= 1) 0f else size.width / (points.size - 1).toFloat()
                val chartPoints = points.mapIndexed { index, point ->
                    val x = if (points.size <= 1) size.width / 2f else index * stepX
                    val ratio = if (maxWordCount == 0) 0f else point.wordCount.toFloat() / maxWordCount.toFloat()
                    val y = graphBottom - graphHeight * ratio
                    Offset(x, y)
                }

                listOf(graphTop, graphTop + graphHeight / 2f, graphBottom).forEach { y ->
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                }
                drawLine(axisColor, Offset(0f, graphTop), Offset(0f, graphBottom), strokeWidth = 1.dp.toPx())
                drawLine(axisColor, Offset(0f, graphBottom), Offset(size.width, graphBottom), strokeWidth = 1.dp.toPx())
                chartPoints.zipWithNext().forEach { (start, end) ->
                    drawLine(lineColor, start, end, strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                }
                chartPoints.forEach { point ->
                    drawCircle(lineColor, radius = 4.dp.toPx(), center = point)
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 66.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            points.forEach { point ->
                Text(point.label, style = MaterialTheme.typography.bodySmall, color = labelColor)
            }
        }
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

private fun findWeekSummaryDocument(tree: SummaryTree, key: WeekKey): DiaryDocument? {
    return tree.years
        .firstOrNull { it.year == key.year }
        ?.months
        ?.firstOrNull { it.month == key.month }
        ?.weeks
        ?.firstOrNull { it.weekIndex == key.weekIndex }
        ?.document
}

private fun previousDiarySummaryWeekDate(date: LocalDate): LocalDate {
    val key = WeekKey.from(date)
    return monthLocalWeekDates(key.year, key.month, key.weekIndex).firstOrNull()?.minusDays(1)
        ?: date.minusDays(7)
}

private fun nextDiarySummaryWeekDate(date: LocalDate): LocalDate {
    val key = WeekKey.from(date)
    return monthLocalWeekDates(key.year, key.month, key.weekIndex).lastOrNull()?.plusDays(1)
        ?: date.plusDays(7)
}

private fun LocalDate.plusMonthsClamped(delta: Long): LocalDate {
    val nextMonth = YearMonth.from(this).plusMonths(delta)
    val day = dayOfMonth.coerceAtMost(nextMonth.lengthOfMonth())
    return nextMonth.atDay(day)
}

private fun formatCompactCount(count: Int): String {
    return if (count >= 10000) {
        "${trimCount(count / 10000.0)}万"
    } else {
        count.toString()
    }
}

private fun trimCount(value: Double): String {
    val rounded = String.format(java.util.Locale.US, "%.1f", value)
    return rounded.removeSuffix(".0")
}
