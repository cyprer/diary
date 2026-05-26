package com.cypress.diary.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cypress.diary.accounting.AccountingCategoryTotal
import com.cypress.diary.accounting.AccountingDayTotal
import com.cypress.diary.accounting.AccountingMonthTotal
import com.cypress.diary.accounting.categoryTotals
import com.cypress.diary.accounting.dailyTotalsForDates
import com.cypress.diary.accounting.formatAmountCents
import com.cypress.diary.accounting.monthlySummary
import com.cypress.diary.accounting.monthlyTotalsForYear
import com.cypress.diary.accounting.recordsForMonth
import com.cypress.diary.accounting.recordsForWeek
import com.cypress.diary.accounting.recordsForYear
import com.cypress.diary.accounting.summaryForRecords
import com.cypress.diary.accounting.yearlySummary
import com.cypress.diary.model.accounting.AccountingRecord
import com.cypress.diary.model.accounting.AccountingRecordType
import com.cypress.diary.ui.components.RefreshableScreen
import java.time.LocalDate
import java.time.YearMonth

enum class AccountingStatsMode(
    val label: String,
) {
    Week("周账"),
    Month("月账"),
    Year("年账"),
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
    val dates = weekDates(selectedDate)
    val weeklyRecords = recordsForWeek(records, selectedDate)
    val summary = summaryForRecords(weeklyRecords)

    PeriodHeader(
        title = "${dates.first().monthValue}月${dates.first().dayOfMonth}日 - ${dates.last().monthValue}月${dates.last().dayOfMonth}日",
        previousDescription = "上一周",
        nextDescription = "下一周",
        onPrevious = { onDateChange(selectedDate.minusWeeks(1)) },
        onNext = { onDateChange(selectedDate.plusWeeks(1)) },
    )
    SummaryCard("周账", summary.incomeCents, summary.expenseCents, summary.balanceCents)
    DayTrendSection("每日趋势", dailyTotalsForDates(weeklyRecords, dates))
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
    val dates = (1..month.lengthOfMonth()).map { month.atDay(it) }
    val monthlyRecords = recordsForMonth(records, month)
    val summary = monthlySummary(records, month)

    PeriodHeader(
        title = "${month.year}年${month.monthValue}月",
        previousDescription = "上个月",
        nextDescription = "下个月",
        onPrevious = { onDateChange(selectedDate.plusMonthsClamped(-1)) },
        onNext = { onDateChange(selectedDate.plusMonthsClamped(1)) },
    )
    SummaryCard("月账", summary.incomeCents, summary.expenseCents, summary.balanceCents)
    DayTrendSection("每日趋势", dailyTotalsForDates(monthlyRecords, dates))
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
    YearMonthTrendSection(monthlyTotalsForYear(records, selectedYear))
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
private fun DayTrendSection(title: String, totals: List<AccountingDayTotal>) {
    val maxExpense = totals.maxOfOrNull { it.expenseCents } ?: 0L
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            totals.forEach { total ->
                TrendRow(
                    label = "${total.date.monthValue}/${total.date.dayOfMonth}",
                    incomeCents = total.incomeCents,
                    expenseCents = total.expenseCents,
                    balanceCents = total.balanceCents,
                    maxExpense = maxExpense,
                )
            }
        }
    }
}

@Composable
private fun YearMonthTrendSection(totals: List<AccountingMonthTotal>) {
    val maxExpense = totals.maxOfOrNull { it.expenseCents } ?: 0L
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("月度趋势", fontWeight = FontWeight.SemiBold)
            totals.forEach { total ->
                TrendRow(
                    label = "${total.month.monthValue}月",
                    incomeCents = total.incomeCents,
                    expenseCents = total.expenseCents,
                    balanceCents = total.balanceCents,
                    maxExpense = maxExpense,
                )
            }
        }
    }
}

@Composable
private fun TrendRow(
    label: String,
    incomeCents: Long,
    expenseCents: Long,
    balanceCents: Long,
    maxExpense: Long,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontWeight = FontWeight.SemiBold)
            Text("收 ¥${formatAmountCents(incomeCents)} / 支 ¥${formatAmountCents(expenseCents)}", style = MaterialTheme.typography.bodySmall)
        }
        LinearProgressIndicator(
            progress = { if (maxExpense == 0L) 0f else expenseCents.toFloat() / maxExpense.toFloat() },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            "结余 ¥${formatAmountCents(balanceCents)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
        )
    }
}

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

private fun weekDates(date: LocalDate): List<LocalDate> {
    val start = date.minusDays((date.dayOfWeek.value % 7).toLong())
    return (0..6).map { start.plusDays(it.toLong()) }
}

private fun LocalDate.plusMonthsClamped(delta: Long): LocalDate {
    val nextMonth = YearMonth.from(this).plusMonths(delta)
    val day = dayOfMonth.coerceAtMost(nextMonth.lengthOfMonth())
    return nextMonth.atDay(day)
}
