# DroidSentinel agent instructions

Build a privacy-first Android security MVP. Prefer a working, honest scanner over unfinished feature lists.

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
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Report actual command output. Do not claim tests passed unless they ran.

## Hypatia provenance

- Upstream: https://github.com/MaintainTeam/Hypatia
- Inspected revision: c0f98977be935e58849726313635a2459cac3892
- License: AGPL-3.0-or-later. This app is AGPL because it adapts that scanner model and loads those databases.

## Definition of done

A change is done when it builds, has tests for scanner semantics where behavior changed, keeps evidence and status honest, and does not introduce unnecessary permissions or dependencies.
