# Diary UI Refinement Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn the diary page into a date-first daily reader with independent year/month/day selection, and add a simple theme color switcher on the profile page.

**Architecture:** Keep the single-activity Compose shell and reuse the existing diary data model. Move date selection into a small calendar state helper so the header can clamp day values when year or month changes. Simplify the diary body to show only the selected day’s content. Add a small fixed palette enum in `ui/theme` so the app theme can switch accent colors without changing navigation or data flow.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, JUnit4, existing `DiaryWeek` / `DiaryDay` model classes.

---

### Task 1: Add date-selection math and tests

**Files:**
- Create: `app/src/main/java/com/cypress/diary/ui/state/DateSelectionState.kt`
- Create: `app/src/test/java/com/cypress/diary/ui/state/DateSelectionStateTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
class DateSelectionStateTest {
    @Test
    fun changingYearOrMonthKeepsTheDayWhenItExists() {
        val state = DateSelectionState(LocalDate.of(2026, 5, 24))

        assertEquals(LocalDate.of(2026, 6, 24), state.withMonth(6).currentDate)
        assertEquals(LocalDate.of(2025, 6, 24), state.withYear(2025).currentDate)
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*DateSelectionStateTest"`
Expected: FAIL because `DateSelectionState` does not exist yet.

- [ ] **Step 3: Implement the minimal helper**

```kotlin
data class DateSelectionState(val currentDate: LocalDate) {
    fun withYear(year: Int): DateSelectionState = copy(currentDate = currentDate.withYear(year))
    fun withMonth(month: Int): DateSelectionState = copy(currentDate = currentDate.withMonth(month))
}
```

- [ ] **Step 4: Re-run the test**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*DateSelectionStateTest"`
Expected: PASS.

---

### Task 2: Replace the diary header with year/month/day dropdowns and simple day text

**Files:**
- Modify: `app/src/main/java/com/cypress/diary/DiaryApp.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/screens/DiaryScreen.kt`
- Create: `app/src/main/java/com/cypress/diary/ui/components/DatePickerHeader.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/components/DiaryCard.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
class DateSelectionMathTest {
    @Test
    fun clampsDayWhenSwitchingToShorterMonth() {
        assertEquals(28, clampDay(2026, 2, 31))
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*DateSelectionMathTest"`
Expected: FAIL because `clampDay` does not exist yet.

- [ ] **Step 3: Implement the header and day-only diary rendering**

```kotlin
fun clampDay(year: Int, month: Int, day: Int): Int {
    val maxDay = YearMonth.of(year, month).lengthOfMonth()
    return min(day, maxDay)
}
```

```kotlin
@Composable
fun DatePickerHeader(
    date: LocalDate,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDayChange: (Int) -> Unit,
) {
    // three dropdowns: year, month, day
}
```

```kotlin
@Composable
fun DiaryCard(body: String?, modifier: Modifier = Modifier) {
    // only render the current day's text body
}
```

```kotlin
@Composable
fun DiaryScreen(...) {
    // header row + one simple text card
}
```

- [ ] **Step 4: Re-run the build**

Run: `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug`
Expected: PASS.

---

### Task 3: Add theme palette switching on the profile page

**Files:**
- Create: `app/src/main/java/com/cypress/diary/ui/theme/ThemePalette.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/theme/Theme.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/theme/Color.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/screens/ProfileScreen.kt`
- Modify: `app/src/main/java/com/cypress/diary/DiaryApp.kt`
- Create: `app/src/test/java/com/cypress/diary/ui/theme/ThemePaletteTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
class ThemePaletteTest {
    @Test
    fun exposesDistinctAccentPalettes() {
        assertEquals(4, ThemePalette.entries.size)
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*ThemePaletteTest"`
Expected: FAIL because `ThemePalette` does not exist yet.

- [ ] **Step 3: Implement palette selection and theme wiring**

```kotlin
enum class ThemePalette(val label: String, val primary: Color, val secondary: Color) {
    BlueGray("蓝灰", Color(0xFF4D6B8A), Color(0xFFD9E3F1)),
    Mint("薄荷绿", Color(0xFF3F6652), Color(0xFFD6E8DA)),
    Lavender("浅紫", Color(0xFF6B5B95), Color(0xFFE6DFF7)),
    Graphite("深灰", Color(0xFF4A4F57), Color(0xFFDDE0E5)),
}
```

```kotlin
@Composable
fun DiaryTheme(palette: ThemePalette, content: @Composable () -> Unit) {
    // Build a Material 3 color scheme from the selected palette.
}
```

```kotlin
@Composable
fun ProfileScreen(selectedPalette: ThemePalette, onPaletteSelected: (ThemePalette) -> Unit) {
    // show palette chips / swatches
}
```

- [ ] **Step 4: Re-run the build**

Run: `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug`
Expected: PASS.

---

### Task 4: Verify on device

**Files:**
- None

- [ ] **Step 1: Install the rebuilt APK**

Run: `adb install -r app/build/outputs/apk/debug/app-debug.apk`

- [ ] **Step 2: Launch the app**

Run: `adb shell am start -W -n com.cypress.diary/.MainActivity`

- [ ] **Step 3: Confirm there is no new AndroidRuntime crash**

Run: `adb logcat -d -s AndroidRuntime`

