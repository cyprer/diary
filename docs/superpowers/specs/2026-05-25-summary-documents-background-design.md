# Summary Documents and Background Design

## Goal

Update the Android diary app so the Summary page matches the real `astroblog`
summary structure:

- Year summaries live at `src/content/posts/summary/YYyear/index.md`.
- Month summaries live at `src/content/posts/summary/YYyear/Mmonth/index.md`.
- Weekly journals live at `src/content/posts/summary/YYyear/Mmonth/Nweek.md`.

The Summary page should read these Markdown files, render selected content for
reading, and only show raw Markdown after the user chooses to edit.

Also fix the app background image feature so selected images are visible and
fail gracefully when an image cannot be opened.

## Summary Page Behavior

The Summary page has two interaction modes:

- Expansion controls: tapping the arrow or side control next to a year/month
  expands or collapses child nodes.
- Selection controls: tapping the year/month/week label selects that document
  and displays its rendered content.

Node behavior:

- Year label selects and renders the year `index.md`.
- Year expansion shows months.
- Month label selects and renders the month `index.md`.
- Month expansion shows the four weekly files.
- Week label selects and renders the corresponding `Nweek.md`.
- Week nodes do not expand into day nodes.

The selected document reader shows rendered Markdown content. It is not an
editor by default. A floating edit button opens the selected document as full
raw Markdown.

## Editing and Push

Editing from the Summary page uses the same GitHub push path as the existing
editor:

- The editor receives the selected document path and raw Markdown.
- Drafts are keyed by document path.
- Push writes the edited Markdown back to the same GitHub path.
- After a successful push, the app refreshes remote documents and updates the
  local cache.

Daily editing can keep the current day-specific behavior. Summary document
editing is path-based and should not try to merge individual day sections.

## Data Model

Introduce a generic summary document model:

- `DiaryDocument`
  - `path`
  - `type`: year summary, month summary, or week journal
  - `year`
  - optional `month`
  - optional `weekIndex`
  - `title`
  - `published`
  - `markdown`
  - `body`

The existing `DiaryWeek` model can still be used for the daily diary page and
week parsing. The Summary page should be built from `DiaryDocument` so it can
include year and month `index.md` documents.

## GitHub Loading

For token-based repositories, list the Git tree recursively and include:

- `src/content/posts/summary/YYyear/index.md`
- `src/content/posts/summary/YYyear/Mmonth/index.md`
- `src/content/posts/summary/YYyear/Mmonth/Nweek.md`

For public repositories without a token, derive candidate paths from the same
known structure used by `astroblog`:

- years from 2025 through the current year
- months up to the current month for the current year
- `index.md` for each year and month
- `1week.md` through `4week.md` for each month

Missing public files are ignored.

## Rendering

Markdown rendering should be simple for this version:

- Strip frontmatter.
- Strip the first H1 from the displayed body if it duplicates the document
  title.
- Render headings and paragraphs with Compose text styles.
- Preserve readable line breaks.

This avoids adding a Markdown rendering dependency while still giving a clean
reading mode.

## Background Image Fix

The current background layer makes selected images nearly invisible because the
image is drawn at low alpha and then covered by a mostly opaque theme layer.

Change the background behavior:

- Draw the selected image at full opacity with `ContentScale.Crop`.
- Draw a translucent theme-colored scrim above it for readability.
- If no image is selected, use the normal solid theme background.
- Load the image off the main thread.
- If URI loading fails, render the normal background instead of crashing.

## Tests

Add focused unit tests for:

- Path matching and extraction for year/month/week summary documents.
- Summary tree building that includes year and month summary documents and
  week leaves without day expansion.
- Markdown display body extraction from frontmatter documents.
- Cache round trip keyed by document path.

Existing tests for week parsing, path resolving, drafts, and themes should
continue to pass.
