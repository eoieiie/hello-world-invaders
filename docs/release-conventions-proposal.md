# Release conventions

- Jira: [HW-41](https://helloworld-invaders.atlassian.net/browse/HW-41)
- Author: Junhyeok Han
- Proposed: 2026-11-09
- Status: **Accepted**
- Approved: 2026-11-09 by Byeongjoo in [PR #12](https://github.com/eoieiie/hello-world-invaders/pull/12)
- Review date: 2026-11-23

Use this convention for sprint releases and retain the approval date and PR link
when recording the accepted decision in the Wiki Decision Log.

## Context

The team needs a reproducible build and release notes for each sprint review.
The existing Release workflow publishes `Invaders.jar` after a push to `main`,
replacing the prerelease named `latest`. This is useful for daily testing, but
does not preserve each sprint's artifact.

## Options considered

| Option | Benefit | Cost or limitation |
| --- | --- | --- |
| Keep `latest` only | No additional publishing step | Earlier sprint artifacts and notes are replaced |
| Keep `latest` and publish a fixed version per sprint | Preserves the exact demo build and evidence | A small manual verification and publishing step each sprint |
| Add release branches and a separate tag-triggered workflow | Supports independent maintenance lines | More workflow and branch maintenance for one active development line |

## Decision

Choose the second option without changing the existing workflow in this task.

### Names and tag behavior

- Keep `latest` as the moving prerelease produced from `main`. It is a convenience
  download, not a permanent version identifier.
- Use `v0.<sprint>.<patch>` for fixed sprint snapshots: `v0.1.0` for the first
  Sprint 1 snapshot, `v0.2.0` for Sprint 2, and so on. Increment the patch number
  for a corrected snapshot of the same sprint, such as `v0.1.1`.
- This is a project-specific pre-1.0 convention using the familiar
  `MAJOR.MINOR.PATCH` layout. The minor number identifies the sprint; it does not
  promise an API compatibility contract or automatically make the final sprint
  `v1.0.0`.
- Create each fixed version tag on the exact, verified `main` commit. Never move
  or reuse a published version tag. Publish a new patch version for a correction
  and explain what changed.
- Decide whether to name the final delivery `v1.0.0` separately, based on the
  final acceptance criteria and the lead's approval.

### When to publish

1. Select a merged `main` commit after its required checks pass and the intended
   sprint changes have been reviewed.
2. Verify the corresponding Release run succeeded. Obtain the JAR for that exact
   source SHA, record its digest, and launch it in an isolated test directory.
3. Publish the fixed sprint snapshot and its release notes before Tuesday
   planning. Record known bugs and unverified checks explicitly. A title-screen
   smoke check is not a full gameplay test.
4. If verification cannot finish in time, record the reason and remaining work
   instead of labeling the snapshot ready.

### Artifact and evidence

- Attach the verified `Invaders.jar` to the fixed release. Record the full commit
  SHA, build-run link, SHA-256, runtime requirement, test results, and related
  Jira and PR links using the [release notes template](release-notes-template.md).
- Check that the downloaded `latest` asset still corresponds to the selected
  commit. If `latest` has moved, obtain or build an artifact from the selected
  commit and verify it before publication. Do not attach a different commit's
  JAR to the old tag.
- Mirror the version's notes on the Wiki Release Notes page and keep earlier
  entries.
- Use Java 17 or newer and `java -jar Invaders.jar`. Keep existing saved game
  files when replacing the JAR; record and verify any save-format changes.
- Manual Release workflow runs must target `main`. A version-tag push alone
  does not trigger the current workflow.

## Rationale

Fixed snapshots preserve evidence for sprint demonstrations while `latest`
stays convenient for everyday testing. The team can use the existing build
without maintaining a second release pipeline. A patch suffix makes corrections
explicit without rewriting earlier evidence.

## Scope and follow-up

This document does not create a tag, publish a release, change workflow triggers,
or introduce a release branch. A tag-triggered workflow would be a separate
automation proposal and implementation task.

HW-42 records verification of the existing workflow and released JAR. Record the
time spent on each release, including SHA-256 verification, launching the JAR in
a clean folder, and checking that the `latest` asset matches the source commit.
At the review on 2026-11-23, assess the publishing effort, wrong-version
incidents, and use of snapshots in sprint reviews to judge whether the benefits
justify the work.

The existing workflow notes describe the project font as absent, although
Silkscreen is already tracked and selected by the code. Correcting that wording
is a separate follow-up; this document does not change the workflow.

## References

- [Working Agreement](https://github.com/eoieiie/hello-world-invaders/wiki/Working-Agreement)
- [Decision Log destination](https://github.com/eoieiie/hello-world-invaders/wiki/Decision-Log)
- [Release Notes destination](https://github.com/eoieiie/hello-world-invaders/wiki/Release-Notes)
- [Workflow at the reviewed commit](https://github.com/eoieiie/hello-world-invaders/blob/0467dd34bfad4ef634857803e52dae1e90aa91cc/.github/workflows/release.yml)
- [Existing rolling release](https://github.com/eoieiie/hello-world-invaders/releases/tag/latest)
