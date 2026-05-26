# Diary

A personal diary Android app with **Markdown journaling** and **accounting (expense/income tracker)** modules, built with Jetpack Compose.

## Features

### Diary Module
- **Daily journal** — write Markdown-formatted diary entries for each day
- **Calendar navigation** — year/month/week pickers with filter chips
- **Editor** — day and week editing modes with auto-saved drafts
- **Full-text search** — search across diary entries
- **Summary documents** — yearly, monthly, weekly summaries with word-count charts
- **Daily quote** — fetches from Hitokoto API
- **GitHub sync** (hidden) — optional sync with a GitHub repo, unlocked via easter egg
- **Export/Import** — `.diary` ZIP archives for manual migration

### Accounting Module
- **Ledger** — view income/expense records with calendar picker
- **Record editor** — add/edit/delete records with categories
- **Stats** — weekly/monthly/yearly charts with category breakdowns
- **Export/Import** — `.accounting` ZIP archives with merge-or-replace on import

### Appearance
- 4 theme palettes (BlueGray, Mint, Lavender, Graphite)
- Custom background image
- Layout opacity adjustment

## Tech Stack

| Layer | Technology |
|---|---|
| Language | **Kotlin** 1.9.24 |
| UI | **Jetpack Compose** + Material 3 |
| Navigation | navigation-compose 2.7.7 |
| Async | Kotlin Coroutines 1.9.0 |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 34 |
| Build | Gradle (Kotlin DSL), AGP 8.4.2 |

## Build & Run

```powershell
# Run unit tests
.\gradlew.bat testDebugUnitTest --console=plain

# Build debug APK
.\gradlew.bat :app:assembleDebug --console=plain

# Install on device
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## Project Structure

```
app/src/main/java/com/cypress/diary/
├── MainActivity.kt            — Android entry point
├── DiaryApp.kt                — Compose root app
├── model/                     — DiaryWeek, DiaryDay, WeekKey, accounting models
├── parser/                    — Markdown codec, document codec, path resolver
├── github/                    — GitHub API client & config (hidden feature)
├── storage/                   — SharedPreferences caches, drafts, settings
├── accounting/                — Amount parsing, stats logic
├── export/                    — .diary / .accounting ZIP export/import
├── quote/                     — Hitokoto daily quote API
├── ui/
│   ├── screens/               — Diary, Summary, Editor, Profile, Accounting screens
│   ├── calendar/              — Year/Month/Week pickers
│   ├── editor/                — Editor & draft merge logic
│   ├── summary/               — Summary tree & week day filtering
│   ├── components/            — Shared UI components
│   └── navigation/            — Routes & module definitions
```

## License

MIT
