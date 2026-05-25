package com.cypress.diary.model.accounting

import java.time.LocalDate

enum class AccountingRecordType(
    val label: String,
) {
    Expense("支出"),
    Income("收入"),
}

data class AccountingCategory(
    val key: String,
    val label: String,
    val type: AccountingRecordType,
)

data class AccountingRecord(
    val id: String,
    val type: AccountingRecordType,
    val amountCents: Long,
    val category: String,
    val date: LocalDate,
    val note: String,
    val createdAt: Long,
    val updatedAt: Long,
)

val defaultAccountingCategories = listOf(
    AccountingCategory("dining", "餐饮", AccountingRecordType.Expense),
    AccountingCategory("transport", "交通", AccountingRecordType.Expense),
    AccountingCategory("shopping", "购物", AccountingRecordType.Expense),
    AccountingCategory("home", "居家", AccountingRecordType.Expense),
    AccountingCategory("entertainment", "娱乐", AccountingRecordType.Expense),
    AccountingCategory("medical", "医疗", AccountingRecordType.Expense),
    AccountingCategory("learning", "学习", AccountingRecordType.Expense),
    AccountingCategory("expense_other", "其他", AccountingRecordType.Expense),
    AccountingCategory("salary", "工资", AccountingRecordType.Income),
    AccountingCategory("bonus", "奖金", AccountingRecordType.Income),
    AccountingCategory("reimbursement", "报销", AccountingRecordType.Income),
    AccountingCategory("investment", "投资", AccountingRecordType.Income),
    AccountingCategory("income_other", "其他", AccountingRecordType.Income),
)
