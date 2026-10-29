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
