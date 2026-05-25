package com.cypress.diary.accounting

import com.cypress.diary.model.accounting.AccountingRecord
import com.cypress.diary.model.accounting.AccountingRecordType
import java.time.YearMonth

data class AccountingMonthSummary(
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

fun monthlySummary(records: List<AccountingRecord>, month: YearMonth): AccountingMonthSummary {
    val monthlyRecords = recordsForMonth(records, month)
    val income = monthlyRecords.filter { it.type == AccountingRecordType.Income }.sumOf { it.amountCents }
    val expense = monthlyRecords.filter { it.type == AccountingRecordType.Expense }.sumOf { it.amountCents }
    return AccountingMonthSummary(
        incomeCents = income,
        expenseCents = expense,
        balanceCents = income - expense,
    )
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
