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
import com.cypress.diary.accounting.AccountingMonthTotal
import com.cypress.diary.accounting.categoryTotals
import com.cypress.diary.accounting.formatAmountCents
import com.cypress.diary.accounting.monthlySummary
import com.cypress.diary.accounting.monthlyTotalsForYear
import com.cypress.diary.accounting.recordsForMonth
import com.cypress.diary.accounting.recordsForYear
import com.cypress.diary.accounting.yearlySummary
import com.cypress.diary.model.accounting.AccountingRecord
import com.cypress.diary.model.accounting.AccountingRecordType
import com.cypress.diary.ui.components.RefreshableScreen
import java.time.YearMonth
import java.time.format.DateTimeFormatter

enum class AccountingStatsMode(
    val label: String,
) {
    Month("月度"),
    Year("年度"),
}

@Composable
fun AccountingStatsScreen(
    records: List<AccountingRecord>,
    selectedMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    selectedYear: Int,
    onYearChange: (Int) -> Unit,
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
            AccountingStatsMode.Month -> AccountingMonthlyStatsContent(
                records = records,
                selectedMonth = selectedMonth,
                onMonthChange = onMonthChange,
            )

            AccountingStatsMode.Year -> AccountingYearlyStatsContent(
                records = records,
                selectedYear = selectedYear,
                onYearChange = onYearChange,
            )
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
                Button(
                    onClick = { onModeChange(option) },
                    modifier = modifier,
                ) {
                    Text(option.label)
                }
            } else {
                OutlinedButton(
                    onClick = { onModeChange(option) },
                    modifier = modifier,
                ) {
                    Text(option.label)
                }
            }
        }
    }
}

@Composable
private fun AccountingMonthlyStatsContent(
    records: List<AccountingRecord>,
    selectedMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
) {
    val monthlyRecords = recordsForMonth(records, selectedMonth)
    val summary = monthlySummary(records, selectedMonth)
    val expenseTotals = categoryTotals(monthlyRecords, AccountingRecordType.Expense)
    val incomeTotals = categoryTotals(monthlyRecords, AccountingRecordType.Income)

    PeriodHeader(
        title = selectedMonth.format(DateTimeFormatter.ofPattern("yyyy年M月")),
        previousDescription = "上个月",
        nextDescription = "下个月",
        onPrevious = { onMonthChange(selectedMonth.minusMonths(1)) },
        onNext = { onMonthChange(selectedMonth.plusMonths(1)) },
    )

    SummaryCard(
        title = "月度统计",
        incomeCents = summary.incomeCents,
        expenseCents = summary.expenseCents,
        balanceCents = summary.balanceCents,
    )

    CategoryTotalSection("支出分类", expenseTotals)
    CategoryTotalSection("收入分类", incomeTotals)
}

@Composable
private fun AccountingYearlyStatsContent(
    records: List<AccountingRecord>,
    selectedYear: Int,
    onYearChange: (Int) -> Unit,
) {
    val yearlyRecords = recordsForYear(records, selectedYear)
    val summary = yearlySummary(records, selectedYear)
    val monthlyTotals = monthlyTotalsForYear(records, selectedYear)
    val expenseTotals = categoryTotals(yearlyRecords, AccountingRecordType.Expense)
    val incomeTotals = categoryTotals(yearlyRecords, AccountingRecordType.Income)

    PeriodHeader(
        title = "${selectedYear}年",
        previousDescription = "上一年",
        nextDescription = "下一年",
        onPrevious = { onYearChange(selectedYear - 1) },
        onNext = { onYearChange(selectedYear + 1) },
    )

    SummaryCard(
        title = "年度总结",
        incomeCents = summary.incomeCents,
        expenseCents = summary.expenseCents,
        balanceCents = summary.balanceCents,
    )

    YearMonthTrendSection(monthlyTotals)
    CategoryTotalSection("年度支出分类", expenseTotals)
    CategoryTotalSection("年度收入分类", incomeTotals)
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
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = nextDescription)
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    incomeCents: Long,
    expenseCents: Long,
    balanceCents: Long,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text("收入 ¥${formatAmountCents(incomeCents)}")
            Text("支出 ¥${formatAmountCents(expenseCents)}")
            Text("结余 ¥${formatAmountCents(balanceCents)}")
        }
    }
}

@Composable
private fun YearMonthTrendSection(
    totals: List<AccountingMonthTotal>,
) {
    val maxExpense = totals.maxOfOrNull { it.expenseCents } ?: 0L
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("月度趋势", fontWeight = FontWeight.SemiBold)
            totals.forEach { total ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("${total.month.monthValue}月", fontWeight = FontWeight.SemiBold)
                        Text(
                            "收 ¥${formatAmountCents(total.incomeCents)} / 支 ¥${formatAmountCents(total.expenseCents)}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    LinearProgressIndicator(
                        progress = {
                            if (maxExpense == 0L) {
                                0f
                            } else {
                                total.expenseCents.toFloat() / maxExpense.toFloat()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        "结余 ¥${formatAmountCents(total.balanceCents)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryTotalSection(
    title: String,
    totals: List<AccountingCategoryTotal>,
) {
    val max = totals.maxOfOrNull { it.amountCents } ?: 0L
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(title, fontWeight = FontWeight.SemiBold)
            if (totals.isEmpty()) {
                Text(
                    text = "暂无数据",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                )
            } else {
                totals.forEach { total ->
                    Text("${total.category} ¥${formatAmountCents(total.amountCents)}")
                    LinearProgressIndicator(
                        progress = {
                            if (max == 0L) {
                                0f
                            } else {
                                total.amountCents.toFloat() / max.toFloat()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
