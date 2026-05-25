# Accounting Module Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a local accounting workspace that users can switch to from the Profile screen, with ledger, stats, add/edit/delete records, local persistence, and tests.

**Architecture:** Keep accounting independent from diary Markdown, GitHub sync, and `.diary` import/export. Add small model, storage, and domain calculation units, then integrate them into `DiaryApp.kt` with module-aware bottom navigation. Reuse existing Compose and SharedPreferences patterns.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, SharedPreferences through `PreferenceStore`, JUnit 4.

---

## File Structure

- Create `app/src/main/java/com/cypress/diary/ui/navigation/AppModule.kt`
  - Defines `Diary` and `Accounting` workspace state plus labels.
- Create `app/src/main/java/com/cypress/diary/storage/AppModuleStore.kt`
  - Persists selected workspace.
- Modify `app/src/main/java/com/cypress/diary/ui/navigation/DiaryRoute.kt`
  - Adds accounting routes and module-specific root route lists.
- Create `app/src/main/java/com/cypress/diary/model/accounting/AccountingRecord.kt`
  - Defines record, type, category keys, and default categories.
- Create `app/src/main/java/com/cypress/diary/accounting/AccountingMoney.kt`
  - Parses and formats cent amounts.
- Create `app/src/main/java/com/cypress/diary/accounting/AccountingSummary.kt`
  - Pure functions for month filtering, totals, grouping, and sorting.
- Create `app/src/main/java/com/cypress/diary/storage/AccountingRecordStore.kt`
  - Loads, saves, upserts, and deletes records.
- Create `app/src/main/java/com/cypress/diary/ui/screens/AccountingLedgerScreen.kt`
  - Ledger overview and grouped records.
- Create `app/src/main/java/com/cypress/diary/ui/screens/AccountingStatsScreen.kt`
  - Monthly totals and category summaries.
- Create `app/src/main/java/com/cypress/diary/ui/screens/AccountingEditorScreen.kt`
  - Add/edit/delete record form.
- Modify `app/src/main/java/com/cypress/diary/ui/screens/ProfileScreen.kt`
  - Adds visible module switch card.
- Modify `app/src/main/java/com/cypress/diary/DiaryApp.kt`
  - Wires stores, module routes, accounting state, and screen actions.
- Add tests under `app/src/test/java/com/cypress/diary/accounting/`, `storage/`, and `ui/navigation/`.

---

### Task 1: Add Workspace Module State

**Files:**
- Create: `app/src/main/java/com/cypress/diary/ui/navigation/AppModule.kt`
- Create: `app/src/main/java/com/cypress/diary/storage/AppModuleStore.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/navigation/DiaryRoute.kt`
- Test: `app/src/test/java/com/cypress/diary/storage/AppModuleStoreTest.kt`
- Test: `app/src/test/java/com/cypress/diary/ui/navigation/DiaryRouteTest.kt`

- [ ] **Step 1: Write AppModuleStore tests**

Create `AppModuleStoreTest.kt`:

```kotlin
package com.cypress.diary.storage

import com.cypress.diary.ui.navigation.AppModule
import org.junit.Assert.assertEquals
import org.junit.Test

class AppModuleStoreTest {
    @Test
    fun defaultsToDiaryWhenNoValueIsSaved() {
        val store = AppModuleStore(InMemoryPreferenceStore())

        assertEquals(AppModule.Diary, store.load())
    }

    @Test
    fun savesAndLoadsAccountingModule() {
        val store = AppModuleStore(InMemoryPreferenceStore())

        store.save(AppModule.Accounting)

        assertEquals(AppModule.Accounting, store.load())
    }

    @Test
    fun malformedValueFallsBackToDiary() {
        val prefs = InMemoryPreferenceStore()
        prefs.putString("app_module", "unknown")

        assertEquals(AppModule.Diary, AppModuleStore(prefs).load())
    }

    private class InMemoryPreferenceStore : PreferenceStore {
        private val values = mutableMapOf<String, String?>()

        override fun getString(key: String, defaultValue: String?): String? {
            return values.getOrDefault(key, defaultValue)
        }

        override fun putString(key: String, value: String?) {
            values[key] = value
        }

        override fun remove(key: String) {
            values.remove(key)
        }
    }
}
```

- [ ] **Step 2: Extend DiaryRouteTest**

Add assertions for module-specific routes:

```kotlin
@Test
fun diaryRootRoutesMatchDiaryWorkspace() {
    assertEquals(
        listOf(DiaryRoute.Diary, DiaryRoute.Summary, DiaryRoute.Profile),
        DiaryRoute.rootRoutesFor(AppModule.Diary),
    )
}

@Test
fun accountingRootRoutesMatchAccountingWorkspace() {
    assertEquals(
        listOf(DiaryRoute.Ledger, DiaryRoute.AccountingStats, DiaryRoute.Profile),
        DiaryRoute.rootRoutesFor(AppModule.Accounting),
    )
}
```

- [ ] **Step 3: Run route and module tests to verify failure**

Run:

```powershell
.\gradlew.bat testDebugUnitTest --tests "*AppModuleStoreTest" --tests "*DiaryRouteTest" --console=plain
```

Expected: fails because `AppModule`, `AppModuleStore`, `Ledger`, `AccountingStats`, and `rootRoutesFor` do not exist.

- [ ] **Step 4: Add AppModule**

Create `AppModule.kt`:

```kotlin
package com.cypress.diary.ui.navigation

enum class AppModule(
    val label: String,
) {
    Diary("日记"),
    Accounting("记账"),
}
```

- [ ] **Step 5: Add AppModuleStore**

Create `AppModuleStore.kt`:

```kotlin
package com.cypress.diary.storage

import com.cypress.diary.ui.navigation.AppModule

class AppModuleStore(
    private val preferences: PreferenceStore,
) {
    constructor(prefs: android.content.SharedPreferences) : this(SharedPreferencesPreferenceStore(prefs))

    fun load(): AppModule {
        val value = preferences.getString(KEY_MODULE, AppModule.Diary.name)
        return AppModule.values().firstOrNull { it.name == value } ?: AppModule.Diary
    }

    fun save(module: AppModule) {
        preferences.putString(KEY_MODULE, module.name)
    }

    companion object {
        private const val KEY_MODULE = "app_module"
    }
}
```

- [ ] **Step 6: Add accounting routes**

Modify `DiaryRoute.kt` imports and objects:

```kotlin
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
```

Add route objects:

```kotlin
data object Ledger : DiaryRoute("ledger", "账本", Icons.Filled.AccountBalanceWallet)
data object AccountingStats : DiaryRoute("accounting_stats", "统计", Icons.Filled.BarChart)
data object AccountingEditor : DiaryRoute("accounting_editor", "记一笔", Icons.Filled.Add)
```

Replace the companion object with:

```kotlin
companion object {
    val diaryRootRoutes = listOf(Diary, Summary, Profile)
    val accountingRootRoutes = listOf(Ledger, AccountingStats, Profile)
    val rootRoutes = diaryRootRoutes

    fun rootRoutesFor(module: AppModule): List<DiaryRoute> {
        return when (module) {
            AppModule.Diary -> diaryRootRoutes
            AppModule.Accounting -> accountingRootRoutes
        }
    }
}
```

- [ ] **Step 7: Run tests**

Run:

```powershell
.\gradlew.bat testDebugUnitTest --tests "*AppModuleStoreTest" --tests "*DiaryRouteTest" --console=plain
```

Expected: PASS.

- [ ] **Step 8: Commit**

```powershell
git add app/src/main/java/com/cypress/diary/ui/navigation/AppModule.kt app/src/main/java/com/cypress/diary/storage/AppModuleStore.kt app/src/main/java/com/cypress/diary/ui/navigation/DiaryRoute.kt app/src/test/java/com/cypress/diary/storage/AppModuleStoreTest.kt app/src/test/java/com/cypress/diary/ui/navigation/DiaryRouteTest.kt
git commit -m "feat: add app module navigation state"
```

---

### Task 2: Add Accounting Models, Money Helpers, and Summary Logic

**Files:**
- Create: `app/src/main/java/com/cypress/diary/model/accounting/AccountingRecord.kt`
- Create: `app/src/main/java/com/cypress/diary/accounting/AccountingMoney.kt`
- Create: `app/src/main/java/com/cypress/diary/accounting/AccountingSummary.kt`
- Test: `app/src/test/java/com/cypress/diary/accounting/AccountingMoneyTest.kt`
- Test: `app/src/test/java/com/cypress/diary/accounting/AccountingSummaryTest.kt`

- [ ] **Step 1: Write money tests**

Create `AccountingMoneyTest.kt`:

```kotlin
package com.cypress.diary.accounting

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AccountingMoneyTest {
    @Test
    fun parsesYuanInputToCents() {
        assertEquals(12345L, parseAmountCents("123.45"))
        assertEquals(1200L, parseAmountCents("12"))
        assertEquals(1200L, parseAmountCents("12.0"))
        assertEquals(1205L, parseAmountCents("12.05"))
    }

    @Test
    fun rejectsInvalidAmounts() {
        assertNull(parseAmountCents(""))
        assertNull(parseAmountCents("abc"))
        assertNull(parseAmountCents("0"))
        assertNull(parseAmountCents("-1"))
        assertNull(parseAmountCents("12.345"))
    }

    @Test
    fun formatsCentsAsDecimalAmount() {
        assertEquals("0.01", formatAmountCents(1))
        assertEquals("12.00", formatAmountCents(1200))
        assertEquals("123.45", formatAmountCents(12345))
    }
}
```

- [ ] **Step 2: Write summary tests**

Create `AccountingSummaryTest.kt` with records that cover month filtering, totals, categories, and sorting:

```kotlin
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
```

- [ ] **Step 3: Run tests to verify failure**

```powershell
.\gradlew.bat testDebugUnitTest --tests "*AccountingMoneyTest" --tests "*AccountingSummaryTest" --console=plain
```

Expected: fails because accounting model and functions do not exist.

- [ ] **Step 4: Add accounting model**

Create `AccountingRecord.kt`:

```kotlin
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
```

- [ ] **Step 5: Add money helpers**

Create `AccountingMoney.kt`:

```kotlin
package com.cypress.diary.accounting

fun parseAmountCents(input: String): Long? {
    val normalized = input.trim()
    if (normalized.isBlank()) return null
    if (!normalized.matches(Regex("""\d+(\.\d{1,2})?"""))) return null
    val parts = normalized.split(".")
    val yuan = parts[0].toLongOrNull() ?: return null
    val centsText = parts.getOrNull(1).orEmpty().padEnd(2, '0')
    val cents = centsText.ifBlank { "00" }.toLongOrNull() ?: return null
    val total = yuan * 100 + cents
    return total.takeIf { it > 0 }
}

fun formatAmountCents(amountCents: Long): String {
    val absValue = kotlin.math.abs(amountCents)
    val sign = if (amountCents < 0) "-" else ""
    return "$sign${absValue / 100}.${(absValue % 100).toString().padStart(2, '0')}"
}
```

- [ ] **Step 6: Add summary helpers**

Create `AccountingSummary.kt`:

```kotlin
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
```

- [ ] **Step 7: Run tests**

```powershell
.\gradlew.bat testDebugUnitTest --tests "*AccountingMoneyTest" --tests "*AccountingSummaryTest" --console=plain
```

Expected: PASS.

- [ ] **Step 8: Commit**

```powershell
git add app/src/main/java/com/cypress/diary/model/accounting/AccountingRecord.kt app/src/main/java/com/cypress/diary/accounting/AccountingMoney.kt app/src/main/java/com/cypress/diary/accounting/AccountingSummary.kt app/src/test/java/com/cypress/diary/accounting/AccountingMoneyTest.kt app/src/test/java/com/cypress/diary/accounting/AccountingSummaryTest.kt
git commit -m "feat: add accounting domain model"
```

---

### Task 3: Add Accounting Record Persistence

**Files:**
- Create: `app/src/main/java/com/cypress/diary/storage/AccountingRecordStore.kt`
- Test: `app/src/test/java/com/cypress/diary/storage/AccountingRecordStoreTest.kt`

- [ ] **Step 1: Write store tests**

Create tests for round-trip, upsert, delete, sorting, and malformed entries:

```kotlin
package com.cypress.diary.storage

import com.cypress.diary.model.accounting.AccountingRecord
import com.cypress.diary.model.accounting.AccountingRecordType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class AccountingRecordStoreTest {
    @Test
    fun savesAndLoadsRecords() {
        val store = AccountingRecordStore(InMemoryPreferenceStore())
        val records = listOf(record("a"), record("b", amountCents = 2500))

        store.saveRecords(records)

        assertEquals(records, store.loadRecords())
    }

    @Test
    fun upsertReplacesExistingRecordById() {
        val store = AccountingRecordStore(InMemoryPreferenceStore())
        store.saveRecords(listOf(record("a", amountCents = 100)))

        store.upsert(record("a", amountCents = 200))

        assertEquals(listOf(record("a", amountCents = 200)), store.loadRecords())
    }

    @Test
    fun deleteRemovesRecordById() {
        val store = AccountingRecordStore(InMemoryPreferenceStore())
        store.saveRecords(listOf(record("a"), record("b")))

        store.delete("a")

        assertEquals(listOf(record("b")), store.loadRecords())
    }

    @Test
    fun malformedRecordsAreSkipped() {
        val prefs = InMemoryPreferenceStore()
        prefs.putString("accounting_records", "bad-line\n${AccountingRecordStore.encode(record("ok"))}")

        assertEquals(listOf(record("ok")), AccountingRecordStore(prefs).loadRecords())
    }

    private fun record(
        id: String,
        amountCents: Long = 1200,
    ): AccountingRecord {
        return AccountingRecord(
            id = id,
            type = AccountingRecordType.Expense,
            amountCents = amountCents,
            category = "餐饮",
            date = LocalDate.of(2026, 5, 25),
            note = "午餐",
            createdAt = 10,
            updatedAt = 20,
        )
    }

    private class InMemoryPreferenceStore : PreferenceStore {
        private val values = mutableMapOf<String, String?>()

        override fun getString(key: String, defaultValue: String?): String? {
            return values.getOrDefault(key, defaultValue)
        }

        override fun putString(key: String, value: String?) {
            values[key] = value
        }

        override fun remove(key: String) {
            values.remove(key)
        }
    }
}
```

- [ ] **Step 2: Run store test to verify failure**

```powershell
.\gradlew.bat testDebugUnitTest --tests "*AccountingRecordStoreTest" --console=plain
```

Expected: fails because `AccountingRecordStore` does not exist.

- [ ] **Step 3: Implement AccountingRecordStore**

Create `AccountingRecordStore.kt`:

```kotlin
package com.cypress.diary.storage

import com.cypress.diary.accounting.sortRecordsForLedger
import com.cypress.diary.model.accounting.AccountingRecord
import com.cypress.diary.model.accounting.AccountingRecordType
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.util.Base64

class AccountingRecordStore(
    private val preferences: PreferenceStore,
) {
    constructor(prefs: android.content.SharedPreferences) : this(SharedPreferencesPreferenceStore(prefs))

    fun loadRecords(): List<AccountingRecord> {
        return preferences.getString(KEY_RECORDS, "")
            .orEmpty()
            .lineSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { line -> runCatching { decode(line) }.getOrNull() }
            .toList()
            .let(::sortRecordsForLedger)
    }

    fun saveRecords(records: List<AccountingRecord>) {
        preferences.putString(KEY_RECORDS, records.joinToString("\n") { encode(it) })
    }

    fun upsert(record: AccountingRecord) {
        saveRecords((loadRecords().filterNot { it.id == record.id } + record).let(::sortRecordsForLedger))
    }

    fun delete(id: String) {
        saveRecords(loadRecords().filterNot { it.id == id })
    }

    companion object {
        private const val KEY_RECORDS = "accounting_records"

        fun encode(record: AccountingRecord): String {
            return listOf(
                safe(record.id),
                record.type.name,
                record.amountCents.toString(),
                safe(record.category),
                record.date.toString(),
                safe(record.note),
                record.createdAt.toString(),
                record.updatedAt.toString(),
            ).joinToString("|")
        }

        private fun decode(value: String): AccountingRecord {
            val parts = value.split("|")
            require(parts.size == 8) { "invalid accounting record" }
            return AccountingRecord(
                id = unsafe(parts[0]),
                type = AccountingRecordType.valueOf(parts[1]),
                amountCents = parts[2].toLong(),
                category = unsafe(parts[3]),
                date = LocalDate.parse(parts[4]),
                note = unsafe(parts[5]),
                createdAt = parts[6].toLong(),
                updatedAt = parts[7].toLong(),
            )
        }

        private fun safe(value: String): String {
            return Base64.getUrlEncoder().encodeToString(value.toByteArray(StandardCharsets.UTF_8))
        }

        private fun unsafe(value: String): String {
            return String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8)
        }
    }
}
```

- [ ] **Step 4: Run store test**

```powershell
.\gradlew.bat testDebugUnitTest --tests "*AccountingRecordStoreTest" --console=plain
```

Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
git add app/src/main/java/com/cypress/diary/storage/AccountingRecordStore.kt app/src/test/java/com/cypress/diary/storage/AccountingRecordStoreTest.kt
git commit -m "feat: persist accounting records"
```

---

### Task 4: Build Accounting Ledger Screen

**Files:**
- Create: `app/src/main/java/com/cypress/diary/ui/screens/AccountingLedgerScreen.kt`

- [ ] **Step 1: Create ledger screen**

Create `AccountingLedgerScreen.kt`:

```kotlin
package com.cypress.diary.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.cypress.diary.ui.components.RefreshableScreen
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun AccountingLedgerScreen(
    records: List<AccountingRecord>,
    selectedMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    onRecordSelected: (AccountingRecord) -> Unit,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val monthlyRecords = recordsForMonth(records, selectedMonth)
    val summary = monthlySummary(records, selectedMonth)
    RefreshableScreen(
        refreshing = refreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = { onMonthChange(selectedMonth.minusMonths(1)) }) {
                Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "上个月")
            }
            Text(
                text = selectedMonth.format(DateTimeFormatter.ofPattern("yyyy年M月")),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            IconButton(onClick = { onMonthChange(selectedMonth.plusMonths(1)) }) {
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "下个月")
            }
        }
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
        if (monthlyRecords.isEmpty()) {
            Text(
                text = "这个月还没有账目",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
            )
        } else {
            sortRecordsForLedger(monthlyRecords)
                .groupBy { it.date }
                .forEach { (date, dayRecords) ->
                    Text(
                        text = "${date.monthValue}月${date.dayOfMonth}日",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    dayRecords.forEach { record ->
                        AccountingRecordRow(record = record, onClick = { onRecordSelected(record) })
                    }
                }
        }
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
```

- [ ] **Step 2: Run compile check for screen**

```powershell
.\gradlew.bat testDebugUnitTest --console=plain
```

Expected: PASS after prior tasks are present.

- [ ] **Step 3: Commit**

```powershell
git add app/src/main/java/com/cypress/diary/ui/screens/AccountingLedgerScreen.kt
git commit -m "feat: add accounting ledger screen"
```

---

### Task 5: Build Accounting Stats Screen

**Files:**
- Create: `app/src/main/java/com/cypress/diary/ui/screens/AccountingStatsScreen.kt`

- [ ] **Step 1: Create stats screen**

Create `AccountingStatsScreen.kt`:

```kotlin
package com.cypress.diary.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cypress.diary.accounting.categoryTotals
import com.cypress.diary.accounting.formatAmountCents
import com.cypress.diary.accounting.monthlySummary
import com.cypress.diary.accounting.recordsForMonth
import com.cypress.diary.accounting.AccountingCategoryTotal
import com.cypress.diary.model.accounting.AccountingRecord
import com.cypress.diary.model.accounting.AccountingRecordType
import com.cypress.diary.ui.components.RefreshableScreen
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun AccountingStatsScreen(
    records: List<AccountingRecord>,
    selectedMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val monthlyRecords = recordsForMonth(records, selectedMonth)
    val summary = monthlySummary(records, selectedMonth)
    val expenseTotals = categoryTotals(monthlyRecords, AccountingRecordType.Expense)
    val incomeTotals = categoryTotals(monthlyRecords, AccountingRecordType.Income)
    RefreshableScreen(
        refreshing = refreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = { onMonthChange(selectedMonth.minusMonths(1)) }) {
                Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "上个月")
            }
            Text(
                text = selectedMonth.format(DateTimeFormatter.ofPattern("yyyy年M月")),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            IconButton(onClick = { onMonthChange(selectedMonth.plusMonths(1)) }) {
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "下个月")
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("月度统计", fontWeight = FontWeight.SemiBold)
                Text("收入 ¥${formatAmountCents(summary.incomeCents)}")
                Text("支出 ¥${formatAmountCents(summary.expenseCents)}")
                Text("结余 ¥${formatAmountCents(summary.balanceCents)}")
            }
        }
        CategoryTotalSection("支出分类", expenseTotals)
        CategoryTotalSection("收入分类", incomeTotals)
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
                        progress = { if (max == 0L) 0f else total.amountCents.toFloat() / max.toFloat() },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 2: Run compile check**

```powershell
.\gradlew.bat testDebugUnitTest --console=plain
```

Expected: PASS.

- [ ] **Step 3: Commit**

```powershell
git add app/src/main/java/com/cypress/diary/ui/screens/AccountingStatsScreen.kt
git commit -m "feat: add accounting stats screen"
```

---

### Task 6: Build Accounting Editor Screen

**Files:**
- Create: `app/src/main/java/com/cypress/diary/ui/screens/AccountingEditorScreen.kt`

- [ ] **Step 1: Create editor screen**

Create `AccountingEditorScreen.kt`:

```kotlin
package com.cypress.diary.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cypress.diary.accounting.formatAmountCents
import com.cypress.diary.accounting.parseAmountCents
import com.cypress.diary.model.accounting.AccountingRecord
import com.cypress.diary.model.accounting.AccountingRecordType
import com.cypress.diary.model.accounting.defaultAccountingCategories
import com.cypress.diary.ui.components.RefreshableScreen
import java.time.LocalDate
import java.util.UUID

@Composable
fun AccountingEditorScreen(
    record: AccountingRecord?,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    onSave: (AccountingRecord) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var typeName by rememberSaveable(record?.id) { mutableStateOf(record?.type?.name ?: AccountingRecordType.Expense.name) }
    val type = AccountingRecordType.valueOf(typeName)
    var amountText by rememberSaveable(record?.id) { mutableStateOf(record?.amountCents?.let(::formatAmountCents).orEmpty()) }
    var category by rememberSaveable(record?.id) {
        mutableStateOf(record?.category ?: defaultAccountingCategories.first { it.type == type }.label)
    }
    var dateText by rememberSaveable(record?.id) { mutableStateOf(record?.date?.toString() ?: LocalDate.now().toString()) }
    var note by rememberSaveable(record?.id) { mutableStateOf(record?.note.orEmpty()) }
    var showDeleteConfirm by rememberSaveable(record?.id) { mutableStateOf(false) }
    val amountCents = parseAmountCents(amountText)
    val parsedDate = runCatching { LocalDate.parse(dateText) }.getOrNull()
    val canSave = amountCents != null && parsedDate != null && category.isNotBlank()

    RefreshableScreen(
        refreshing = refreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = if (record == null) "记一笔" else "编辑账目",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AccountingRecordType.values().forEach { option ->
                FilterChip(
                    selected = type == option,
                    onClick = {
                        typeName = option.name
                        category = defaultAccountingCategories.first { it.type == option }.label
                    },
                    label = { Text(option.label) },
                )
            }
        }
        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("金额") },
            singleLine = true,
            isError = amountText.isNotBlank() && amountCents == null,
        )
        OutlinedTextField(
            value = dateText,
            onValueChange = { dateText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("日期 YYYY-MM-DD") },
            singleLine = true,
            isError = dateText.isNotBlank() && parsedDate == null,
        )
        Text("分类", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            defaultAccountingCategories.filter { it.type == type }.forEach { option ->
                FilterChip(
                    selected = category == option.label,
                    onClick = { category = option.label },
                    label = { Text(option.label) },
                )
            }
        }
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("备注") },
        )
        Button(
            enabled = canSave,
            onClick = {
                val now = System.currentTimeMillis()
                onSave(
                    AccountingRecord(
                        id = record?.id ?: UUID.randomUUID().toString(),
                        type = type,
                        amountCents = requireNotNull(amountCents),
                        category = category,
                        date = requireNotNull(parsedDate),
                        note = note,
                        createdAt = record?.createdAt ?: now,
                        updatedAt = now,
                    ),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("保存")
        }
        if (record != null) {
            TextButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("删除这笔账目")
            }
        }
        if (showDeleteConfirm && record != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("删除账目") },
                text = { Text("删除后无法恢复。") },
                confirmButton = {
                    TextButton(onClick = { onDelete(record.id) }) {
                        Text("删除")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("取消")
                    }
                },
            )
        }
    }
}
```

- [ ] **Step 2: Run compile check**

```powershell
.\gradlew.bat testDebugUnitTest --console=plain
```

Expected: PASS.

- [ ] **Step 3: Commit**

```powershell
git add app/src/main/java/com/cypress/diary/ui/screens/AccountingEditorScreen.kt
git commit -m "feat: add accounting editor screen"
```

---

### Task 7: Integrate Accounting Workspace into Profile and DiaryApp

**Files:**
- Modify: `app/src/main/java/com/cypress/diary/ui/screens/ProfileScreen.kt`
- Modify: `app/src/main/java/com/cypress/diary/DiaryApp.kt`

- [ ] **Step 1: Modify ProfileScreen signature**

Add parameters:

```kotlin
currentModule: AppModule,
onModuleSelected: (AppModule) -> Unit,
```

Add import:

```kotlin
import com.cypress.diary.ui.navigation.AppModule
```

- [ ] **Step 2: Add module switch card after AccountCard**

Add this block after the private diary account card:

```kotlin
AccountCard {
    Text("模块", fontWeight = FontWeight.SemiBold)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        AppModule.values().forEach { module ->
            FilterChip(
                selected = currentModule == module,
                onClick = { onModuleSelected(module) },
                label = { Text(module.label) },
            )
        }
    }
    Text(
        text = if (currentModule == AppModule.Diary) {
            "当前使用日记模块"
        } else {
            "当前使用记账模块"
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
    )
}
```

- [ ] **Step 3: Modify DiaryApp imports and stores**

Add imports:

```kotlin
import androidx.compose.material.icons.filled.Add
import com.cypress.diary.model.accounting.AccountingRecord
import com.cypress.diary.storage.AccountingRecordStore
import com.cypress.diary.storage.AppModuleStore
import com.cypress.diary.ui.navigation.AppModule
import com.cypress.diary.ui.screens.AccountingEditorScreen
import com.cypress.diary.ui.screens.AccountingLedgerScreen
import com.cypress.diary.ui.screens.AccountingStatsScreen
import java.time.YearMonth
```

Add stores:

```kotlin
val moduleStore = remember(context) {
    AppModuleStore(context.getSharedPreferences("app_module", android.content.Context.MODE_PRIVATE))
}
val accountingRecordStore = remember(context) {
    AccountingRecordStore(context.getSharedPreferences("accounting_records", android.content.Context.MODE_PRIVATE))
}
```

Add state:

```kotlin
var activeModuleName by rememberSaveable { mutableStateOf(moduleStore.load().name) }
var accountingRecords by remember { mutableStateOf(accountingRecordStore.loadRecords()) }
var accountingMonthValue by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
var selectedAccountingRecordId by rememberSaveable { mutableStateOf<String?>(null) }

val activeModule = AppModule.valueOf(activeModuleName)
val accountingMonth = YearMonth.parse(accountingMonthValue)
val selectedAccountingRecord = accountingRecords.firstOrNull { it.id == selectedAccountingRecordId }
```

- [ ] **Step 4: Add module change and record persistence helpers**

Add inside `DiaryApp()`:

```kotlin
fun selectModule(module: AppModule) {
    activeModuleName = module.name
    moduleStore.save(module)
    route = when (module) {
        AppModule.Diary -> DiaryRoute.Diary.route
        AppModule.Accounting -> DiaryRoute.Ledger.route
    }
}

fun saveAccountingRecord(record: AccountingRecord) {
    accountingRecordStore.upsert(record)
    accountingRecords = accountingRecordStore.loadRecords()
}

fun deleteAccountingRecord(id: String) {
    accountingRecordStore.delete(id)
    accountingRecords = accountingRecordStore.loadRecords()
}
```

- [ ] **Step 5: Make bottom navigation module-aware**

Replace hardcoded `NavigationBarItem` blocks with:

```kotlin
DiaryRoute.rootRoutesFor(activeModule).forEach { rootRoute ->
    NavigationBarItem(
        selected = when (rootRoute) {
            DiaryRoute.Diary -> route == DiaryRoute.Diary.route || route == DiaryRoute.Editor.route
            DiaryRoute.Ledger -> route == DiaryRoute.Ledger.route || route == DiaryRoute.AccountingEditor.route
            else -> route == rootRoute.route
        },
        onClick = { route = rootRoute.route },
        icon = {
            Icon(
                imageVector = rootRoute.icon,
                contentDescription = rootRoute.label,
            )
        },
        label = { Text(rootRoute.label) },
    )
}
```

- [ ] **Step 6: Make FAB module-aware**

Replace the FAB condition with:

```kotlin
when {
    activeModule == AppModule.Diary && route == DiaryRoute.Diary.route -> {
        FloatingActionButton(
            onClick = {
                editorDocumentPath = null
                editorDocumentFallback = null
                editorModeName = EditMode.Day.name
                route = DiaryRoute.Editor.route
            },
        ) {
            Icon(imageVector = Icons.Filled.Edit, contentDescription = "编辑")
        }
    }
    activeModule == AppModule.Accounting && route == DiaryRoute.Ledger.route -> {
        FloatingActionButton(
            onClick = {
                selectedAccountingRecordId = null
                route = DiaryRoute.AccountingEditor.route
            },
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "记一笔")
        }
    }
}
```

- [ ] **Step 7: Pass module controls to ProfileScreen**

Add arguments to `ProfileScreen`:

```kotlin
currentModule = activeModule,
onModuleSelected = ::selectModule,
```

- [ ] **Step 8: Add route rendering branches**

Add branches to the `when (route)`:

```kotlin
DiaryRoute.Ledger.route -> AccountingLedgerScreen(
    records = accountingRecords,
    selectedMonth = accountingMonth,
    onMonthChange = { month -> accountingMonthValue = month.toString() },
    onRecordSelected = { record ->
        selectedAccountingRecordId = record.id
        route = DiaryRoute.AccountingEditor.route
    },
    refreshing = refreshing,
    onRefresh = { refreshWeeks() },
    modifier = Modifier.padding(innerPadding),
)

DiaryRoute.AccountingStats.route -> AccountingStatsScreen(
    records = accountingRecords,
    selectedMonth = accountingMonth,
    onMonthChange = { month -> accountingMonthValue = month.toString() },
    refreshing = refreshing,
    onRefresh = { refreshWeeks() },
    modifier = Modifier.padding(innerPadding),
)

DiaryRoute.AccountingEditor.route -> AccountingEditorScreen(
    record = selectedAccountingRecord,
    refreshing = refreshing,
    onRefresh = { refreshWeeks() },
    onBack = {
        selectedAccountingRecordId = null
        route = DiaryRoute.Ledger.route
    },
    onSave = { record ->
        saveAccountingRecord(record)
        selectedAccountingRecordId = null
        route = DiaryRoute.Ledger.route
        Toast.makeText(context, "账目已保存", Toast.LENGTH_SHORT).show()
    },
    onDelete = { id ->
        deleteAccountingRecord(id)
        selectedAccountingRecordId = null
        route = DiaryRoute.Ledger.route
        Toast.makeText(context, "账目已删除", Toast.LENGTH_SHORT).show()
    },
    modifier = Modifier.padding(innerPadding),
)
```

- [ ] **Step 9: Run unit tests and compile**

```powershell
.\gradlew.bat testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
```

Expected: both PASS.

- [ ] **Step 10: Commit**

```powershell
git add app/src/main/java/com/cypress/diary/DiaryApp.kt app/src/main/java/com/cypress/diary/ui/screens/ProfileScreen.kt
git commit -m "feat: integrate accounting workspace"
```

---

### Task 8: Final Verification

**Files:**
- Verify only; no planned file changes.

- [ ] **Step 1: Check worktree**

```powershell
git status --short
```

Expected: only pre-existing unrelated untracked files remain, or no output if those files have been handled separately.

- [ ] **Step 2: Run full unit test suite**

```powershell
.\gradlew.bat testDebugUnitTest --console=plain
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Build debug APK**

```powershell
.\gradlew.bat :app:assembleDebug --console=plain
```

Expected: `BUILD SUCCESSFUL` and APK at `app/build/outputs/apk/debug/app-debug.apk`.

- [ ] **Step 4: Manual smoke checklist**

Run the app on an emulator or device and verify:

- App opens in diary workspace by default.
- Profile screen shows module switch card.
- Switching to accounting changes bottom navigation to `账本 / 统计 / 我的`.
- Ledger screen opens and empty state is visible.
- FAB opens accounting editor.
- Saving an expense returns to ledger and shows the record.
- Stats screen shows expense total and category total.
- Editing the record changes amount/category/note.
- Deleting the record removes it from ledger and stats.
- Switching back to diary restores `日记 / 总结 / 我的`.
- Hidden GitHub seven-tap entry still works from Profile avatar.

- [ ] **Step 5: Resolve verification failures through the relevant task**

If verification reveals a defect, return to the task that introduced that file, make the smallest targeted code or test change there, rerun the same two verification commands from Steps 2 and 3, then commit the exact files changed by that task using that task's commit pattern.
