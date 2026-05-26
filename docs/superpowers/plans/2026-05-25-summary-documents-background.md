# Summary Documents and Background Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the Summary page load year/month/week Markdown documents, render selected documents for reading, edit selected Markdown on demand, and fix visible background images.

**Architecture:** Add a generic `DiaryDocument` model and codec for summary documents while keeping `DiaryWeek` for daily diary behavior. Extend GitHub loading and local cache to work by document path, then rebuild the Summary UI around selecting documents instead of expanding weeks into days.

**Tech Stack:** Kotlin, Jetpack Compose, JUnit 4, Android SharedPreferences, GitHub Contents API.

---

### Task 1: Summary Document Model, Paths, Codec, Cache

**Files:**
- Create: `app/src/main/java/com/cypress/diary/model/DiaryDocument.kt`
- Create: `app/src/main/java/com/cypress/diary/parser/DiaryDocumentCodec.kt`
- Create: `app/src/main/java/com/cypress/diary/storage/DiaryDocumentCacheStore.kt`
- Modify: `app/src/main/java/com/cypress/diary/github/GitHubDiaryPaths.kt`
- Test: `app/src/test/java/com/cypress/diary/github/GitHubPathMatcherTest.kt`
- Test: `app/src/test/java/com/cypress/diary/parser/DiaryDocumentCodecTest.kt`
- Test: `app/src/test/java/com/cypress/diary/storage/DiaryDocumentCacheStoreTest.kt`

- [ ] Write failing tests for year/month/week summary path matching and sorting.
- [ ] Write failing tests for parsing title, published date, body, and raw markdown from frontmatter documents.
- [ ] Write failing tests for cache round trip by path.
- [ ] Implement `DiaryDocument`, path matching, codec, and cache.
- [ ] Run focused tests and commit.

### Task 2: GitHub Document Loading

**Files:**
- Modify: `app/src/main/java/com/cypress/diary/github/GitHubDiaryRepository.kt`
- Test: `app/src/test/java/com/cypress/diary/github/GitHubPathMatcherTest.kt`

- [ ] Write failing tests for candidate document paths and document sort order.
- [ ] Add `loadDocuments(config)` that includes year `index.md`, month `index.md`, and `Nweek.md`.
- [ ] Keep `saveWeek(config, path, markdown)` as the existing push method; do not rewrite push.
- [ ] Run focused tests and commit.

### Task 3: Summary Tree and Reader UI

**Files:**
- Modify: `app/src/main/java/com/cypress/diary/ui/summary/SummaryTreeBuilder.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/components/WeekTree.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/screens/SummaryScreen.kt`
- Create: `app/src/main/java/com/cypress/diary/ui/components/MarkdownDocumentView.kt`
- Test: `app/src/test/java/com/cypress/diary/ui/summary/SummaryTreeBuilderTest.kt`

- [ ] Write failing tests proving year/month documents are included and weeks are leaves.
- [ ] Implement the document-based summary tree.
- [ ] Update `WeekTree` to separate expand clicks from document selection clicks.
- [ ] Render selected document content in `SummaryScreen`.
- [ ] Run focused tests and commit.

### Task 4: Summary Editing Integration

**Files:**
- Modify: `app/src/main/java/com/cypress/diary/DiaryApp.kt`
- Modify: `app/src/main/java/com/cypress/diary/ui/screens/EditorScreen.kt`

- [ ] Add summary editor state keyed by selected document path.
- [ ] Open editor from Summary FAB with raw Markdown.
- [ ] Push edited Markdown to the selected document path using the existing repository method.
- [ ] Keep daily editor behavior unchanged.
- [ ] Run app tests and commit.

### Task 5: Background Image Fix

**Files:**
- Modify: `app/src/main/java/com/cypress/diary/ui/components/AppBackground.kt`

- [ ] Load selected image URI on `Dispatchers.IO`.
- [ ] Draw image at full opacity when available.
- [ ] Use a translucent scrim only when an image is present.
- [ ] Fall back to solid theme background when URI loading fails.
- [ ] Run app tests and commit.

### Task 6: Final Verification

**Files:**
- No source edits expected.

- [ ] Run `.\gradlew.bat test`.
- [ ] Run `.\gradlew.bat :app:assembleDebug`.
- [ ] Check `git status --short`.
- [ ] Report changed files, commits, and verification results.
