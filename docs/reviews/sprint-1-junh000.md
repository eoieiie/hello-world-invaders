# Sprint 1 code review record — junh000

- Jira: [HW-47](https://helloworld-invaders.atlassian.net/browse/HW-47)
- Reviewer: Junhyeok Han (`junh000`)
- Evidence recorded: 2026-11-09 (Asia/Seoul)

## PR #8: damaged high score entries

[PR #8](https://github.com/eoieiie/hello-world-invaders/pull/8) changes score parsing so damaged entries do not prevent valid scores from loading.

- Tested commit: `9d3b2d5c09d98e0cf302b5385af70997158cd016`
- Comparison base: `1e60c2dfc4ac1013ebc1b6fac61f9962bd4de1ba`
- [Submitted review](https://github.com/eoieiie/hello-world-invaders/pull/8#pullrequestreview-5821366523): **Comment**, submitted after the PR had merged.
- Environment: Windows, JDK 17, isolated test copy with separate saved data; testing used Codex assistance.

### Build and automated checks

All 80 Java source files compiled without errors or warnings. The following checks passed:

- Valid and damaged name/score pairs; non-numeric, oversized and decimal scores.
- Blank lines, CRLF and empty files.
- Sorting, save/reload, a fresh-process read, the seven-record save limit and default-score fallback.

The damaged-file case raised `NumberFormatException` on the comparison base and loaded successfully on the tested PR commit.

### Manual screen checks

A test scores file containing the lines `AAA`, `abc`, `CCC`, `700` displayed only `CCC 0700` on the High scores screen. The warning identified the skipped `AAA` entry, and ESC returned to the menu.

An empty scores file displayed an empty list, and ESC returned to the menu.

### Limits and follow-up

The game-over results screen, name entry and saving through that screen were **not verified**. Automated input did not advance past the tutorial. Automated save/reload coverage does not establish that the complete game-over UI flow works.

No new defect was found in the tested cases. This does not establish that every corrupted-file format or gameplay path works. Original source files and existing user saves were preserved.

This record covers the linked PR #8 review only. It does not close HW-47 or assert that every Sprint 1 review is complete. Check for new review requests before closing the Jira task, and record the actual review time there; no actual hours have been entered by this document.
