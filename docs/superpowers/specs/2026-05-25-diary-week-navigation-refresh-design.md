# Diary Week Navigation and Manual Refresh Design

## Goal
Improve the diary page so week mode behaves like a week navigator, and stop automatic remote refresh on app launch.

## Requirements
- In diary week mode, the left control switches to the previous month-local week.
- In diary week mode, the right control switches to the next month-local week.
- Month-local weeks keep the existing app rule: week 1 is days 1-7, week 2 is days 8-14, week 3 is days 15-21, week 4 is days 22-28, and week 5 is days 29 through the end of the month.
- Crossing a month boundary should move to the adjacent month's matching edge week. For example, May week 1 left moves to April week 5.
- The selected day offset within the current week should be preserved when possible. If the target week is shorter, clamp to the target week's last day.
- The app should not fetch remote diary data automatically during startup.
- Remote fetch should happen when the user explicitly pulls to refresh.

## UI Behavior
Week mode keeps the existing card and week list layout. The header arrows in week mode become week navigation controls instead of month navigation controls. Month mode and year mode keep their current behavior.

## Data Flow
The selected date remains the only state passed up from the calendar picker. Week navigation computes the target date and calls the existing `onDateSelected` callback.

Startup should prefer locally cached or sample data. The existing refresh function remains available and is still wired to pull-to-refresh.

## Edge Cases
- May 1, 2026 previous week becomes April 29, 2026.
- May 3, 2026 previous week attempts to preserve offset into April week 5 and clamps to April 30, 2026.
- April 30, 2026 next week becomes May 2, 2026 if preserving the offset from April week 5.
- December week 5 next week crosses into January week 1 of the next year.
- January week 1 previous week crosses into December week 5 of the previous year.

## Testing
Add unit tests for month-local week navigation helpers, especially month and year boundaries. Existing calendar tests should continue to pass.
