package com.cypress.diary.accounting

import com.cypress.diary.model.accounting.AccountingRecord
import com.cypress.diary.model.accounting.AccountingCategory
import com.cypress.diary.model.accounting.AccountingRecordType
import java.time.LocalDate
import java.time.YearMonth

data class AccountingMonthSummary(
    val incomeCents: Long,
    val expenseCents: Long,
    val balanceCents: Long,
)

data class AccountingMonthTotal(
    val month: YearMonth,
    val incomeCents: Long,
    val expenseCents: Long,
    val balanceCents: Long,
)

data class AccountingWeekTotal(
    val weekNumber: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val incomeCents: Long,
    val expenseCents: Long,
    val balanceCents: Long,
)

data class AccountingDayTotal(
    val date: LocalDate,
    val incomeCents: Long,
    val expenseCents: Long,
    val balanceCents: Long,
)

data class AccountingCategoryTotal(
    val category: String,
    val amountCents: Long,
)

fun recordsForMonth(records: List<AccountingRecord>, month: YearMonth): List<AccountingRecord> {
    return records.filter { YearMonth.from(it.date) == month }
}

fun recordsForWeek(records: List<AccountingRecord>, selectedDate: LocalDate): List<AccountingRecord> {
    val (start, end) = accountingWeekRange(selectedDate)
    return records.filter { record -> !record.date.isBefore(start) && !record.date.isAfter(end) }
}

fun accountingWeekRange(selectedDate: LocalDate): Pair<LocalDate, LocalDate> {
    val month = YearMonth.from(selectedDate)
    val weekNumber = accountingWeekNumber(selectedDate)
    val startDay = when (weekNumber) {
        1 -> 1
        2 -> 8
        3 -> 15
        else -> 22
    }
    val endDay = when (weekNumber) {
        1 -> 7
        2 -> 14
        3 -> 21
        else -> month.lengthOfMonth()
    }
    return month.atDay(startDay) to month.atDay(endDay)
}

fun accountingWeekNumber(date: LocalDate): Int {
    return when (date.dayOfMonth) {
        in 1..7 -> 1
        in 8..14 -> 2
        in 15..21 -> 3
        else -> 4
    }
}

fun summaryForRecords(records: List<AccountingRecord>): AccountingMonthSummary {
    val income = records.filter { it.type == AccountingRecordType.Income }.sumOf { it.amountCents }
    val expense = records.filter { it.type == AccountingRecordType.Expense }.sumOf { it.amountCents }
    return AccountingMonthSummary(
        incomeCents = income,
        expenseCents = expense,
        balanceCents = income - expense,
    )
}

fun monthlySummary(records: List<AccountingRecord>, month: YearMonth): AccountingMonthSummary {
    return summaryForRecords(recordsForMonth(records, month))
}

fun recordsForYear(records: List<AccountingRecord>, year: Int): List<AccountingRecord> {
    return records.filter { it.date.year == year }
}

fun yearlySummary(records: List<AccountingRecord>, year: Int): AccountingMonthSummary {
    return summaryForRecords(recordsForYear(records, year))
}

fun dailyTotalsForDates(
    records: List<AccountingRecord>,
    dates: List<LocalDate>,
): List<AccountingDayTotal> {
    return dates.map { date ->
        val summary = summaryForRecords(records.filter { it.date == date })
        AccountingDayTotal(
            date = date,
            incomeCents = summary.incomeCents,
            expenseCents = summary.expenseCents,
            balanceCents = summary.balanceCents,
        )
    }
}

fun monthlyTotalsForYear(records: List<AccountingRecord>, year: Int): List<AccountingMonthTotal> {
    return (1..12).map { monthValue ->
        val month = YearMonth.of(year, monthValue)
        val summary = monthlySummary(records, month)
        AccountingMonthTotal(
            month = month,
            incomeCents = summary.incomeCents,
            expenseCents = summary.expenseCents,
            balanceCents = summary.balanceCents,
        )
    }
}

fun weeklyTotalsForMonth(records: List<AccountingRecord>, month: YearMonth): List<AccountingWeekTotal> {
    return (1..4).map { weekNumber ->
        val startDay = when (weekNumber) {
            1 -> 1
            2 -> 8
            3 -> 15
            else -> 22
        }
        val endDay = when (weekNumber) {
            1 -> 7
            2 -> 14
            3 -> 21
            else -> month.lengthOfMonth()
        }
        val startDate = month.atDay(startDay)
        val endDate = month.atDay(endDay)
        val weeklyRecords = records.filter { record ->
            !record.date.isBefore(startDate) && !record.date.isAfter(endDate)
        }
        val summary = summaryForRecords(weeklyRecords)
        AccountingWeekTotal(
            weekNumber = weekNumber,
            startDate = startDate,
            endDate = endDate,
            incomeCents = summary.incomeCents,
            expenseCents = summary.expenseCents,
            balanceCents = summary.balanceCents,
        )
    }
}

fun categoryTotals(
    records: List<AccountingRecord>,
    type: AccountingRecordType,
): List<AccountingCategoryTotal> {
    return records
        .filter { it.type == type }
        .groupBy { it.category }
        .map { (category, categoryRecords) ->
            AccountingCategoryTotal(
                category = category,
                amountCents = categoryRecords.sumOf { it.amountCents },
            )
        }
        .sortedWith(compareByDescending<AccountingCategoryTotal> { it.amountCents }.thenBy { it.category })
}

fun sortRecordsForLedger(records: List<AccountingRecord>): List<AccountingRecord> {
    return records.sortedWith(compareByDescending<AccountingRecord> { it.date }.thenByDescending { it.createdAt })
}

fun replaceAccountingRecords(imported: List<AccountingRecord>): List<AccountingRecord> {
    return sortRecordsForLedger(imported)
}

fun mergeAccountingRecords(
    local: List<AccountingRecord>,
    imported: List<AccountingRecord>,
): List<AccountingRecord> {
    val importedIds = imported.map { it.id }.toSet()
    return sortRecordsForLedger(local.filterNot { it.id in importedIds } + imported)
}

fun mergeAccountingCategories(
    local: List<AccountingCategory>,
    imported: List<AccountingCategory>,
): List<AccountingCategory> {
    val seen = mutableSetOf<Pair<AccountingRecordType, String>>()
    return (local + imported).filter { category ->
        seen.add(category.type to category.label)
    }
}
