# Diary Refresh, Push, and Background Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix the diary app UI so every page can scroll and pull to refresh, make the summary tree start collapsed, change editor saving into local autosave plus remote push, and add a persistent background-image picker on the profile page.

**Architecture:** Keep the single-activity Compose shell and the existing GitHub config store, but split new state into small focused stores: one for appearance settings, one for editor drafts. Wrap each screen in a shared pull-to-refresh container so refresh behavior is consistent. The editor will own only form state; the app layer will coordinate local draft persistence and GitHub push calls, including week-file rebuilds from the existing markdown codec. Background images will be stored as URI strings and rendered behind the app content with a lightweight Compose image loader.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, `androidx.compose.material:material` pull-refresh APIs, Activity Result contracts, SharedPreferences, existing GitHub API code, existing markdown codec, JUnit4.

---

### Task 1: Add persistent appearance storage and background rendering

**Files:**
- Create: `app/src/main/java/com/cypress/diary/storage/AppAppearanceStore.kt`
- Create: `app/src/main/java/com/cypress/diary/ui/background/BackgroundLayer.kt`
- Modify: `app/src/main/java/com/cypress/diary/DiaryApp.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/screens/ProfileScreen.kt`
- Create: `app/src/test/java/com/cypress/diary/storage/AppAppearanceStoreTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
class AppAppearanceStoreTest {
    @Test
    fun savesAndLoadsPaletteAndBackgroundUri() {
        val store = AppAppearanceStore(context)
        store.save(ThemePalette.Mint.name, "content://example/image")

        val state = store.load()

        assertEquals(ThemePalette.Mint.name, state.paletteName)
        assertEquals("content://example/image", state.backgroundUri)
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*AppAppearanceStoreTest"`
Expected: FAIL because the store does not exist yet.

- [ ] **Step 3: Implement the appearance store and background layer**

```kotlin
data class AppAppearanceState(
    val paletteName: String,
    val backgroundUri: String?,
)

class AppAppearanceStore(context: Context) {
    fun load(): AppAppearanceState
    fun save(paletteName: String, backgroundUri: String?)
}
```

```kotlin
@Composable
fun BackgroundLayer(backgroundUri: String?, content: @Composable () -> Unit) {
    // Load a bitmap from the stored URI and draw it behind the app content.
}
```

`ProfileScreen` gets a background module with an image-picker button and a clear button. `DiaryApp` reads the saved URI and wraps the app shell in `BackgroundLayer`.

- [ ] **Step 4: Re-run the test**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*AppAppearanceStoreTest"`
Expected: PASS.

---

### Task 2: Add pull-to-refresh and fix overflow on every screen

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/java/com/cypress/diary/DiaryApp.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/screens/DiaryScreen.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/screens/SummaryScreen.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/screens/ProfileScreen.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/screens/EditorScreen.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/components/WeekTree.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
class WeekTreeDefaultsTest {
    @Test
    fun collapsedByDefaultShowsOnlyTopLevelYears() {
        val policy = TreeExpansionPolicy.defaultCollapsed()

        assertFalse(policy.isExpanded("year-2025"))
        assertFalse(policy.isExpanded("month-2025-1"))
        assertFalse(policy.isExpanded("week-2025-1-1"))
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*WeekTreeDefaultsTest"`
Expected: FAIL because the collapse policy does not exist yet.

- [ ] **Step 3: Implement refresh containers and collapse policy**

```kotlin
class TreeExpansionPolicy {
    fun isExpanded(key: String): Boolean
    companion object { fun defaultCollapsed(): TreeExpansionPolicy }
}
```

Use `androidx.compose.material.pullrefresh.PullRefreshIndicator` and `Modifier.pullRefresh(...)` around each screen root. Wrap the root `Column` in `verticalScroll(...)` so the profile page, editor page, and summary page can scroll all the way to the bottom on small screens.

Make the summary tree start fully collapsed by default, with expansion only after tapping a year, month, or week row.

- [ ] **Step 4: Re-run the test and assemble**

Run: `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug`
Expected: PASS.

---

### Task 3: Add local editor draft autosave and GitHub push

**Files:**
- Create: `app/src/main/java/com/cypress/diary/storage/EditorDraftStore.kt`
- Modify: `app/src/main/java/com/cypress/diary/github/GitHubDiaryRepository.kt`
- Modify: `app/src/main/java/com/cypress/diary/DiaryApp.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/screens/EditorScreen.kt`
- Create: `app/src/main/java/com/cypress/diary/model/DiaryWeekExtensions.kt`
- Create: `app/src/test/java/com/cypress/diary/storage/EditorDraftStoreTest.kt`
- Create: `app/src/test/java/com/cypress/diary/model/DiaryWeekExtensionsTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
class EditorDraftStoreTest {
    @Test
    fun keepsDistinctDraftsForDayAndWeekModes() {
        val store = EditorDraftStore(context)
        store.save("src/content/posts/summary/25year/5month/4week.md#day-2026-05-24", "day")
        store.save("src/content/posts/summary/25year/5month/4week.md#week", "week")

        assertEquals("day", store.load("src/content/posts/summary/25year/5month/4week.md#day-2026-05-24"))
        assertEquals("week", store.load("src/content/posts/summary/25year/5month/4week.md#week"))
    }
}
```

```kotlin
class DiaryWeekExtensionsTest {
    @Test
    fun replacesOnlyOneDayWhenEditingDayMode() {
        val updated = original.withDayContent(LocalDate.of(2026, 5, 24), "new text")
        assertEquals("new text", updated.days.first { it.date == LocalDate.of(2026, 5, 24) }.content)
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*EditorDraftStoreTest" --tests "*DiaryWeekExtensionsTest"`
Expected: FAIL because the store and helper do not exist yet.

- [ ] **Step 3: Implement autosave and push**

```kotlin
fun DiaryWeek.withDayContent(date: LocalDate, content: String): DiaryWeek
```

```kotlin
suspend fun GitHubDiaryRepository.saveWeek(config: GitHubConfig, path: String, markdown: String)
```

`EditorScreen` becomes stateless: the app layer owns the current draft text, saves it to `EditorDraftStore` on every change, and changes the bottom-right button label from `保存` to `推送`. Pushing in day mode rebuilds the whole week file with the edited day inserted. If the week file does not exist, the app creates a new one using the existing markdown codec.

- [ ] **Step 4: Re-run the tests and build**

Run: `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug`
Expected: PASS.

---

### Task 4: Verify the device flow and UI behavior on phone

**Files:**
- None

- [ ] **Step 1: Install the rebuilt APK**

Run: `adb install -r app/build/outputs/apk/debug/app-debug.apk`

- [ ] **Step 2: Launch the app and check the key flows**

Run: `adb shell am start -W -n com.cypress.diary/.MainActivity`

- [ ] **Step 3: Confirm the target behaviors**

Check that:
- the diary, summary, profile, and editor pages all scroll
- pull-to-refresh is visible on each page
- summary starts collapsed
- editor shows `推送` instead of `保存`
- the left-bottom `未保存` text is gone
- profile can pick and clear a background image

