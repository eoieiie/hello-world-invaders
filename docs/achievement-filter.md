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

### Verified on 2026-11-14

- Local JUnit Console 6.1.3: 25 tests passed, including 6 new filter tests.
  Windows, OpenJDK 27.0.1.0.3, compiled with `--release 17`.
- GitHub Actions: Java 17 build, Gradle unit tests and title-screen smoke passed.
- Junhyeok manually verified F toggle/hold, page navigation, scrolling, ESC,
  reopening in All, restoring hidden rows and retaining requirements.
- A disposable profile (all five Normal achievements and Bronze Hunter
  unlocked) verified the completed-page message and mixed Tier filtering.
  Existing player saves were not used or changed.
- Captures: [All](screenshots/HW-64/all.png) and
  [completed page in Locked only](screenshots/HW-64/locked-empty.png).
