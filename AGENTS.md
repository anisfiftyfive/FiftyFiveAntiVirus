# DroidSentinel agent instructions

Canonical project instructions. Cursor rules import this file. Prefer a working, honest scanner over unfinished feature lists.

Architecture, directory map, and versions: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md). Security model: [docs/SECURITY_MODEL.md](docs/SECURITY_MODEL.md). AI policy: [docs/AI_WORKFLOW.md](docs/AI_WORKFLOW.md).

## Architecture

- Kotlin, Jetpack Compose, Material 3, MVVM, StateFlow.
- Scanner engine stays out of Compose. UI only renders state.
- Hypatia-compatible BloomFilter matching is the signature engine. Metadata analysis is a separate deterministic engine.
- Local Room history. No analytics.

## Security rules

- Never claim malware from permissions alone.
- Never convert `SCAN_FAILED`, `DATABASE_UNAVAILABLE`, `NOT_SCANNED`, or `SCAN_INCOMPLETE` into `NO_MATCH_FOUND`.
- BloomFilter hits are possible matches, not malware family names.
- Do not upload APKs, hashes of user files, or installed-app inventories.
- Do not hardcode API keys or signing secrets.
- Do not silently skip GPG verification of Hypatia databases.

## Build and test

```
./gradlew testDebugUnitTest --quiet
./gradlew testDebugUnitTest --tests <FullyQualifiedTestClass> --quiet
./gradlew lintDebug --quiet
./gradlew assembleDebug --quiet
```

Report actual command output. Do not claim tests passed unless they ran.

## Human approval required

Agents must stop and ask a human before:

- `git push --force`, `git push -f`, or any `--no-verify` git command
- Changing `.github/`, `.cursor/hooks*`, `CODEOWNERS`, or signing/keystore files
- Adding dependencies, permissions, or network calls
- Changing Hypatia GPG key, database URLs, or skipping verification
- Deploy, release, or publish steps (none are configured)

## AI attribution

If an agent authored the patch, the human committer adds `Co-authored-by: Cursor <cursoragent@cursor.com>` and the PR label `ai-generated`. Failures caused by agent output use label `ai-bug` and a note in [docs/AI_DECISIONS.md](docs/AI_DECISIONS.md).

## Git hooks

After clone: `git config core.hooksPath .githooks` (repo-local only). Pre-commit scans staged files for secrets; pre-push runs `./gradlew testDebugUnitTest --quiet`.

## Hypatia provenance

- Upstream: https://github.com/MaintainTeam/Hypatia
- Inspected revision: c0f98977be935e58849726313635a2459cac3892
- License: AGPL-3.0-or-later. This app is AGPL because it adapts that scanner model and loads those databases.

## Definition of done

A change is done when `./gradlew testDebugUnitTest --quiet` and `./gradlew lintDebug --quiet` have been run, scanner semantics have tests if they changed, evidence and status stay honest, and no extra permissions or dependencies were added.
