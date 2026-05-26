# Diary UI Simplification and Theme Switch Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Simplify the diary screen into a date-first daily view with independent year/month/day selection, and add a profile-page theme color switcher.

**Architecture:** Keep the existing single-activity Compose shell, but move the selected date into a small reusable state/helper so the header can change year, month, and day independently. The diary screen will render only the chosen day’s body text. Theme colors become a small palette enum in `ui/theme`, so `DiaryTheme` can swap the app accent and surface colors from one selected palette without changing navigation or data flow.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, JUnit4, existing diary model/parser code.

---

### Task 1: Add date-selection helpers and regression tests

**Files:**
- Create: `app/src/main/java/com/cypress/diary/ui/state/DateSelectionState.kt`
- Create: `app/src/test/java/com/cypress/diary/ui/state/DateSelectionStateTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
class DateSelectionStateTest {
    @Test
    fun changingYearOrMonthKeepsTheSelectedDayWhenPossible() {
        val state = DateSelectionState(LocalDate.of(2026, 5, 24))

        assertEquals(LocalDate.of(2026, 5, 24), state.currentDate)
        assertEquals(LocalDate.of(2026, 6, 24), state.withMonth(6).currentDate)
        assertEquals(LocalDate.of(2025, 6, 24), state.withYear(2025).currentDate)
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*DateSelectionStateTest"`
Expected: FAIL because `DateSelectionState` does not exist yet.

- [ ] **Step 3: Write the minimal implementation**

```kotlin
data class DateSelectionState(val currentDate: LocalDate) {
    fun withYear(year: Int): DateSelectionState = copy(currentDate = currentDate.withYear(year))
    fun withMonth(month: Int): DateSelectionState = copy(currentDate = currentDate.withMonth(month))
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*DateSelectionStateTest"`
Expected: PASS.

---

### Task 2: Replace the diary header with year/month/day selectors and daily-only content

**Files:**
- Modify: `app/src/main/java/com/cypress/diary/DiaryApp.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/screens/DiaryScreen.kt`
- Create: `app/src/main/java/com/cypress/diary/ui/components/DateSelectorRow.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
class DateSelectorRowTest {
    @Test
    fun formatsYearMonthDayForDisplay() {
        val label = formatDateParts(LocalDate.of(2026, 5, 24))
        assertEquals("2026 / 5 / 24", label)
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*DateSelectorRowTest"`
Expected: FAIL because the formatting helper does not exist yet.

- [ ] **Step 3: Implement the selector row and daily-only rendering**

```kotlin
@Composable
fun DateSelectorRow(
    date: LocalDate,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDayChange: (Int) -> Unit,
) {
    Row {
        // three dropdowns: year, month, day
    }
}
```

```kotlin
@Composable
fun DiaryScreen(
    date: LocalDate,
    body: String?,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDayChange: (Int) -> Unit,
) {
    // top selector row + previous/next buttons
    // render only the current day's text body below it
}
```

- [ ] **Step 4: Run the tests and build**

Run: `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug`
Expected: PASS.

---

### Task 3: Add profile-page theme palette switching

**Files:**
- Create: `app/src/main/java/com/cypress/diary/ui/theme/ThemePalette.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/theme/Color.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/theme/Theme.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/screens/ProfileScreen.kt`
- Modify: `app/src/main/java/com/cypress/diary/DiaryApp.kt`
- Create: `app/src/test/java/com/cypress/diary/ui/theme/ThemePaletteTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
class ThemePaletteTest {
    @Test
    fun palettesExposeDistinctAccentColors() {
        val colors = ThemePalette.entries.map { it.primary }
        assertEquals(colors.distinct().size, colors.size)
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*ThemePaletteTest"`
Expected: FAIL because `ThemePalette` does not exist yet.

- [ ] **Step 3: Implement the palette model and selector UI**

```kotlin
enum class ThemePalette(val label: String, val primary: Color, val secondary: Color) {
    BlueGray("蓝灰", Color(0xFF4D6B8A), Color(0xFFD9E3F1)),
    Mint("薄荷绿", Color(0xFF3F6652), Color(0xFFD6E8DA)),
    Lavender("浅紫", Color(0xFF6B5B95), Color(0xFFE6DFF7)),
    Graphite("深灰", Color(0xFF4A4F57), Color(0xFFDDE0E5))
}
```

```kotlin
@Composable
fun ProfileScreen(onPaletteSelected: (ThemePalette) -> Unit, selectedPalette: ThemePalette) {
    // show swatches, current palette, and a simple toggle row
}
```

```kotlin
@Composable
fun DiaryTheme(palette: ThemePalette, content: @Composable () -> Unit) {
    // build Material 3 colors from the selected palette
}
```

- [ ] **Step 4: Run the tests and assemble**

Run: `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug`
Expected: PASS.

---

### Task 4: Verify on device

**Files:**
- None

- [ ] **Step 1: Install the rebuilt APK**

Run: `adb install -r app/build/outputs/apk/debug/app-debug.apk`

- [ ] **Step 2: Launch and confirm**

Run: `adb shell am start -W -n com.cypress.diary/.MainActivity`

- [ ] **Step 3: Check crash logs if needed**

Run: `adb logcat -d -s AndroidRuntime`

