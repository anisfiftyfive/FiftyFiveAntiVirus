---
description: Review scanner status, GPG, permissions, logging, and secrets. Use before merging security-sensitive changes.
---

Inspect scanner status mapping, database verification, permissions, logging, and network use.

Checklist:

- Failed/unavailable scans are not shown as clean.
- GPG verification is not skipped.
- No APK contents or inventories in logs.
- No extra permissions.
- No hardcoded secrets.
- AI text cannot override scanner evidence.

When the review touches scanner or GPG code, verify with:

```
./gradlew testDebugUnitTest --quiet
```
