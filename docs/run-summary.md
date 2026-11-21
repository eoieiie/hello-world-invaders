# Run summary (HW-100)

The existing end-of-run screen now has a consistent title/footer and aligned
score, lives, enemies destroyed and accuracy rows. Values are presentation
copies; the original score still decides record eligibility and is saved.
The name entry, Space replay and ESC menu actions retain their existing flow
and input delay. Animation adds no extra input lockout.

Each row takes 300 ms (the Normal step in Screen Rules) with linear
interpolation, in order (1.2 s total). The screen can be left when its
existing input delay expires, before the counters finish.
Zero-shot runs show 0.00% rather than NaN. Negative scores remain negative.
The existing shots/kills ratio is otherwise unchanged, including the known
multi-projectile accuracy issue; this story does not fix gameplay accounting.

## Verification on Windows, 17 November 2026

- JDK 27.0.1.0.3 with `javac --release 17 -encoding UTF-8`.
- Eight new JUnit cases cover row boundaries, final values, negative/zero
  time, dropped frames, configurable duration, negative scores, accuracy
  formatting inputs, and invalid arguments.
- Actual isolated gameplay: title -> tutorial -> stage 1 -> loss of all lives
  -> summary. Replayed a second run and returned to the menu with ESC.
- Name changed from AAA to BAA; Space replay saved BAA/-100. ESC after the
  second run saved AAA/-100. The summary correctly showed zero lives, zero
  kills and 0.00% accuracy. These short runs ended at stage 1; they do not
  demonstrate a full ten-level clear or successful firing/aiming.
- Screenshot below was captured from the actual first run, without the
  computer-use cursor. After capture, the zero-shot expression was extracted
  into a tested helper and method names were clarified; the layout is unchanged.

![Run summary after a stage-1 loss](screenshots/HW-100/run-summary.png)

## Checks before ready for review

1. Repeat a visual check with non-zero counters. The short live runs above
   had no kills; unit tests cover the numeric sequence.
2. Play a run with kills and a non-zero score. Check sequential animation,
   final values against the run log, and continued responsiveness.
3. Check a run which does not qualify for a record: no name editor, same
   summary position, one footer. Check a successful cash-out as well.
4. With a new record, edit all three initials, then test Space and ESC exits.
   Confirm the full score is saved even if leaving before counters finish.
5. Check with Java 17 in CI; request kyobak's screen review and Aninnom's
   shared DrawManager review. Record actual human time separately in Jira.

Use a disposable run folder/JAR and fresh saves for these checks. Do not
overwrite an existing scores file to prepare a scenario.
