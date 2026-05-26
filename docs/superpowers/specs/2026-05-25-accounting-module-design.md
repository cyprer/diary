# Diary App Accounting Module Design

## Goal

Add a complete local accounting module to the existing Diary Android app. The entry point for switching modules lives on the Profile screen. The app remains diary-first by default, but users can switch between the diary workspace and the accounting workspace.

## Current Context

- The app is a Kotlin Jetpack Compose Android app.
- `DiaryApp.kt` owns top-level state, bottom navigation, and route rendering.
- `ProfileScreen.kt` already hosts personal settings, theme controls, import/export, background image, and the hidden GitHub sync dialog.
- Diary data, GitHub sync, Markdown parsing, and `.diary` import/export must remain isolated from accounting data.

## Product Behavior

The app has two workspaces:

- Diary workspace: current behavior with `Diary`, `Summary`, and `Profile`.
- Accounting workspace: new behavior with `Ledger`, `Stats`, and `Profile`.

The Profile screen includes a visible module switch card. It shows the current module and lets the user switch between Diary and Accounting. Switching modules changes the bottom navigation and the main content route. App theme, background image, and layout opacity remain shared across both workspaces.

The default module is Diary. The selected module is stored locally so the app can reopen in the user's last selected workspace.

## Accounting Features

The first implementation must provide a complete local accounting loop:

- Add income and expense records.
- Edit existing records.
- Delete records.
- View records in a ledger list grouped by date.
- View a monthly summary with income, expense, and balance.
- View category totals for the selected month.

Each record contains:

- `id`: stable unique string.
- `type`: income or expense.
- `amountCents`: integer amount in cents.
- `category`: user-facing category label.
- `date`: `LocalDate`.
- `note`: optional text.
- `createdAt`: epoch milliseconds.
- `updatedAt`: epoch milliseconds.

Amounts are stored as cents to avoid floating point errors. UI formatting displays values as currency-like decimal strings.

## Default Categories

Expense categories:

- Dining
- Transport
- Shopping
- Home
- Entertainment
- Medical
- Learning
- Other

Income categories:

- Salary
- Bonus
- Reimbursement
- Investment
- Other

The UI may display these labels in Chinese. The storage format should not depend on localized display text if a stable enum or key is available.

## Navigation Design

Add an app-level module state:

- `AppModule.Diary`
- `AppModule.Accounting`

Diary routes remain:

- `Diary`
- `Summary`
- `Profile`
- `Editor`

Accounting routes:

- `Ledger`
- `AccountingStats`
- `Profile`
- `AccountingEditor`

When the user switches modules:

- Switching to Diary routes to `Diary`.
- Switching to Accounting routes to `Ledger`.
- Profile stays available in both modules.
- The diary editor is only reachable in the diary workspace.
- The accounting editor is only reachable in the accounting workspace.

Bottom navigation:

- Diary workspace: `日记 / 总结 / 我的`.
- Accounting workspace: `账本 / 统计 / 我的`.

Floating action button:

- Diary workspace: current edit button on the diary route.
- Accounting workspace: add-record button on the ledger route.

## UI Design

### Profile Screen

Add a module switch card near the top of the Profile screen, after the account card. It uses a segmented control or two clear buttons:

- `日记`
- `记账`

The card should not expose GitHub concepts. Hidden GitHub behavior remains unchanged: the avatar seven-tap entry still opens GitHub sync settings.

### Ledger Screen

The ledger screen is a dense operational view:

- Header with selected month and totals.
- Month navigation controls.
- List of records grouped by date.
- Each row shows type, category, note, and amount.
- Tapping a row opens edit mode.

Empty state: show a concise message and keep the add-record FAB visible.

### Accounting Stats Screen

The stats screen shows:

- Selected month.
- Income total.
- Expense total.
- Balance.
- Expense category totals.
- Income category totals.

Use simple Compose lists and progress bars; no chart dependency is required for the first implementation.

### Accounting Editor

The editor screen supports add and edit:

- Income/expense segmented control.
- Amount input.
- Category selector.
- Date selector or date field using existing date patterns where practical.
- Optional note field.
- Save button.
- Delete button only for existing records.

Validation:

- Amount must be greater than zero.
- Category must be nonblank.
- Date must be valid.

## Data Storage

Create an accounting-specific storage layer:

- `model/accounting/AccountingRecord.kt`
- `model/accounting/AccountingRecordType.kt`
- `storage/AccountingRecordStore.kt`

Use SharedPreferences through the existing `PreferenceStore` abstraction where practical. Accounting records should use their own preferences file, for example `accounting_records`, and must not share keys with diary caches or drafts.

Storage responsibilities:

- Load all records.
- Save all records.
- Upsert a record.
- Delete a record by id.
- Ignore malformed stored entries rather than crashing app startup.

## Domain Logic

Create pure functions for accounting calculations:

- Filter records by month.
- Sum income.
- Sum expenses.
- Calculate balance.
- Group totals by category.
- Sort records by date descending and creation time descending.

These functions should live outside Composables and be covered by unit tests.

## Isolation Rules

Accounting must not:

- Change diary Markdown parsing or rendering.
- Change GitHub sync behavior.
- Add accounting data to GitHub sync.
- Add accounting data to `.diary` import/export in this first implementation.
- Expose Markdown, repository paths, owner, repo, or token to normal users.

Diary import/export continues to export diary data only. The Profile screen copy should make this clear by keeping existing button labels as diary-specific.

## Error Handling

- Invalid amount input disables save or shows a short inline validation message.
- Malformed stored records are skipped during load.
- Failed persistence should show a Toast and keep the user on the current screen.
- Deleting a record requires a confirmation dialog or a clearly labeled delete action in edit mode.

## Tests

Add focused unit tests for:

- Accounting record serialization and deserialization.
- Store load/save/upsert/delete behavior.
- Malformed storage entries are ignored.
- Amount parsing and formatting.
- Monthly filtering.
- Income, expense, balance totals.
- Category totals.
- Accounting route selection for app module navigation.
- Profile module switching state persistence.

Run at minimum:

```powershell
.\gradlew.bat testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
```

## Non-Goals

This implementation does not include:

- Cloud sync for accounting.
- Export/import for accounting.
- Custom user-defined categories.
- Budgets.
- Recurring transactions.
- Multi-account wallet support.
- Charts requiring new third-party dependencies.

These can be added later after the local accounting module is stable.

## Navigation Decision

The implementation uses the workspace model:

- Diary workspace and accounting workspace share Profile.
- Each workspace has its own bottom navigation.
