# AI decisions and corrections

## ChatGPT plan vs Hypatia README vs source

The ChatGPT master prompt said Hypatia uses Guava BloomFilters. The maintained Hypatia README still says ClamAV `.hdb` HashMaps. Source at `c0f9897` loads `BloomFilter.readFrom`. We followed source.

## Do not embed the Hypatia app

Copying Activities, FileObserver, and notifications would have burned the remaining day and mixed two UIs. The adapter boundary is `SignatureScanner`.

## Do not fake a loaded database

The UI starts with `DATABASE_UNAVAILABLE` rather than shipping an unverified BloomFilter. That costs demo flashiness and gains honesty points.

## Advisor stays local for the MVP

A remote LLM could invent malware names. The in-app advisor only restates findings. `AdvisorOutputValidator` exists so a future remote path cannot override evidence.

## AGPL

Adapting Hypatia's scanner model and consuming its databases is treated as AGPL-triggering. The app is AGPL-3.0-or-later. This is a conservative engineering choice, not legal advice.

## Android BouncyCastle SHA-256

The first on-device database update failed with `NoSuchAlgorithmException: no such algorithm: SHA-256 for provider BC`. Android already registers a stripped JCA provider named `BC`. JCA-based PGP verification (`setProvider("BC")`) asked that provider for SHA-256 and crashed. Fix: use BouncyCastle's lightweight `operator.bc` APIs, the same path Hypatia uses, and do not register a second `BC` provider.

## Lottie vs Compose

Hero motion is Canvas/Compose so scan states stay bound to real status. A tiny original Lottie file is used for the empty history illustration. If Lottie fails to parse, the screen still has text.
