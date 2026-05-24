# Diary Android App Design

## Goal
Build an Android app for writing diary entries on a phone and syncing them directly to the user's GitHub blog repository.

The app should support:
- GitHub login
- browsing diary content by day and by week/month/year
- creating a new weekly markdown file when a new week starts
- editing existing diary entries inside weekly markdown files
- pushing changes back to GitHub without a separate backend

## Current Blog Structure
The target blog already stores content as Markdown files in an Astro/Fuwari repository.

Relevant patterns observed:
- weekly diary files live under `src/content/posts/summary/YYYYyear/Mmonth/`
- files are named like `1week.md`, `2week.md`, etc.
- the frontmatter uses standard blog fields such as `title`, `published`, `description`, `tags`, `category`, and `draft`
- each file contains the week title plus day-level sections inside the Markdown body

## Product Shape
The app has three main surfaces:

1. Diary view
   - shows a single day's entry
   - supports previous/next day navigation
   - includes a floating edit action
   - uses a card-based reading layout similar to the reference image

2. Summary view
   - shows year -> month -> week -> day hierarchy
   - expands/collapses nested ranges
   - lets the user jump to any week or day quickly

3. Editor
   - lets the user edit the current day or the whole weekly file
   - creates a new weekly file automatically when the current date falls into a week that does not exist yet

## Recommended Approach
Use a single Android client with no backend.

Core stack:
- Jetpack Compose UI
- Kotlin
- MVVM-style state handling
- GitHub OAuth login
- GitHub Contents API for reading and writing markdown files
- local draft cache for offline editing and crash recovery

Why this route:
- simplest end-to-end path
- no server to deploy or maintain
- matches the user's existing GitHub-based workflow
- keeps the app focused on writing, not infrastructure

## Data Model
Treat each weekly markdown file as the source of truth.

Domain objects:
- `DiaryWeek`
  - repo path
  - year
  - month
  - week index within the month
  - published date
  - title
  - markdown body

- `DiaryDay`
  - date
  - heading text
  - body markdown
  - optional visual marker or section icon

The app should parse and regenerate Markdown instead of inventing a second storage format.

## Sync Flow
Read flow:
- log in to GitHub
- fetch repo file list or resolve the expected weekly file path
- load the markdown content
- parse it into week/day structures for the UI

Write flow:
- edit in the app
- save to local draft cache immediately
- regenerate the markdown file
- upload the updated file to GitHub
- if the file does not exist for the new week, create it first

## Weekly File Rules
When the date moves into a new week:
- compute the new year/month/week location
- create a fresh `Nweek.md` file if it is missing, where `N` is the week index within the month
- write the standard frontmatter
- seed it with the new week title and an empty or starter day section

When editing an existing week:
- update only the relevant markdown file
- preserve the rest of the file unless the parser needs to normalize formatting

## Error Handling
Handle these cases explicitly:
- GitHub login cancelled or denied
- network offline while editing
- file conflict because the same file changed on GitHub
- missing or malformed markdown frontmatter
- week path not found

Recovery behavior:
- keep the local draft
- show a clear sync status
- retry upload after refetching the latest remote content when conflicts occur

## Testing Scope
Minimum tests:
- markdown parse/regenerate round trip
- week path resolution from date
- new-week file creation logic
- GitHub sync service tests with mocked responses
- UI smoke coverage for diary and summary screens

## Out Of Scope For v1
- backend service
- social features
- multiple blogs/accounts
- rich media uploads beyond what the current markdown format already supports
- full WYSIWYG editor

## Acceptance Criteria
The app is done when:
- the user can log in to GitHub
- the user can read diary entries from the repository
- the user can create and edit a diary entry on the phone
- the app can create a new weekly file automatically
- the app can push changes back to GitHub successfully
