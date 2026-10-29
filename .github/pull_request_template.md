## Summary

## Test evidence

Paste command output or the CI run URL.

- [ ] `./gradlew testDebugUnitTest --quiet`
- [ ] `./gradlew lintDebug --quiet`
- [ ] `./gradlew connectedDebugAndroidTest --quiet` (or the CI `ui` job)
- [ ] Scanner status tests updated if mapping changed

## AI assistance

- [ ] Human-only change
- [ ] AI-assisted — add label `ai-generated` and `Co-authored-by: Cursor <cursoragent@cursor.com>`
- [ ] Agent mistake follow-up — add label `ai-bug` and a note in `docs/AI_DECISIONS.md`

## Review

- [ ] Non-author reviewed scanner/GPG/permissions if those files changed
- [ ] No secrets, APK dumps, or inventories committed
