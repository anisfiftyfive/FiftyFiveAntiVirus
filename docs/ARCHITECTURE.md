# Architecture

```
Compose UI (Home, Apps, Results, History, Settings)
        │
   AppViewModel / StateFlow
        │
 ScanCoordinator ── MetadataRiskEngine
        │
 InstalledAppRepository / ApkInspector
        │
 HypatiaSignatureScanner ── HypatiaDatabaseRepository
        │
 Room scan history (on device)
```

## Layers

- **UI:** Jetpack Compose, Material 3, midnight-navy theme, Canvas shield + optional Lottie.
- **Presentation:** `AppViewModel` maps user actions to coordinator calls and exposes immutable UI state.
- **Domain:** `ScanCoordinator`, `MetadataRiskEngine`, `FileHasher`, advisor.
- **Data:** package manager inventory, SAF APK reader, Hypatia BloomFilters, GPG verification, Room.

The scanner never depends on Compose. Unit tests construct BloomFilters in memory and assert status mapping without an emulator.

## Directory map

| Path | What agents should use it for |
| --- | --- |
| `AGENTS.md` | Canonical agent instructions, commands, approval list |
| `docs/ARCHITECTURE.md` | This overview |
| `docs/SECURITY_MODEL.md` | Assets, permissions, data classes for agents |
| `docs/HYPATIA_INTEGRATION.md` | BloomFilter + GPG provenance |
| `docs/AI_WORKFLOW.md` | AI policy, models, IP, telemetry |
| `docs/AI_DECISIONS.md` | Corrections after agent mistakes |
| `docs/TESTING.md` | Test commands and coverage intent |
| `app/src/main/java/com/droidsentinel/app/domain/` | Scanner, metadata engine, hasher |
| `app/src/main/java/com/droidsentinel/app/data/hypatia/` | Hypatia adapter |
| `app/src/main/java/com/droidsentinel/app/ui/` | Compose only; no scan logic |
| `app/src/test/` | JVM unit tests (no emulator) |
| `.cursor/` | Rules, commands, blocking hooks |
| `.githooks/` | Pre-commit secret scan, pre-push tests |
| `.github/` | CI, CODEOWNERS, PR template |

Pinned versions live in [gradle/libs.versions.toml](../gradle/libs.versions.toml). JDK 17, compile SDK 36, Kotlin 2.0.21, Compose BOM 2024.12.01.
