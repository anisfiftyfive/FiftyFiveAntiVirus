# Testing

## Commands

```bash
./gradlew testDebugUnitTest --quiet
./gradlew testDebugUnitTest --tests com.droidsentinel.app.domain.hash.FileHasherTest --quiet
./gradlew lintDebug --quiet
./gradlew assembleDebug --quiet
```

## Coverage intent

- Hashing of empty and known payloads
- Oversize and cancellation paths
- Metadata rules: SMS + accessibility, camera apps, sideload installer
- Scanner statuses: missing DB, unverified DB, possible match, no match, empty-file skip, hash failure
- Advisor JSON validation and anti-override

## What is not claimed

End-to-end detection against live malware is not part of CI. Official Hypatia databases are large, remotely fetched, and not bundled. Adapter tests use in-memory BloomFilters.

Harmless EICAR-style fixtures can be added later if licensing is verified. Do not commit live malware samples.
