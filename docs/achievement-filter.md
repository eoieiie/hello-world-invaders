# Achievement filter (HW-64)

Open **Achievements** from the main menu. The screen starts in **All** mode.
Press **F** to switch between **All** and **Locked only**. Release F before
pressing it again; holding it must not repeatedly change the mode.

- Left/right (or A/D) changes the Normal/Tier page.
- Up/down (or W/S) selects and scrolls rows on that page.
- Changing the filter resets the selection and scroll to the first visible row.
- The filter stays selected when changing pages, but resets to All on reopening.
- Locked achievements keep their requirements and progress indicators.
- A completed page shows "All achievements on this page unlocked."
- The Unlocked counter always describes the full catalogue, not the filtered list.
- ESC returns to the main menu. Filtering does not change saved achievements.

## Validation

JUnit tests cover mixed, all-locked, all-unlocked and empty catalogues;
page boundaries; order and object preservation; offscreen rows; returning to
All mode; and reading a changed unlock state. Run `./gradlew test` with JDK 17.

For a manual check, use a separate game folder and a disposable profile:

1. Open Achievements and check All, the F hint and the full Unlocked count.
2. Toggle F with some achievements unlocked: only locked rows should remain.
3. Visit Tier, scroll to its last row, then toggle twice. Selection and scrolling
   should reset safely while the global Unlocked count remains unchanged.
4. Hold F for two seconds: the mode should change only once per press.
5. On a completed page, check the empty message, then change pages and toggle
   back to All. Both pages must remain usable even when one has no visible rows.
6. Press ESC and reopen Achievements: All mode should be restored.

Do not replace a player's existing profile to create these test conditions.
