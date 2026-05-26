# Todo Module Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a third first-class Todo module with local persistence, list filtering, editing, completion toggles, focused tests, README update, and refreshed APK.

**Architecture:** Follow the existing Diary/Accounting structure: a model package, a SharedPreferences-backed store, small pure sorting/filtering helpers, two Compose screens, and integration in `DiaryApp` through existing route/module state. Keep Todo local-only for this iteration.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, SharedPreferences via `PreferenceStore`, JUnit 4, Gradle Android plugin.

---

## File Structure

- Create `app/src/main/java/com/cypress/diary/model/todo/TodoItem.kt`: todo data classes/enums.
- Create `app/src/main/java/com/cypress/diary/todo/TodoFilters.kt`: pure filter and sorting helpers.
- Create `app/src/main/java/com/cypress/diary/storage/TodoItemStore.kt`: local persistence.
- Create `app/src/main/java/com/cypress/diary/ui/screens/TodoListScreen.kt`: list/filter UI and quick completion toggle.
- Create `app/src/main/java/com/cypress/diary/ui/screens/TodoEditorScreen.kt`: create/edit/delete UI.
- Modify `app/src/main/java/com/cypress/diary/ui/navigation/AppModule.kt`: add `Todo`.
- Modify `app/src/main/java/com/cypress/diary/ui/navigation/DiaryRoute.kt`: add Todo routes and root routes.
- Modify `app/src/main/java/com/cypress/diary/DiaryApp.kt`: wire state, callbacks, routes, FAB behavior.
- Modify `README.md`: mention Todo in user-facing features.
- Replace `release/dailylife.apk` after a successful debug build.
- Test `app/src/test/java/com/cypress/diary/storage/TodoItemStoreTest.kt`.
- Test `app/src/test/java/com/cypress/diary/todo/TodoFiltersTest.kt`.
- Test existing `DiaryRouteTest.kt` and `AppModuleStoreTest.kt`.

## Task 1: Model and Pure Todo Rules

**Files:**
- Create: `app/src/main/java/com/cypress/diary/model/todo/TodoItem.kt`
- Create: `app/src/main/java/com/cypress/diary/todo/TodoFilters.kt`
- Test: `app/src/test/java/com/cypress/diary/todo/TodoFiltersTest.kt`

- [ ] **Step 1: Write failing filter and sort tests**

Create `TodoFiltersTest.kt` with tests for all filters and ordering:

```kotlin
package com.cypress.diary.todo

import com.cypress.diary.model.todo.TodoItem
import com.cypress.diary.model.todo.TodoPriority
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class TodoFiltersTest {
    private val today = LocalDate.of(2026, 5, 26)

    @Test
    fun filtersTodayFutureCompletedAndAll() {
        val todayItem = item("today", dueDate = today)
        val futureItem = item("future", dueDate = today.plusDays(1))
        val unscheduled = item("none", dueDate = null)
        val done = item("done", completed = true, dueDate = today)
        val items = listOf(done, futureItem, unscheduled, todayItem)

        assertEquals(listOf("today", "future", "none", "done"), filterTodoItems(items, TodoFilter.All, today).map { it.id })
        assertEquals(listOf("today"), filterTodoItems(items, TodoFilter.Today, today).map { it.id })
        assertEquals(listOf("future", "none"), filterTodoItems(items, TodoFilter.Future, today).map { it.id })
        assertEquals(listOf("done"), filterTodoItems(items, TodoFilter.Completed, today).map { it.id })
    }

    @Test
    fun sortsOpenDatedPriorityThenCompletedUpdatedDescending() {
        val lowToday = item("low", dueDate = today, priority = TodoPriority.Low, updatedAt = 1)
        val highToday = item("high", dueDate = today, priority = TodoPriority.High, updatedAt = 2)
        val tomorrow = item("tomorrow", dueDate = today.plusDays(1), priority = TodoPriority.High, updatedAt = 3)
        val noDate = item("none", dueDate = null, priority = TodoPriority.High, updatedAt = 4)
        val doneOld = item("done-old", completed = true, updatedAt = 10)
        val doneNew = item("done-new", completed = true, updatedAt = 20)

        assertEquals(
            listOf("high", "low", "tomorrow", "none", "done-new", "done-old"),
            sortTodoItems(listOf(doneOld, noDate, lowToday, doneNew, tomorrow, highToday)).map { it.id },
        )
    }

    private fun item(
        id: String,
        dueDate: LocalDate? = today,
        priority: TodoPriority = TodoPriority.Medium,
        completed: Boolean = false,
        updatedAt: Long = 1,
    ) = TodoItem(
        id = id,
        title = id,
        note = "",
        dueDate = dueDate,
        priority = priority,
        completed = completed,
        createdAt = 1,
        updatedAt = updatedAt,
        completedAt = if (completed) updatedAt else null,
    )
}
```

- [ ] **Step 2: Run the focused tests and confirm failure**

Run:

```powershell
.\gradlew.bat testDebugUnitTest --tests com.cypress.diary.todo.TodoFiltersTest --console=plain
```

Expected: compile failure for missing `TodoItem`, `TodoPriority`, `TodoFilter`, `sortTodoItems`, and `filterTodoItems`.

- [ ] **Step 3: Add model and pure helper implementation**

Create `TodoItem.kt`:

```kotlin
package com.cypress.diary.model.todo

import java.time.LocalDate

enum class TodoPriority(val label: String, val rank: Int) {
    Low("低", 0),
    Medium("中", 1),
    High("高", 2),
}

data class TodoItem(
    val id: String,
    val title: String,
    val note: String,
    val dueDate: LocalDate?,
    val priority: TodoPriority,
    val completed: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long?,
)
```

Create `TodoFilters.kt`:

```kotlin
package com.cypress.diary.todo

import com.cypress.diary.model.todo.TodoItem
import java.time.LocalDate

enum class TodoFilter(val label: String) {
    All("全部"),
    Today("今天"),
    Future("未来"),
    Completed("已完成"),
}

fun filterTodoItems(items: List<TodoItem>, filter: TodoFilter, selectedDate: LocalDate): List<TodoItem> {
    return when (filter) {
        TodoFilter.All -> items
        TodoFilter.Today -> items.filter { !it.completed && it.dueDate == selectedDate }
        TodoFilter.Future -> items.filter { !it.completed && (it.dueDate == null || it.dueDate > selectedDate) }
        TodoFilter.Completed -> items.filter { it.completed }
    }.let(::sortTodoItems)
}

fun sortTodoItems(items: List<TodoItem>): List<TodoItem> {
    return items.sortedWith(
        compareBy<TodoItem> { it.completed }
            .thenBy { if (it.completed) 1 else if (it.dueDate == null) 1 else 0 }
            .thenBy { it.dueDate ?: LocalDate.MAX }
            .thenByDescending { it.priority.rank }
            .thenByDescending { it.updatedAt },
    )
}
```

- [ ] **Step 4: Run focused tests and commit**

Run:

```powershell
.\gradlew.bat testDebugUnitTest --tests com.cypress.diary.todo.TodoFiltersTest --console=plain
```

Expected: PASS.

Commit:

```powershell
git add app/src/main/java/com/cypress/diary/model/todo/TodoItem.kt app/src/main/java/com/cypress/diary/todo/TodoFilters.kt app/src/test/java/com/cypress/diary/todo/TodoFiltersTest.kt
git commit -m "feat: add todo model and filters"
```

## Task 2: Todo Persistence

**Files:**
- Create: `app/src/main/java/com/cypress/diary/storage/TodoItemStore.kt`
- Test: `app/src/test/java/com/cypress/diary/storage/TodoItemStoreTest.kt`

- [ ] **Step 1: Write failing store tests**

Create tests for save/load, upsert, delete, and malformed line skipping. Use the same in-memory `PreferenceStore` pattern as `AccountingRecordStoreTest`.

- [ ] **Step 2: Implement `TodoItemStore`**

Implement a line-based store with `KEY_ITEMS = "todo_items"`, Base64 string escaping, nullable date/completedAt encoding as empty strings, and `sortTodoItems` applied after load and save.

- [ ] **Step 3: Run focused tests and commit**

Run:

```powershell
.\gradlew.bat testDebugUnitTest --tests com.cypress.diary.storage.TodoItemStoreTest --console=plain
```

Expected: PASS.

Commit:

```powershell
git add app/src/main/java/com/cypress/diary/storage/TodoItemStore.kt app/src/test/java/com/cypress/diary/storage/TodoItemStoreTest.kt
git commit -m "feat: persist todo items"
```

## Task 3: Navigation and Module Wiring

**Files:**
- Modify: `app/src/main/java/com/cypress/diary/ui/navigation/AppModule.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/navigation/DiaryRoute.kt`
- Modify tests: `app/src/test/java/com/cypress/diary/ui/navigation/DiaryRouteTest.kt`, `app/src/test/java/com/cypress/diary/storage/AppModuleStoreTest.kt`

- [ ] **Step 1: Extend tests first**

Add assertions that `AppModule.Todo` persists through `AppModuleStore`, and that `rootRoutesFor(AppModule.Todo)` returns `[TodoList, Profile]`.

- [ ] **Step 2: Implement route/module changes**

Add `Todo("待办")`, `TodoList("todo", "待办", Icons.Filled.CheckCircle)`, and `TodoEditor("todo_editor", "编辑待办", Icons.Filled.Edit)`.

- [ ] **Step 3: Run focused tests and commit**

Run:

```powershell
.\gradlew.bat testDebugUnitTest --tests com.cypress.diary.ui.navigation.DiaryRouteTest --tests com.cypress.diary.storage.AppModuleStoreTest --console=plain
```

Expected: PASS.

Commit:

```powershell
git add app/src/main/java/com/cypress/diary/ui/navigation/AppModule.kt app/src/main/java/com/cypress/diary/ui/navigation/DiaryRoute.kt app/src/test/java/com/cypress/diary/ui/navigation/DiaryRouteTest.kt app/src/test/java/com/cypress/diary/storage/AppModuleStoreTest.kt
git commit -m "feat: add todo navigation"
```

## Task 4: Todo Screens

**Files:**
- Create: `app/src/main/java/com/cypress/diary/ui/screens/TodoListScreen.kt`
- Create: `app/src/main/java/com/cypress/diary/ui/screens/TodoEditorScreen.kt`

- [ ] **Step 1: Add list screen**

Implement a Compose screen that accepts `items`, `selectedDate`, `selectedFilter`, `onFilterChange`, `onItemSelected`, `onToggleCompleted`, `refreshing`, and `onRefresh`. Use `RefreshableScreen`, filter chips, `Card`, and compact text.

- [ ] **Step 2: Add editor screen**

Implement a Compose screen that accepts nullable `item`, `selectedDate`, `onSave`, `onDelete`, and `onCancel`. Use text fields for title/note, priority chips, due-date buttons for none/today/tomorrow, and save/delete actions.

- [ ] **Step 3: Compile and commit**

Run:

```powershell
.\gradlew.bat :app:compileDebugKotlin --console=plain
```

Expected: PASS.

Commit:

```powershell
git add app/src/main/java/com/cypress/diary/ui/screens/TodoListScreen.kt app/src/main/java/com/cypress/diary/ui/screens/TodoEditorScreen.kt
git commit -m "feat: add todo screens"
```

## Task 5: App Integration

**Files:**
- Modify: `app/src/main/java/com/cypress/diary/DiaryApp.kt`

- [ ] **Step 1: Wire store and state**

Add `TodoItemStore`, `todoItems`, `selectedTodoItemId`, and `todoFilterName` state. Save/update/delete/toggle callbacks update both store and Compose state.

- [ ] **Step 2: Wire module selection and FAB behavior**

When `AppModule.Todo` is selected, route to `DiaryRoute.TodoList.route`. For Todo list FAB, clear `selectedTodoItemId` and route to `TodoEditor`.

- [ ] **Step 3: Wire route rendering**

Render `TodoListScreen` and `TodoEditorScreen` in the existing `when (route)` block. Include `isBottomRouteSelected` behavior for Todo list/editor.

- [ ] **Step 4: Compile and commit**

Run:

```powershell
.\gradlew.bat :app:compileDebugKotlin --console=plain
```

Expected: PASS.

Commit:

```powershell
git add app/src/main/java/com/cypress/diary/DiaryApp.kt
git commit -m "feat: wire todo module"
```

## Task 6: Docs, APK, and Final Verification

**Files:**
- Modify: `README.md`
- Replace: `release/dailylife.apk`

- [ ] **Step 1: Update README**

Add the Todo module to the feature list and keep APK-first install instructions intact.

- [ ] **Step 2: Run full test/build**

Run:

```powershell
.\gradlew.bat testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
```

Expected: both commands exit 0.

- [ ] **Step 3: Refresh APK**

Copy the debug APK:

```powershell
Copy-Item -LiteralPath .\app\build\outputs\apk\debug\app-debug.apk -Destination .\release\dailylife.apk -Force
```

- [ ] **Step 4: Commit and push**

Commit:

```powershell
git add README.md release/dailylife.apk
git commit -m "docs: update readme for todo module"
git push
```

- [ ] **Step 5: Update GitHub Release asset**

Delete and re-upload the `dailylife.apk` asset for `v1.0.0`:

```powershell
gh release delete-asset v1.0.0 dailylife.apk --repo cyprer/diary --yes
gh release upload v1.0.0 release/dailylife.apk --repo cyprer/diary
```

- [ ] **Step 6: Verify remote release and status**

Run:

```powershell
gh release view v1.0.0 --repo cyprer/diary --json tagName,url,assets
git status --short --branch
```

Expected: release has `dailylife.apk`, and branch is clean/synced.
