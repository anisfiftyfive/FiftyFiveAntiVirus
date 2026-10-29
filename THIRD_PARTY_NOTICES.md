# Third-party notices

## Hypatia

- Project: MaintainTeam/Hypatia, fork of Divested-Mobile/Hypatia
- URL: https://github.com/MaintainTeam/Hypatia
- Inspected revision: c0f98977be935e58849726313635a2459cac3892
- License: GNU Affero General Public License v3.0 or later
- Copyright: Divested Computing Group; MaintainTeam Organization
- Use in DroidSentinel: compatible BloomFilter loader, hash-and-match scanner semantics, database file names, HTTPS update URLs, GPG key id `5298C0C0C3E73288`, and official self-test canary hashes

The original Hypatia application UI, realtime FileObserver, and notifications are **not** copied into this repository.

## Hypatia signature databases

Downloaded only when the user starts an update.

- Default source: https://maintainteam.github.io/HypatiaDatabases/
- Alternate documented by upstream: https://maintainteam.codeberg.page/HypatiaDatabases/
- Database contents originate from ClamAV-style and other malware hash sets redistributed by the Hypatia project. See Hypatia's README for ClamAV (GPLv2) and ESET (BSD-2-Clause) credit.

## Libraries

- AndroidX / Jetpack Compose / Material 3 / Room / Navigation — Apache-2.0
- Guava BloomFilter — Apache-2.0
- OkHttp — Apache-2.0
- Bouncy Castle bcpg — MIT
- Lottie for Android — Apache-2.0
- Lottie JSON in `app/src/main/res/raw/shield_pulse.json` — original to DroidSentinel

## Icons

Launcher and shield vectors are original to DroidSentinel.
