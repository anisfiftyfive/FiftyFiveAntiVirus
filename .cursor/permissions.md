# Cursor permission policy

Committed allow / ask / deny for this repo. Enforcement is `.cursor/hooks/` (`failClosed: true`). Do not enable bypass / yolo / dangerously-skip-permissions.

## Deny (hooks block)

- Read or git-add `local.properties`, `*.jks`, `*.keystore`, `keystore.properties`, `.env`, `secrets.env`, `*.pem`
- `git --no-verify`, `git push --force`, `rm -rf`, `curl | sh`, `DROP`/`TRUNCATE`, `terraform destroy`, `kubectl delete`

## Ask a human

- Push to `main`
- Changes to `.github/`, `.cursor/hooks*`, `CODEOWNERS`, GPG key, database URLs, Android permissions, or new dependencies

## Allow

- `./gradlew testDebugUnitTest --quiet`, `./gradlew lintDebug --quiet`, `./gradlew assembleDebug --quiet`
- Edits under `app/src` and `docs/` that are not secret files

## Sandbox and network

Run agent terminals in Cursor sandbox (workspace-write). Do not disable sandbox. Network is for GitHub, Gradle/Google Maven, and MaintainTeam Hypatia HTTPS only. Do not upload APKs, hashes, or app inventories.
