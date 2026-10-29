# DroidSentinel

Privacy-first Android security companion for everyday users.

**Know what is happening on your Android device.**

DroidSentinel inspects installed apps and user-selected APK files on-device. It combines:

1. Hypatia-compatible signature scanning (MD5 / SHA-1 / SHA-256 BloomFilters)
2. Deterministic permission and metadata analysis
3. An evidence-only advisor that cannot invent detections

It does **not** claim to replace Play Protect, provide real-time protection, or prove that a device is safe.

## What it can do

- List apps Android is willing to expose, including permissions, installer, SDK, and signing certs
- Hash a readable APK in one pass and check official Hypatia BloomFilter databases
- Explain why a permission cluster deserves attention
- Store scan history locally
- Download and GPG-verify Hypatia databases over HTTPS when you ask it to

## What it will not do

- Delete or disable other apps
- Upload your files or app list
- Treat a missing database as a clean scan
- Name a malware family from a BloomFilter hit

## Build

JDK 17+, Android SDK with compile SDK 36.

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/`.

On first launch, signature scanning stays `DATABASE_UNAVAILABLE` until you download the Hypatia databases in Settings. Metadata inspection still works.

## License

[AGPL-3.0-or-later](LICENSE). Hypatia is AGPL, and DroidSentinel adapts its scanner model and loads its databases.

See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) and [docs/HYPATIA_INTEGRATION.md](docs/HYPATIA_INTEGRATION.md).
