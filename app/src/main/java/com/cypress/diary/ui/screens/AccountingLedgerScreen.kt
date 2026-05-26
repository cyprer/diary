package com.cypress.diary.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cypress.diary.accounting.formatAmountCents
import com.cypress.diary.accounting.monthlySummary
import com.cypress.diary.accounting.recordsForMonth
import com.cypress.diary.accounting.sortRecordsForLedger
import com.cypress.diary.model.accounting.AccountingRecord
import com.cypress.diary.model.accounting.AccountingRecordType
import com.cypress.diary.ui.calendar.CalendarModeTabs
import com.cypress.diary.ui.calendar.CalendarMonthPicker
import com.cypress.diary.ui.calendar.CalendarWeekPicker
import com.cypress.diary.ui.calendar.CalendarYearPicker
import com.cypress.diary.ui.calendar.DiaryCalendarMode
import com.cypress.diary.ui.components.RefreshableScreen
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun AccountingLedgerScreen(
    records: List<AccountingRecord>,
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    calendarMode: DiaryCalendarMode,
    onCalendarModeChange: (DiaryCalendarMode) -> Unit,
    onRecordSelected: (AccountingRecord) -> Unit,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedMonth = YearMonth.from(selectedDate)
    val dayRecords = records.filter { it.date == selectedDate }
    val summary = monthlySummary(records, selectedMonth)

    RefreshableScreen(
        refreshing = refreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CalendarModeTabs(
            selectedMode = calendarMode,
            onModeSelected = onCalendarModeChange,
        )

        AccountingCalendarSwitcher(
            mode = calendarMode,
            date = selectedDate,
            onDateChange = onDateChange,
            onCalendarModeChange = onCalendarModeChange,
        )

        if (calendarMode != DiaryCalendarMode.Year) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("本月概览", fontWeight = FontWeight.SemiBold)
                    Text("收入 ¥${formatAmountCents(summary.incomeCents)}")
                    Text("支出 ¥${formatAmountCents(summary.expenseCents)}")
                    Text("结余 ¥${formatAmountCents(summary.balanceCents)}")
                }
            }

            Text(
                text = "${selectedDate.monthValue}月${selectedDate.dayOfMonth}日账单",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            if (dayRecords.isEmpty()) {
                Text(
                    text = "这天还没有账目",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                )
            } else {
                sortRecordsForLedger(dayRecords).forEach { record ->
                    AccountingRecordRow(record = record, onClick = { onRecordSelected(record) })
                }
            }
        }
    }
}

@Composable
private fun AccountingCalendarSwitcher(
    mode: DiaryCalendarMode,
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    onCalendarModeChange: (DiaryCalendarMode) -> Unit,
) {
    when (mode) {
        DiaryCalendarMode.Year -> CalendarYearPicker(
            selectedDate = date,
            onYearChanged = onDateChange,
            onMonthSelected = { selected ->
                onDateChange(selected)
                onCalendarModeChange(DiaryCalendarMode.Month)
            },
        )

        DiaryCalendarMode.Month -> CalendarMonthPicker(
            selectedDate = date,
            onDateSelected = onDateChange,
        )

        DiaryCalendarMode.Week -> CalendarWeekPicker(
            selectedDate = date,
            onDateSelected = onDateChange,
        )
    }
}

@Composable
private fun AccountingRecordRow(
    record: AccountingRecord,
    onClick: () -> Unit,
) {
    val sign = if (record.type == AccountingRecordType.Expense) "-" else "+"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(record.category, fontWeight = FontWeight.SemiBold)
                if (record.note.isNotBlank()) {
                    Text(
                        text = record.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                    )
                }
            }
            Text(
                text = "$sign¥${formatAmountCents(record.amountCents)}",
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
