# QA report

Recorded 29 October 2026 on this machine.

## `./gradlew testDebugUnitTest`

`BUILD SUCCESSFUL`

20 unit tests, 0 failures (including a GPG round-trip that does not use Android's stripped `BC` provider):

- `FileHasherTest` (4)
- `MetadataRiskEngineTest` (5)
- `HypatiaSignatureScannerTest` (7)
- `AdvisorOutputValidatorTest` (3)
- `HypatiaGpgVerifierTest` (1)

Notable cases that passed:

- Missing / unverified databases are `DATABASE_UNAVAILABLE`, not clean
- Empty-file hashes are ignored
- Hash failures stay `SCAN_FAILED`
- SMS + accessibility is high severity without a malware verdict
- Advisor JSON cannot invent a malware claim

## `./gradlew assembleDebug`

Succeeded. Debug APK:

`app/build/outputs/apk/debug/app-debug.apk` (21 MB)

## Not verified in this pass

- Install on a physical device or emulator
- Live download of official Hypatia databases (large remote files, needs network + GPG)
- TalkBack pass
- End-to-end detection against a live malware sample (intentionally not done)
