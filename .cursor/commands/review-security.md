# Review security-sensitive code

Inspect scanner status mapping, database verification, permissions, logging, and network use.

Checklist:

- Failed/unavailable scans are not shown as clean.
- GPG verification is not skipped.
- No APK contents or inventories in logs.
- No extra permissions.
- No hardcoded secrets.
- AI text cannot override scanner evidence.
