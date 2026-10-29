# Security model

## Assets

- Installed-app metadata (names, permissions, installer, cert hashes)
- APK bytes the user chose to scan
- Hypatia BloomFilter databases
- Local scan history

## Permissions

- `INTERNET` — HTTPS database updates only
- `QUERY_ALL_PACKAGES` — genuine antivirus-style inventory. Android 11+ otherwise hides most packages.

No storage, contacts, SMS, camera, or accessibility permissions are requested. APKs are opened through the Storage Access Framework.

## Trust boundaries

- Signature matches come only from a GPG-verified BloomFilter.
- Metadata findings never change signature status.
- The advisor cannot override scanner evidence.
- Logs must not include APK contents or full inventories.

## Threats we accept for this MVP

- BloomFilter false positives
- Incomplete package visibility
- Unreadable `/data/app` APKs on some devices
- User-supplied APKs that are not actually APKs

## Agent data classes

| Class | Agent access |
| --- | --- |
| Source, tests, docs | Allowed |
| Synthetic test hashes | Allowed |
| `local.properties`, keystores, `.env` | Denied (hooks + `.cursorignore`) |
| User APKs, inventories, scan history | Not in git; do not paste into chats |
| Hypatia databases | Downloaded on device; not committed |

## Misuse

DroidSentinel is not a silent monitor, not an enterprise EDR, and not a tool for attacking other apps.
