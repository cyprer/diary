# Diary Sync, Refresh, and Background Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the diary app feel complete on Android: every main page can pull to refresh, the summary tree starts collapsed, editor changes are saved locally by default and can be pushed to GitHub, and the profile page can choose a persistent background image.

**Architecture:** Keep the single-activity Compose shell, but add two small persistence layers: one for UI appearance (`ThemePalette` plus background image URI) and one for editor drafts keyed by file path and edit mode. Put pull-to-refresh and page scrolling into reusable screen wrappers so diary, summary, profile, and editor pages stay consistent. For GitHub writes, reuse the existing repository layer and add a focused Markdown merge/update helper so day edits and week edits both end up as one rewritten summary file.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, `androidx.compose.material:material` for pull refresh, `SharedPreferences`, existing GitHub HttpURLConnection layer, JUnit4.

---

### Task 1: Add persistent appearance and draft stores

**Files:**
- Create: `app/src/main/java/com/cypress/diary/ui/state/DiaryAppearanceStore.kt`
- Create: `app/src/main/java/com/cypress/diary/ui/state/DiaryDraftStore.kt`
- Create: `app/src/test/java/com/cypress/diary/ui/state/DiaryAppearanceStoreTest.kt`
- Create: `app/src/test/java/com/cypress/diary/ui/state/DiaryDraftStoreTest.kt`

- [ ] **Step 1: Write the failing tests**

```kotlin
class DiaryAppearanceStoreTest {
    @Test
    fun savesAndLoadsPaletteAndBackgroundUri() {
        val store = DiaryAppearanceStore(context)
        store.save(ThemePalette.Mint.name, "content://media/external/images/media/42")

        val loaded = store.load()

        assertEquals(ThemePalette.Mint.name, loaded.paletteName)
        assertEquals("content://media/external/images/media/42", loaded.backgroundUri)
    }
}
```

```kotlin
class DiaryDraftStoreTest {
    @Test
    fun keepsDraftsSeparateByKey() {
        val store = DiaryDraftStore(context)
        store.save("src/content/posts/summary/25year/5month/4week.md|day|2026-05-24", "alpha")
        store.save("src/content/posts/summary/25year/5month/4week.md|week", "beta")

        assertEquals("alpha", store.load("src/content/posts/summary/25year/5month/4week.md|day|2026-05-24"))
        assertEquals("beta", store.load("src/content/posts/summary/25year/5month/4week.md|week"))
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*DiaryAppearanceStoreTest" --tests "*DiaryDraftStoreTest"`
Expected: FAIL because the stores do not exist yet.

- [ ] **Step 3: Implement the stores**

```kotlin
data class DiaryAppearance(
    val paletteName: String,
    val backgroundUri: String,
)
```

```kotlin
class DiaryAppearanceStore(context: Context) {
    fun load(): DiaryAppearance
    fun save(paletteName: String, backgroundUri: String)
}
```

```kotlin
class DiaryDraftStore(context: Context) {
    fun load(key: String): String?
    fun save(key: String, value: String)
    fun clear(key: String)
}
```

- [ ] **Step 4: Re-run the tests**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*DiaryAppearanceStoreTest" --tests "*DiaryDraftStoreTest"`
Expected: PASS.

---

### Task 2: Add reusable pull-to-refresh and fix page scrolling

**Files:**
- Modify: `app/build.gradle.kts`
- Create: `app/src/main/java/com/cypress/diary/ui/components/RefreshableScreen.kt`
- Create: `app/src/main/java/com/cypress/diary/ui/components/AppBackground.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/screens/DiaryScreen.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/screens/SummaryScreen.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/screens/ProfileScreen.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/screens/EditorScreen.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/components/WeekTree.kt`
- Modify: `app/src/main/java/com/cypress/diary/DiaryApp.kt`

- [ ] **Step 1: Add the pull-refresh dependency**

```kotlin
implementation("androidx.compose.material:material")
```

- [ ] **Step 2: Implement the reusable screen wrapper**

```kotlin
@Composable
fun RefreshableScreen(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
)
```

- [ ] **Step 3: Wrap each page**
  - Diary page: vertical scroll + pull-to-refresh
  - Summary page: vertical scroll + pull-to-refresh
  - Profile page: vertical scroll + pull-to-refresh
  - Editor page: vertical scroll + pull-to-refresh
  - Summary tree: no auto-expanded year/month/week nodes on first render

- [ ] **Step 4: Wire refresh back into `DiaryApp`**
  - Reuse the current remote load logic
  - Refresh should reload GitHub weeks when connected
  - Refresh should fall back to sample weeks when not connected

- [ ] **Step 5: Rebuild and check the UI**

Run: `.\gradlew.bat :app:assembleDebug`
Expected: build succeeds and the pages no longer clip content off-screen.

---

### Task 3: Make editor changes save locally first and push to GitHub explicitly

**Files:**
- Modify: `app/src/main/java/com/cypress/diary/ui/screens/EditorScreen.kt`
- Modify: `app/src/main/java/com/cypress/diary/DiaryApp.kt`
- Modify: `app/src/main/java/com/cypress/diary/github/GitHubDiaryRepository.kt`
- Create: `app/src/main/java/com/cypress/diary/ui/editor/DiaryEditContentBuilder.kt`
- Create: `app/src/test/java/com/cypress/diary/ui/editor/DiaryEditContentBuilderTest.kt`
- Create: `app/src/test/java/com/cypress/diary/github/GitHubDiaryRepositoryWriteTest.kt`

- [ ] **Step 1: Write the failing tests**

```kotlin
class DiaryEditContentBuilderTest {
    @Test
    fun updatesOnlyTheSelectedDayInsideAWeek() {
        val updated = DiaryEditContentBuilder().updateDayContent(week, LocalDate.of(2026, 5, 24), "new body")
        assertTrue(updated.days.first { it.date == LocalDate.of(2026, 5, 24) }.content.contains("new body"))
    }
}
```

```kotlin
class GitHubDiaryRepositoryWriteTest {
    @Test
    fun buildsUpsertPayloadWithEncodedContent() {
        val payload = GitHubDiaryRepositoryWriteRequest.build(
            path = "src/content/posts/summary/25year/5month/4week.md",
            markdown = "# title",
            sha = "abc123",
        )
        assertEquals("abc123", payload.getString("sha"))
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*DiaryEditContentBuilderTest" --tests "*GitHubDiaryRepositoryWriteTest"`
Expected: FAIL because the merge helper and write payload helper do not exist yet.

- [ ] **Step 3: Implement the editor flow**
  - Day mode stores only the selected day body locally
  - Week mode stores the whole Markdown file locally
  - The bottom-right button becomes `推送`
  - Local draft is saved as the user types
  - Push reads the current local draft, merges if needed, and writes the summary file back to GitHub
  - If the remote file does not exist yet, create it

- [ ] **Step 4: Implement the GitHub write call**
  - Fetch existing file SHA when updating
  - PUT the rewritten Markdown to the same repo path
  - Keep the existing read path and public read fallback intact

- [ ] **Step 5: Re-run the tests and assemble**

Run: `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug`
Expected: PASS.

---

### Task 4: Add persistent background image selection on the profile page

**Files:**
- Modify: `app/src/main/java/com/cypress/diary/ui/screens/ProfileScreen.kt`
- Modify: `app/src/main/java/com/cypress/diary/DiaryApp.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/theme/Theme.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/theme/ThemePalette.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/theme/Color.kt`
- Create: `app/src/test/java/com/cypress/diary/ui/state/DiaryAppearanceStoreRoundTripTest.kt`

- [ ] **Step 1: Add the image picker flow**

```kotlin
val picker = rememberLauncherForActivityResult(OpenDocument()) { uri ->
    if (uri != null) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )
        onBackgroundSelected(uri.toString())
    }
}
```

- [ ] **Step 2: Render the background image behind the app**
  - Load the saved URI if one exists
  - Draw it behind the Scaffold content
  - Keep a soft scrim so text stays readable

- [ ] **Step 3: Persist the selected palette and background**
  - Profile page writes both values through `DiaryAppearanceStore`
  - `DiaryApp` restores them on launch

- [ ] **Step 4: Re-run the build**

Run: `.\gradlew.bat :app:assembleDebug`
Expected: build succeeds and the chosen background survives app restarts.

---

### Task 5: Verify on the device

**Files:**
- None

- [ ] **Step 1: Install the rebuilt APK**

Run: `adb install -r app/build/outputs/apk/debug/app-debug.apk`

- [ ] **Step 2: Launch the app**

Run: `adb shell am start -W -n com.cypress.diary/.MainActivity`

- [ ] **Step 3: Confirm the key flows**
  - Profile page scrolls fully
  - Summary tree starts collapsed
  - Pull-to-refresh appears on all pages
  - Editor button says `推送`
  - Background image can be selected and stays after restart

