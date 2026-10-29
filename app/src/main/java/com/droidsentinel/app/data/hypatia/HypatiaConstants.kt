package com.droidsentinel.app.data.hypatia

/**
 * Constants taken from the maintained Hypatia scanner so DroidSentinel can
 * load the same BloomFilter databases.
 *
 * Upstream: https://github.com/MaintainTeam/Hypatia
 * Revision: c0f98977be935e58849726313635a2459cac3892
 * License: AGPL-3.0-or-later
 */
object HypatiaConstants {
    const val UPSTREAM_URL = "https://github.com/MaintainTeam/Hypatia"
    const val UPSTREAM_COMMIT = "c0f98977be935e58849726313635a2459cac3892"
    const val DATABASE_BASE_URL = "https://maintainteam.github.io/HypatiaDatabases/"
    const val DATABASE_MIRROR_URL = "https://maintainteam.codeberg.page/HypatiaDatabases/"
    const val SIGNING_KEY_ID = "5298C0C0C3E73288"
    const val USER_AGENT = "DroidSentinel/0.1 (Hypatia-compatible)"

    val DATABASE_FILES = listOf(
        "hypatia-md5-bloom.bin",
        "hypatia-sha1-bloom.bin",
        "hypatia-sha256-bloom.bin",
    )

    const val PUBLIC_KEY_FILE = "gpg.key"

    /**
     * Canary hashes embedded in official Hypatia databases.
     * Source: Database.selfTest() in MaintainTeam/Hypatia.
     */
    val SELF_TEST_MD5 = listOf(
        "903616d0dbe074aa363d2d49c03f7362",
        "faf325d9d4b2a6c9457405eb31870b22",
    )
    val SELF_TEST_SHA1 = listOf(
        "fc4a3e802894cc2229be77ec6f082d1aab744e54",
        "04445ab9332cdca0031184165eb88a0a9dcec4ed",
    )
    val SELF_TEST_SHA256 = listOf(
        "df44844a0e99ddd935e8419257440a2ca7ef3243435a67416fcbb6cd3ae560c3",
        "3be403a831795482e82e2f9bc3e57ca34744aca3756e297b056b9f9c41830eaf",
    )

    val EMPTY_MD5 = "d41d8cd98f00b204e9800998ecf8427e"
    val EMPTY_SHA1 = "da39a3ee5e6b4b0d3255bfef95601890afd80709"
    val EMPTY_SHA256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
}
