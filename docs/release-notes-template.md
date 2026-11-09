# Release notes template

Related issue: [HW-41](https://helloworld-invaders.atlassian.net/browse/HW-41).
Status: **Accepted.**

Replace every placeholder with verified information. Do not publish this
template as an actual release or claim unrun tests passed. The accepted rules
are in [Release conventions](release-conventions-proposal.md).

---

## VERSION — Sprint N — YYYY-MM-DD

- Source commit: FULL_SHA_AND_LINK
- Release / JAR: VERSIONED_RELEASE_URL_AND_ARTIFACT_LINK
- Build evidence: SUCCESSFUL_RELEASE_WORKFLOW_RUN_LINK
- SHA-256: VERIFIED_ARTIFACT_DIGEST
- Runtime: Java 17 or newer
- Run: `java -jar Invaders.jar`
- Release preparation and verification time: ACTUAL_TIME

### Changes

- PLAYER_VISIBLE_CHANGE — JIRA_LINK, PR_LINK
- BUG_FIXED_AND_PLAYER_IMPACT — JIRA_LINK, PR_LINK

### Verification

| Check | Environment | Result | Evidence |
| --- | --- | --- | --- |
| Build and automated tests | OS / JDK / commit | Pass / Fail / Not run | CI or test record link |
| Downloaded JAR starts | OS / JDK | Pass / Fail / Not run | Log or screenshot |
| Changed behavior | Steps | Pass / Fail / Not run | Test record or screenshot |

Not verified: REMAINING_CHECKS_AND_REASON_OR_NONE.

### Known issues

- IMPACT_AND_WORKAROUND — JIRA_BUG_LINK

### Save data

Scores and other game save files are stored next to the JAR. Keep existing save
files when replacing the JAR. Record any save-format changes or migration steps
for this version; do not assume compatibility without checking.

### Correction history

For a patch snapshot, link the previous version, describe the correction, and
record the new commit and artifact digest. Otherwise omit this section.

---

- [Working Agreement](https://github.com/eoieiie/hello-world-invaders/wiki/Working-Agreement)
- [Release Notes destination](https://github.com/eoieiie/hello-world-invaders/wiki/Release-Notes)
