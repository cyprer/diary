package com.cypress.diary.accounting

import com.cypress.diary.model.accounting.AccountingRecord
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
