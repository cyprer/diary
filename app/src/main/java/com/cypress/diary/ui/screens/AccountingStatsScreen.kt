package com.cypress.diary.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cypress.diary.accounting.AccountingCategoryTotal
import com.cypress.diary.accounting.AccountingMonthTotal
import com.cypress.diary.accounting.AccountingWeekTotal
import com.cypress.diary.accounting.accountingWeekRange
import com.cypress.diary.accounting.categoryTotals
import com.cypress.diary.accounting.formatAmountCents
import com.cypress.diary.accounting.monthlySummary
import com.cypress.diary.accounting.monthlyTotalsForYear
import com.cypress.diary.accounting.recordsForMonth
import com.cypress.diary.accounting.recordsForWeek
import com.cypress.diary.accounting.recordsForYear
import com.cypress.diary.accounting.sortRecordsForLedger
import com.cypress.diary.accounting.summaryForRecords
import com.cypress.diary.accounting.weeklyTotalsForMonth
import com.cypress.diary.accounting.yearlySummary
import com.cypress.diary.model.accounting.AccountingRecord
import com.cypress.diary.model.accounting.AccountingRecordType
import com.cypress.diary.ui.components.RefreshableScreen
import java.time.LocalDate
import java.time.YearMonth

enum class AccountingStatsMode(
    val label: String,
) {
    Year("年账"),
    Month("月账"),
    Week("周账"),
}

@Composable
fun AccountingStatsScreen(
    records: List<AccountingRecord>,
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    mode: AccountingStatsMode,
    onModeChange: (AccountingStatsMode) -> Unit,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RefreshableScreen(
        refreshing = refreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AccountingStatsModeSelector(mode = mode, onModeChange = onModeChange)
        when (mode) {
            AccountingStatsMode.Week -> AccountingWeeklyStatsContent(records, selectedDate, onDateChange)
            AccountingStatsMode.Month -> AccountingMonthlyStatsContent(records, selectedDate, onDateChange)
            AccountingStatsMode.Year -> AccountingYearlyStatsContent(records, selectedDate, onDateChange)
        }
    }
}

@Composable
private fun AccountingStatsModeSelector(
    mode: AccountingStatsMode,
    onModeChange: (AccountingStatsMode) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AccountingStatsMode.values().forEach { option ->
            val modifier = Modifier.weight(1f)
            if (option == mode) {
                Button(onClick = { onModeChange(option) }, modifier = modifier) {
                    Text(option.label)
                }
            } else {
                OutlinedButton(onClick = { onModeChange(option) }, modifier = modifier) {
                    Text(option.label)
                }
            }
        }
    }
}

@Composable
private fun AccountingWeeklyStatsContent(
    records: List<AccountingRecord>,
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
) {
    val (startDate, endDate) = accountingWeekRange(selectedDate)
    val weeklyRecords = recordsForWeek(records, selectedDate)
    val summary = summaryForRecords(weeklyRecords)

    PeriodHeader(
        title = "${startDate.monthValue}月${startDate.dayOfMonth}日 - ${endDate.monthValue}月${endDate.dayOfMonth}日",
        previousDescription = "上一周",
        nextDescription = "下一周",
        onPrevious = { onDateChange(previousAccountingWeekDate(selectedDate)) },
        onNext = { onDateChange(nextAccountingWeekDate(selectedDate)) },
    )
    SummaryCard("周账", summary.incomeCents, summary.expenseCents, summary.balanceCents)
    AccountingRecordListSection("账单记录", weeklyRecords)
    CategoryTotalSection("本周支出分类", categoryTotals(weeklyRecords, AccountingRecordType.Expense))
    CategoryTotalSection("本周收入分类", categoryTotals(weeklyRecords, AccountingRecordType.Income))
}

@Composable
private fun AccountingMonthlyStatsContent(
    records: List<AccountingRecord>,
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
) {
    val month = YearMonth.from(selectedDate)
    val monthlyRecords = recordsForMonth(records, month)
    val summary = monthlySummary(records, month)
    val weeklyTotals = weeklyTotalsForMonth(records, month)

    PeriodHeader(
        title = "${month.year}年${month.monthValue}月",
        previousDescription = "上个月",
        nextDescription = "下个月",
        onPrevious = { onDateChange(selectedDate.plusMonthsClamped(-1)) },
        onNext = { onDateChange(selectedDate.plusMonthsClamped(1)) },
    )
    SummaryCard("月账", summary.incomeCents, summary.expenseCents, summary.balanceCents)
    WeeklyExpenseLineChartSection(weeklyTotals)
    CategoryTotalSection("本月支出分类", categoryTotals(monthlyRecords, AccountingRecordType.Expense))
    CategoryTotalSection("本月收入分类", categoryTotals(monthlyRecords, AccountingRecordType.Income))
}

@Composable
private fun AccountingYearlyStatsContent(
    records: List<AccountingRecord>,
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
) {
    val selectedYear = selectedDate.year
    val yearlyRecords = recordsForYear(records, selectedYear)
    val summary = yearlySummary(records, selectedYear)

    PeriodHeader(
        title = "${selectedYear}年",
        previousDescription = "上一年",
        nextDescription = "下一年",
        onPrevious = { onDateChange(selectedDate.minusYears(1)) },
        onNext = { onDateChange(selectedDate.plusYears(1)) },
    )
    SummaryCard("年账", summary.incomeCents, summary.expenseCents, summary.balanceCents)
    YearlyExpenseLineChartSection(monthlyTotalsForYear(records, selectedYear))
    CategoryTotalSection("年度支出分类", categoryTotals(yearlyRecords, AccountingRecordType.Expense))
    CategoryTotalSection("年度收入分类", categoryTotals(yearlyRecords, AccountingRecordType.Income))
}

@Composable
private fun PeriodHeader(
    title: String,
    previousDescription: String,
    nextDescription: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = previousDescription)
        }
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = nextDescription)
        }
    }
}

@Composable
private fun SummaryCard(title: String, incomeCents: Long, expenseCents: Long, balanceCents: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text("收入 ¥${formatAmountCents(incomeCents)}")
            Text("支出 ¥${formatAmountCents(expenseCents)}")
            Text("结余 ¥${formatAmountCents(balanceCents)}")
        }
    }
}

@Composable
private fun AccountingRecordListSection(title: String, records: List<AccountingRecord>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            if (records.isEmpty()) {
                Text("暂无账单记录", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f))
            } else {
                sortRecordsForLedger(records).forEach { record ->
                    AccountingStatsRecordRow(record)
                }
            }
        }
    }
}

@Composable
private fun AccountingStatsRecordRow(record: AccountingRecord) {
    val typeLabel = if (record.type == AccountingRecordType.Income) "收入" else "支出"
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${record.date.monthValue}/${record.date.dayOfMonth} $typeLabel · ${record.category}", fontWeight = FontWeight.SemiBold)
            Text("¥${formatAmountCents(record.amountCents)}")
        }
        if (record.note.isNotBlank()) {
            Text(
                record.note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
            )
        }
    }
}

@Composable
private fun WeeklyExpenseLineChartSection(totals: List<AccountingWeekTotal>) {
    ExpenseLineChartSection(
        title = "支出折线",
        points = totals.map { total -> ExpenseChartPoint("第${total.weekNumber}周", total.expenseCents) },
    )
}

@Composable
private fun YearlyExpenseLineChartSection(totals: List<AccountingMonthTotal>) {
    ExpenseLineChartSection(
        title = "支出折线",
        points = totals.map { total -> ExpenseChartPoint("${total.month.monthValue}月", total.expenseCents) },
        chartWidth = 720.dp,
        horizontallyScrollable = true,
    )
}

@Composable
private fun ExpenseLineChartSection(
    title: String,
    points: List<ExpenseChartPoint>,
    chartWidth: Dp = 320.dp,
    horizontallyScrollable: Boolean = false,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(
                "最高支出 ¥${formatAmountCents(points.maxOfOrNull { it.expenseCents } ?: 0L)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
            )
            ExpenseLineChart(
                points = points,
                chartWidth = chartWidth,
                horizontallyScrollable = horizontallyScrollable,
            )
        }
    }
}

@Composable
private fun ExpenseLineChart(
    points: List<ExpenseChartPoint>,
    chartWidth: Dp,
    horizontallyScrollable: Boolean,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
    val axisColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
    val labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
    val maxExpense = points.maxOfOrNull { it.expenseCents } ?: 0L
    val midExpense = maxExpense / 2

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(
                modifier = Modifier.height(150.dp).width(58.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End,
            ) {
                Text(formatCompactAmount(maxExpense), style = MaterialTheme.typography.bodySmall, color = labelColor)
                Text(formatCompactAmount(midExpense), style = MaterialTheme.typography.bodySmall, color = labelColor)
                Text("0", style = MaterialTheme.typography.bodySmall, color = labelColor)
            }
            val plotModifier = if (horizontallyScrollable) {
                Modifier
                    .horizontalScroll(scrollState)
                    .width(chartWidth)
            } else {
                Modifier.weight(1f)
            }
            Column(modifier = plotModifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                    val graphTop = 8.dp.toPx()
                    val graphBottom = size.height - 12.dp.toPx()
                    val graphHeight = graphBottom - graphTop
                    val stepX = if (points.size <= 1) 0f else size.width / (points.size - 1).toFloat()
                    val chartPoints = points.mapIndexed { index, point ->
                        val x = if (points.size <= 1) size.width / 2f else index * stepX
                        val ratio = if (maxExpense == 0L) 0f else point.expenseCents.toFloat() / maxExpense.toFloat()
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    points.forEach { point ->
                        Text(point.label, style = MaterialTheme.typography.bodySmall, color = labelColor)
                    }
                }
            }
        }
    }
}

private data class ExpenseChartPoint(
    val label: String,
    val expenseCents: Long,
)

@Composable
private fun CategoryTotalSection(title: String, totals: List<AccountingCategoryTotal>) {
    val max = totals.maxOfOrNull { it.amountCents } ?: 0L
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            if (totals.isEmpty()) {
                Text("暂无数据", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f))
            } else {
                totals.forEach { total ->
                    Text("${total.category} ¥${formatAmountCents(total.amountCents)}")
                    LinearProgressIndicator(
                        progress = { if (max == 0L) 0f else total.amountCents.toFloat() / max.toFloat() },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

private fun LocalDate.plusMonthsClamped(delta: Long): LocalDate {
    val nextMonth = YearMonth.from(this).plusMonths(delta)
    val day = dayOfMonth.coerceAtMost(nextMonth.lengthOfMonth())
    return nextMonth.atDay(day)
}

private fun previousAccountingWeekDate(date: LocalDate): LocalDate {
    return accountingWeekRange(date).first.minusDays(1)
}

private fun nextAccountingWeekDate(date: LocalDate): LocalDate {
    return accountingWeekRange(date).second.plusDays(1)
}

private fun formatCompactAmount(cents: Long): String {
    val amount = cents / 100.0
    return when {
        cents == 0L -> "0"
        amount >= 10000 -> "${trimAmount(amount / 10000)}万"
        else -> trimAmount(amount)
    }
}

private fun trimAmount(amount: Double): String {
    val rounded = String.format(java.util.Locale.US, "%.1f", amount)
    return rounded.removeSuffix(".0")
}
