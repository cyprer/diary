# Accounting Custom Categories Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let users add local accounting category labels and migrate those custom labels through `.accounting` import/export.

**Architecture:** Store only custom categories separately from built-in defaults. Keep `AccountingRecord.category` as text for compatibility, combine defaults plus custom categories for editor UI, and extend `AccountingExportArchive` with optional `categories.json`.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, SharedPreferences-backed local storage, zip archives, JUnit unit tests.

---

### Task 1: Category Persistence

**Files:**
- Create: `app/src/main/java/com/cypress/diary/storage/AccountingCategoryStore.kt`
- Create: `app/src/test/java/com/cypress/diary/storage/AccountingCategoryStoreTest.kt`

- [ ] Write tests for saving/loading custom categories and skipping damaged rows.
- [ ] Run focused tests and confirm they fail because the store does not exist.
- [ ] Implement `AccountingCategoryStore` with Base64-safe line storage.
- [ ] Run focused tests and confirm they pass.

### Task 2: Category Merge Helpers

**Files:**
- Modify: `app/src/main/java/com/cypress/diary/accounting/AccountingSummary.kt`
- Modify: `app/src/test/java/com/cypress/diary/accounting/AccountingSummaryTest.kt`

- [ ] Write tests for merging custom categories by `type + label`.
- [ ] Implement `mergeAccountingCategories(local, imported)`.
- [ ] Run focused accounting tests.

### Task 3: Archive Categories

**Files:**
- Modify: `app/src/main/java/com/cypress/diary/export/AccountingExportArchive.kt`
- Modify: `app/src/test/java/com/cypress/diary/export/AccountingExportArchiveTest.kt`

- [ ] Add failing tests for writing/reading `categories.json`.
- [ ] Add a compatibility test for old archives with no `categories.json`.
- [ ] Extend archive read/write with `AccountingArchiveData(records, customCategories)`.
- [ ] Keep old `write(records, output)` and `read(input)` wrappers for simple callers.
- [ ] Run focused archive tests.

### Task 4: Editor UI and App Wiring

**Files:**
- Modify: `app/src/main/java/com/cypress/diary/DiaryApp.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/screens/AccountingEditorScreen.kt`

- [ ] Load custom categories from `AccountingCategoryStore`.
- [ ] Pass combined default + custom categories into editor.
- [ ] Add `+ 新分类` chip and dialog.
- [ ] Save new custom category locally and select it immediately.
- [ ] Include custom categories in accounting export/import replace/merge flows.

### Task 5: Docs and Verification

**Files:**
- Modify: `docs/AI_PROJECT_GUIDE.md`

- [ ] Document custom categories and `.accounting` category migration.
- [ ] Run `.\gradlew.bat testDebugUnitTest --console=plain`.
- [ ] Run `.\gradlew.bat :app:assembleDebug --console=plain`.
- [ ] Commit implementation.
