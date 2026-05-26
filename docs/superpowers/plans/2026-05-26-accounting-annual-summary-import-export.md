# Accounting Annual Summary and Import Export Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add month/year switching to accounting statistics and add independent `.accounting` import/export for bill records.

**Architecture:** Keep accounting logic isolated from diary Markdown, GitHub sync, and `.diary` archives. Add pure accounting summary/import helpers first, then wire Compose UI through `DiaryApp` and `ProfileScreen`.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, SharedPreferences-backed local storage, zip archives, JUnit unit tests.

---

### Task 1: Annual Accounting Summary Logic

**Files:**
- Modify: `app/src/main/java/com/cypress/diary/accounting/AccountingSummary.kt`
- Modify: `app/src/test/java/com/cypress/diary/accounting/AccountingSummaryTest.kt`

- [ ] **Step 1: Write failing tests**

Add tests for `recordsForYear`, `yearlySummary`, and `monthlyTotalsForYear`.

- [ ] **Step 2: Run focused tests**

Run: `.\gradlew.bat testDebugUnitTest --tests com.cypress.diary.accounting.AccountingSummaryTest --console=plain`
Expected: fail because yearly functions do not exist.

- [ ] **Step 3: Implement pure summary functions**

Add `AccountingMonthTotal`, `recordsForYear(records, year)`, `yearlySummary(records, year)`, and `monthlyTotalsForYear(records, year)`.

- [ ] **Step 4: Verify focused tests pass**

Run the same focused test command.

### Task 2: Statistics Screen Month/Year Modes

**Files:**
- Modify: `app/src/main/java/com/cypress/diary/DiaryApp.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/screens/AccountingStatsScreen.kt`

- [ ] **Step 1: Add UI state**

Add saveable state in `DiaryApp` for stats mode and selected accounting year.

- [ ] **Step 2: Extend screen parameters**

Pass selected year, mode, and callbacks into `AccountingStatsScreen`.

- [ ] **Step 3: Implement segmented mode control**

Use Material 3 buttons to switch between `月度` and `年度`.

- [ ] **Step 4: Implement yearly content**

Show yearly total card, 12 month trend rows, yearly expense category totals, and yearly income category totals.

### Task 3: Accounting Export Archive

**Files:**
- Create: `app/src/main/java/com/cypress/diary/export/AccountingExportArchive.kt`
- Create: `app/src/test/java/com/cypress/diary/export/AccountingExportArchiveTest.kt`

- [ ] **Step 1: Write failing archive tests**

Test write/read round trip and damaged-record skipping.

- [ ] **Step 2: Run focused tests**

Run: `.\gradlew.bat testDebugUnitTest --tests com.cypress.diary.export.AccountingExportArchiveTest --console=plain`
Expected: fail because archive class does not exist.

- [ ] **Step 3: Implement archive**

Write `manifest.json` and `records.json` inside a zip; read records back while skipping invalid records.

- [ ] **Step 4: Verify focused tests pass**

Run the same focused test command.

### Task 4: Accounting Import Merge Rules

**Files:**
- Modify: `app/src/main/java/com/cypress/diary/accounting/AccountingSummary.kt`
- Modify: `app/src/test/java/com/cypress/diary/accounting/AccountingSummaryTest.kt`

- [ ] **Step 1: Write failing merge tests**

Test replace result and merge-by-id result.

- [ ] **Step 2: Run focused tests**

Run: `.\gradlew.bat testDebugUnitTest --tests com.cypress.diary.accounting.AccountingSummaryTest --console=plain`
Expected: fail because merge helpers do not exist.

- [ ] **Step 3: Implement merge helpers**

Add `replaceAccountingRecords(imported)` and `mergeAccountingRecords(local, imported)`.

- [ ] **Step 4: Verify focused tests pass**

Run the same focused test command.

### Task 5: Profile Import/Export Wiring

**Files:**
- Modify: `app/src/main/java/com/cypress/diary/DiaryApp.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/screens/ProfileScreen.kt`

- [ ] **Step 1: Add callbacks and launchers**

Add `onExportAccounting` and `onImportAccounting` callbacks to `ProfileScreen`; use system document picker/create document.

- [ ] **Step 2: Add accounting data buttons**

Add `导出账单数据` and `导入账单数据` below diary data buttons with explanatory text.

- [ ] **Step 3: Add import mode dialog**

After reading an accounting archive, show a dialog with `替换本地账单`, `合并到账单`, and `取消`.

- [ ] **Step 4: Wire export/import in DiaryApp**

Use `AccountingExportArchive`, `AccountingRecordStore.saveRecords`, `replaceAccountingRecords`, and `mergeAccountingRecords`.

### Task 6: Documentation and Verification

**Files:**
- Modify: `docs/AI_PROJECT_GUIDE.md`

- [ ] **Step 1: Update guide**

Document monthly/yearly stats and `.accounting` import/export.

- [ ] **Step 2: Run full tests**

Run: `.\gradlew.bat testDebugUnitTest --console=plain`
Expected: build successful.

- [ ] **Step 3: Build debug APK**

Run: `.\gradlew.bat :app:assembleDebug --console=plain`
Expected: build successful.

- [ ] **Step 4: Commit implementation**

Stage only relevant code, tests, docs, specs, and this plan.
