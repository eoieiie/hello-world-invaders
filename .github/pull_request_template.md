## Jira
<!-- The Jira key, e.g. HW-12. The branch name should contain it too: hello-world/HW-12-short-name -->
HW-

## What
<!-- One sentence. What does this PR do? -->

## Why
<!-- Which story or bug? What problem does it solve? -->

## How to test
1. Build & run
   - macOS / Linux: `javac -d bin $(find src -name "*.java") && java -cp bin:res engine.Core`
   - Windows (PowerShell): `javac -d bin (Get-ChildItem -Recurse src -Filter *.java).FullName; java -cp "bin;res" engine.Core`
2. 

## Tests
<!-- Which unit tests did you add or change? If none, say why. -->

## Screenshots
<!-- Required for anything visible on screen -->

## Checklist
- [ ] Branch is `hello-world/HW-<n>-<short-name>` and based on the latest `main`
- [ ] One story or bug per PR, under ~400 changed lines
- [ ] Unit tests added or updated for changed logic
- [ ] CI is green
- [ ] I ran the game after my change
- [ ] Jira item updated (status, actual hours)
