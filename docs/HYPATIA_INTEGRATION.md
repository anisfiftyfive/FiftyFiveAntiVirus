# Hypatia integration

Inspected upstream: [MaintainTeam/Hypatia](https://github.com/MaintainTeam/Hypatia) at `c0f98977be935e58849726313635a2459cac3892`.

The original Divested repository is archived. The maintained fork is an Android **application**, not a library. DroidSentinel therefore implements a small compatible engine instead of embedding Hypatia's UI.

## What we verified in source

- Databases are Guava `BloomFilter<String>` files written with `Funnels.stringFunnel(US_ASCII)`
- File names: `hypatia-md5-bloom.bin`, `hypatia-sha1-bloom.bin`, `hypatia-sha256-bloom.bin`
- Matching is `mightContain(hash)` after hashing the file in one pass (MD5 + SHA-1 + SHA-256)
- Hits are described upstream as **potential matches**
- Empty-file hashes are ignored
- Official databases are GPG-signed; key id `5298C0C0C3E73288`
- Self-test canary hashes live in `Database.selfTest()`

The upstream README still mentions ClamAV `.hdb` HashMaps. The Kotlin sources we inspected load BloomFilters. DroidSentinel follows the **source**, not the stale README.

## Integration path chosen

Option B from the hackathon plan: a maintainable adaptation of the scanner components, isolated behind `SignatureScanner`.

Not done:

- Embedding Hypatia Activities
- Realtime FileObserver
- Assuming an installed Hypatia app
- Bundling unverified databases inside the APK

## Database updates

`HypatiaDatabaseRepository` downloads `gpg.key`, each `.bin`, and each `.bin.sig` over HTTPS from `https://maintainteam.github.io/HypatiaDatabases/`. Files that fail GPG verification are discarded. A failed or missing database yields `DATABASE_UNAVAILABLE`, never `NO_MATCH_FOUND`.

## License

Hypatia is AGPL-3.0-or-later. DroidSentinel is distributed under the same license. This note is technical provenance, not legal advice.
