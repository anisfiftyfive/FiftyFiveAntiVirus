---
description: Run unit tests, Android lint, and a debug assemble. Use after code changes before claiming done.
---

Run DroidSentinel checks. Report actual output. Substitute `<FullyQualifiedTestClass>` for a focused run.

```
./gradlew testDebugUnitTest --quiet
./gradlew testDebugUnitTest --tests <FullyQualifiedTestClass> --quiet
./gradlew lintDebug --quiet
./gradlew assembleDebug --quiet
```

Do not claim success if any task failed.
