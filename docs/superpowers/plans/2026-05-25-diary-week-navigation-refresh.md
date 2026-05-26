# Diary Week Navigation and Manual Refresh Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add month-local previous/next week navigation to diary week mode and stop automatic remote refresh on app startup.

**Architecture:** Keep selected date as the calendar state contract. Add pure date helpers beside the existing calendar helpers, wire week mode arrows to those helpers, and guard the refresh effect so it only runs after explicit user refresh.

**Tech Stack:** Kotlin, Jetpack Compose, JUnit 4, Gradle Android plugin.

---

### Task 1: Month-Local Week Date Helpers

**Files:**
- Modify: `app/src/test/java/com/cypress/diary/ui/calendar/CalendarMonthTest.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/calendar/CalendarMonthPicker.kt`

- [ ] **Step 1: Write failing tests**

Add these tests to `CalendarMonthTest`:

```kotlin
@Test
fun previousWeekCrossesFromFirstWeekToPreviousMonthLastWeek() {
    assertEquals(
        LocalDate.of(2026, 4, 29),
        previousMonthLocalWeekDate(LocalDate.of(2026, 5, 1)),
    )
}

@Test
fun previousWeekClampsOffsetWhenTargetWeekIsShorter() {
    assertEquals(
        LocalDate.of(2026, 4, 30),
        previousMonthLocalWeekDate(LocalDate.of(2026, 5, 3)),
    )
}

@Test
fun nextWeekPreservesOffsetAcrossMonthBoundary() {
    assertEquals(
        LocalDate.of(2026, 5, 2),
        nextMonthLocalWeekDate(LocalDate.of(2026, 4, 30)),
    )
}

@Test
fun weekNavigationCrossesYearBoundary() {
    assertEquals(
        LocalDate.of(2025, 12, 29),
        previousMonthLocalWeekDate(LocalDate.of(2026, 1, 1)),
    )
    assertEquals(
        LocalDate.of(2027, 1, 1),
        nextMonthLocalWeekDate(LocalDate.of(2026, 12, 29)),
    )
}
```

- [ ] **Step 2: Run tests and verify they fail**

Run: `.\gradlew.bat testDebugUnitTest --tests com.cypress.diary.ui.calendar.CalendarMonthTest`

Expected: compilation fails because `previousMonthLocalWeekDate` and `nextMonthLocalWeekDate` do not exist.

- [ ] **Step 3: Implement helpers**

Add to `CalendarMonthPicker.kt` near the other calendar helper functions:

```kotlin
fun previousMonthLocalWeekDate(date: LocalDate): LocalDate {
    return date.plusMonthLocalWeeks(-1)
}

fun nextMonthLocalWeekDate(date: LocalDate): LocalDate {
    return date.plusMonthLocalWeeks(1)
}

private fun LocalDate.plusMonthLocalWeeks(delta: Int): LocalDate {
    val month = YearMonth.from(this)
    val weekIndex = monthLocalWeekIndex(dayOfMonth)
    val weekStartDay = monthLocalWeekStartDay(weekIndex)
    val offset = dayOfMonth - weekStartDay
    val targetMonth = when {
        delta < 0 && weekIndex == 1 -> month.minusMonths(1)
        delta > 0 && weekIndex == monthLocalWeekCount(month) -> month.plusMonths(1)
        else -> month
    }
    val targetWeekIndex = when {
        delta < 0 && weekIndex == 1 -> monthLocalWeekCount(targetMonth)
        delta > 0 && weekIndex == monthLocalWeekCount(month) -> 1
        else -> weekIndex + delta
    }
    val targetStartDay = monthLocalWeekStartDay(targetWeekIndex)
    val targetEndDay = minOf(targetStartDay + 6, targetMonth.lengthOfMonth())
    val targetDay = minOf(targetStartDay + offset, targetEndDay)
    return LocalDate.of(targetMonth.year, targetMonth.monthValue, targetDay)
}

private fun monthLocalWeekIndex(dayOfMonth: Int): Int {
    return ((dayOfMonth - 1) / 7) + 1
}

private fun monthLocalWeekStartDay(weekIndex: Int): Int {
    return ((weekIndex - 1) * 7) + 1
}

private fun monthLocalWeekCount(month: YearMonth): Int {
    return monthLocalWeekIndex(month.lengthOfMonth())
}
```

- [ ] **Step 4: Run tests and verify they pass**

Run: `.\gradlew.bat testDebugUnitTest --tests com.cypress.diary.ui.calendar.CalendarMonthTest`

Expected: `CalendarMonthTest` passes.

### Task 2: Wire Week Arrows to Week Navigation

**Files:**
- Modify: `app/src/main/java/com/cypress/diary/ui/calendar/CalendarMonthPicker.kt`

- [ ] **Step 1: Replace week mode arrow handlers**

In `CalendarWeekPicker`, change the left arrow from:

```kotlin
IconButton(onClick = { onDateSelected(selectedDate.plusMonthsClamped(-1)) }) {
```

to:

```kotlin
IconButton(onClick = { onDateSelected(previousMonthLocalWeekDate(selectedDate)) }) {
```

Change the right arrow from:

```kotlin
IconButton(onClick = { onDateSelected(selectedDate.plusMonthsClamped(1)) }) {
```

to:

```kotlin
IconButton(onClick = { onDateSelected(nextMonthLocalWeekDate(selectedDate)) }) {
```

- [ ] **Step 2: Run calendar tests**

Run: `.\gradlew.bat testDebugUnitTest --tests com.cypress.diary.ui.calendar.CalendarMonthTest`

Expected: tests pass.

### Task 3: Stop Startup Remote Fetch

**Files:**
- Modify: `app/src/main/java/com/cypress/diary/DiaryApp.kt`

- [ ] **Step 1: Guard refresh effect**

In `DiaryApp.kt`, change:

```kotlin
LaunchedEffect(githubConfig, refreshVersion) {
    refreshing = true
```

to:

```kotlin
LaunchedEffect(refreshVersion) {
    if (refreshVersion == 0) {
        return@LaunchedEffect
    }
    refreshing = true
```

This preserves `refreshWeeks()` as the only trigger for remote fetch. Updating GitHub config no longer fetches immediately; the user can pull to refresh.

- [ ] **Step 2: Run unit tests**

Run: `.\gradlew.bat testDebugUnitTest`

Expected: all unit tests pass.

### Task 4: Build Verification

**Files:**
- No source edits.

- [ ] **Step 1: Build debug APK**

Run: `.\gradlew.bat :app:assembleDebug`

Expected: build succeeds.
