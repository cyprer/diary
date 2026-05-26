package com.cypress.diary.accounting

import com.cypress.diary.model.accounting.AccountingRecord
import com.cypress.diary.model.accounting.AccountingCategory
import com.cypress.diary.model.accounting.AccountingRecordType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class AccountingSummaryTest {
    @Test
    fun filtersRecordsByMonth() {
        val records = listOf(
            record("a", AccountingRecordType.Expense, 100, "food", LocalDate.of(2026, 5, 1)),
            record("b", AccountingRecordType.Income, 200, "salary", LocalDate.of(2026, 6, 1)),
        )

        assertEquals(listOf("a"), recordsForMonth(records, YearMonth.of(2026, 5)).map { it.id })
    }

    @Test
    fun filtersRecordsByMonthBoundAccountingWeek() {
        val records = listOf(
            record("previous", AccountingRecordType.Expense, 100, "food", LocalDate.of(2026, 5, 21)),
            record("first", AccountingRecordType.Expense, 100, "food", LocalDate.of(2026, 5, 22)),
            record("middle", AccountingRecordType.Income, 200, "salary", LocalDate.of(2026, 5, 26)),
            record("last", AccountingRecordType.Expense, 300, "food", LocalDate.of(2026, 5, 31)),
            record("next", AccountingRecordType.Expense, 400, "food", LocalDate.of(2026, 6, 1)),
        )

        assertEquals(listOf("first", "middle", "last"), recordsForWeek(records, LocalDate.of(2026, 5, 26)).map { it.id })
    }

    @Test
    fun calculatesMonthlyTotals() {
        val records = listOf(
            record("a", AccountingRecordType.Expense, 1000, "food", LocalDate.of(2026, 5, 1)),
            record("b", AccountingRecordType.Expense, 500, "transport", LocalDate.of(2026, 5, 2)),
            record("c", AccountingRecordType.Income, 3000, "salary", LocalDate.of(2026, 5, 3)),
        )

        val summary = monthlySummary(records, YearMonth.of(2026, 5))

        assertEquals(3000L, summary.incomeCents)
        assertEquals(1500L, summary.expenseCents)
        assertEquals(1500L, summary.balanceCents)
    }

    @Test
    fun filtersRecordsByYear() {
        val records = listOf(
            record("a", AccountingRecordType.Expense, 100, "food", LocalDate.of(2026, 1, 1)),
            record("b", AccountingRecordType.Income, 200, "salary", LocalDate.of(2025, 12, 31)),
            record("c", AccountingRecordType.Income, 300, "salary", LocalDate.of(2026, 12, 31)),
        )

        assertEquals(listOf("a", "c"), recordsForYear(records, 2026).map { it.id })
    }

    @Test
    fun calculatesSummaryForArbitraryRecords() {
        val records = listOf(
            record("expense", AccountingRecordType.Expense, 1200, "food", LocalDate.of(2026, 5, 24)),
            record("income", AccountingRecordType.Income, 3000, "salary", LocalDate.of(2026, 5, 24)),
        )

        val summary = summaryForRecords(records)

        assertEquals(3000L, summary.incomeCents)
        assertEquals(1200L, summary.expenseCents)
        assertEquals(1800L, summary.balanceCents)
    }

    @Test
    fun returnsDailyTotalsForRequestedDates() {
        val dates = listOf(
            LocalDate.of(2026, 5, 24),
            LocalDate.of(2026, 5, 25),
        )
        val records = listOf(
            record("expense", AccountingRecordType.Expense, 1200, "food", dates[0]),
            record("income", AccountingRecordType.Income, 3000, "salary", dates[0]),
        )

        val totals = dailyTotalsForDates(records, dates)

        assertEquals(dates, totals.map { it.date })
        assertEquals(3000L, totals[0].incomeCents)
        assertEquals(1200L, totals[0].expenseCents)
        assertEquals(1800L, totals[0].balanceCents)
        assertEquals(0L, totals[1].incomeCents)
        assertEquals(0L, totals[1].expenseCents)
        assertEquals(0L, totals[1].balanceCents)
    }

    @Test
    fun returnsFourWeeklyTotalsForMonth() {
        val records = listOf(
            record("week1", AccountingRecordType.Expense, 1000, "food", LocalDate.of(2026, 5, 7)),
            record("week2", AccountingRecordType.Expense, 2000, "food", LocalDate.of(2026, 5, 8)),
            record("week3", AccountingRecordType.Income, 9000, "salary", LocalDate.of(2026, 5, 21)),
            record("week4", AccountingRecordType.Expense, 4000, "rent", LocalDate.of(2026, 5, 31)),
            record("otherMonth", AccountingRecordType.Expense, 8000, "rent", LocalDate.of(2026, 6, 1)),
        )

        val totals = weeklyTotalsForMonth(records, YearMonth.of(2026, 5))

        assertEquals(listOf(1, 2, 3, 4), totals.map { it.weekNumber })
        assertEquals(
            listOf(
                LocalDate.of(2026, 5, 1) to LocalDate.of(2026, 5, 7),
                LocalDate.of(2026, 5, 8) to LocalDate.of(2026, 5, 14),
                LocalDate.of(2026, 5, 15) to LocalDate.of(2026, 5, 21),
                LocalDate.of(2026, 5, 22) to LocalDate.of(2026, 5, 31),
            ),
            totals.map { it.startDate to it.endDate },
        )
        assertEquals(listOf(1000L, 2000L, 0L, 4000L), totals.map { it.expenseCents })
        assertEquals(listOf(0L, 0L, 9000L, 0L), totals.map { it.incomeCents })
    }

    @Test
    fun calculatesYearlyTotals() {
        val records = listOf(
            record("a", AccountingRecordType.Expense, 1000, "food", LocalDate.of(2026, 1, 1)),
            record("b", AccountingRecordType.Expense, 500, "transport", LocalDate.of(2026, 12, 2)),
            record("c", AccountingRecordType.Income, 3000, "salary", LocalDate.of(2026, 5, 3)),
            record("d", AccountingRecordType.Income, 7000, "salary", LocalDate.of(2025, 5, 3)),
        )

        val summary = yearlySummary(records, 2026)

        assertEquals(3000L, summary.incomeCents)
        assertEquals(1500L, summary.expenseCents)
        assertEquals(1500L, summary.balanceCents)
    }

    @Test
    fun returnsTwelveMonthlyTotalsForYear() {
        val records = listOf(
            record("janExpense", AccountingRecordType.Expense, 1000, "food", LocalDate.of(2026, 1, 8)),
            record("janIncome", AccountingRecordType.Income, 3000, "salary", LocalDate.of(2026, 1, 9)),
            record("decExpense", AccountingRecordType.Expense, 2000, "rent", LocalDate.of(2026, 12, 10)),
        )

        val totals = monthlyTotalsForYear(records, 2026)

        assertEquals((1..12).toList(), totals.map { it.month.monthValue })
        assertEquals(3000L, totals[0].incomeCents)
        assertEquals(1000L, totals[0].expenseCents)
        assertEquals(2000L, totals[0].balanceCents)
        assertEquals(0L, totals[1].incomeCents)
        assertEquals(0L, totals[1].expenseCents)
        assertEquals(0L, totals[1].balanceCents)
        assertEquals(0L, totals[11].incomeCents)
        assertEquals(2000L, totals[11].expenseCents)
        assertEquals(-2000L, totals[11].balanceCents)
    }

    @Test
    fun groupsCategoryTotalsByTypeAndDescendingAmount() {
        val records = listOf(
            record("a", AccountingRecordType.Expense, 1000, "餐饮", LocalDate.of(2026, 5, 1)),
            record("b", AccountingRecordType.Expense, 500, "交通", LocalDate.of(2026, 5, 2)),
            record("c", AccountingRecordType.Expense, 300, "餐饮", LocalDate.of(2026, 5, 3)),
        )

        val totals = categoryTotals(records, AccountingRecordType.Expense)

        assertEquals(listOf("餐饮", "交通"), totals.map { it.category })
        assertEquals(listOf(1300L, 500L), totals.map { it.amountCents })
    }

    @Test
    fun sortsRecordsByDateThenCreatedAtDescending() {
        val older = record("older", AccountingRecordType.Expense, 100, "food", LocalDate.of(2026, 5, 2), createdAt = 1)
        val newer = record("newer", AccountingRecordType.Expense, 100, "food", LocalDate.of(2026, 5, 2), createdAt = 2)
        val latestDate = record("latestDate", AccountingRecordType.Expense, 100, "food", LocalDate.of(2026, 5, 3), createdAt = 1)

        assertEquals(listOf("latestDate", "newer", "older"), sortRecordsForLedger(listOf(older, latestDate, newer)).map { it.id })
    }

    @Test
    fun replacesAccountingRecordsWithImportedRecords() {
        val imported = listOf(
            record("imported", AccountingRecordType.Income, 100, "salary", LocalDate.of(2026, 5, 3)),
            record("older", AccountingRecordType.Expense, 100, "food", LocalDate.of(2026, 5, 2)),
        )

        assertEquals(
            listOf("imported", "older"),
            replaceAccountingRecords(imported).map { it.id },
        )
    }

    @Test
    fun mergesAccountingRecordsByIdUsingImportedRecordOnConflict() {
        val local = listOf(
            record("localOnly", AccountingRecordType.Expense, 100, "food", LocalDate.of(2026, 5, 1)),
            record("same", AccountingRecordType.Expense, 100, "old", LocalDate.of(2026, 5, 2)),
        )
        val imported = listOf(
            record("same", AccountingRecordType.Income, 900, "new", LocalDate.of(2026, 5, 3)),
            record("importedOnly", AccountingRecordType.Income, 500, "salary", LocalDate.of(2026, 5, 4)),
        )

        val merged = mergeAccountingRecords(local, imported)

        assertEquals(listOf("importedOnly", "same", "localOnly"), merged.map { it.id })
        assertEquals(AccountingRecordType.Income, merged.first { it.id == "same" }.type)
        assertEquals(900L, merged.first { it.id == "same" }.amountCents)
        assertEquals("new", merged.first { it.id == "same" }.category)
    }

    @Test
    fun mergesAccountingCategoriesByTypeAndLabel() {
        val local = listOf(
            AccountingCategory("local-coffee", "咖啡", AccountingRecordType.Expense),
            AccountingCategory("local-part-time", "兼职", AccountingRecordType.Income),
        )
        val imported = listOf(
            AccountingCategory("imported-coffee", "咖啡", AccountingRecordType.Expense),
            AccountingCategory("imported-coffee-income", "咖啡", AccountingRecordType.Income),
            AccountingCategory("imported-snack", "零食", AccountingRecordType.Expense),
        )

        val merged = mergeAccountingCategories(local, imported)

        assertEquals(
            listOf("咖啡:Expense", "兼职:Income", "咖啡:Income", "零食:Expense"),
            merged.map { "${it.label}:${it.type.name}" },
        )
        assertEquals("local-coffee", merged.first { it.label == "咖啡" && it.type == AccountingRecordType.Expense }.key)
    }

    private fun record(
        id: String,
        type: AccountingRecordType,
        amountCents: Long,
        category: String,
        date: LocalDate,
        createdAt: Long = 1,
    ): AccountingRecord {
        return AccountingRecord(
            id = id,
            type = type,
            amountCents = amountCents,
            category = category,
            date = date,
            note = "",
            createdAt = createdAt,
            updatedAt = createdAt,
        )
    }
}
