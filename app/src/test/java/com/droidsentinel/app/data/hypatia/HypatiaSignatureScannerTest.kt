package com.droidsentinel.app.data.hypatia

import com.droidsentinel.app.domain.hash.FileHasher
import com.droidsentinel.app.domain.model.FileHashes
import com.droidsentinel.app.domain.model.SignatureScanStatus
import com.google.common.hash.BloomFilter
import com.google.common.hash.Funnels
import java.nio.charset.StandardCharsets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class HypatiaSignatureScannerTest {
    private val scanner = HypatiaSignatureScanner()

    @Test
    fun missingDatabaseIsNotACleanScan() {
        val hashes = FileHasher.hash(ByteArrayInputStream("abc".toByteArray()))
        val outcome = scanner.scanHashes(hashes)
        assertEquals(SignatureScanStatus.DATABASE_UNAVAILABLE, outcome.status)
    }

    @Test
    fun unverifiedDatabaseIsNotACleanScan() {
        val filters = testFilters(setOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"))
        scanner.replaceFilters(filters, verified = false, lastUpdated = 1L, reason = "unverified")
        val hashes = FileHashes("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "b".repeat(40), "c".repeat(64), 4)
        val outcome = scanner.scanHashes(hashes)
        assertEquals(SignatureScanStatus.DATABASE_UNAVAILABLE, outcome.status)
    }

    @Test
    fun knownHashProducesPossibleMatch() {
        val payload = "HypatiaHypatiaHypatia"
        val hashes = FileHasher.hash(ByteArrayInputStream(payload.toByteArray()))
        val filters = testFilters(md5 = setOf(hashes.md5), sha1 = setOf(hashes.sha1), sha256 = setOf(hashes.sha256))
        scanner.replaceFilters(filters, verified = true, lastUpdated = 1L, reason = null)
        val outcome = scanner.scanHashes(hashes)
        assertEquals(SignatureScanStatus.THREAT_DETECTED, outcome.status)
        assertTrue(outcome.matches.isNotEmpty())
        assertTrue(outcome.message!!.contains("possible", ignoreCase = true))
    }

    @Test
    fun unknownHashIsNoMatchNotFailed() {
        val hashes = FileHasher.hash(ByteArrayInputStream("clean-fixture".toByteArray()))
        val filters = testFilters(md5 = setOf("903616d0dbe074aa363d2d49c03f7362"))
        scanner.replaceFilters(filters, verified = true, lastUpdated = 1L, reason = null)
        val outcome = scanner.scanHashes(hashes)
        assertEquals(SignatureScanStatus.NO_MATCH_FOUND, outcome.status)
    }

    @Test
    fun emptyFileHashesAreIgnored() {
        val empty = FileHasher.hash(ByteArrayInputStream(ByteArray(0)))
        val filters = testFilters(
            md5 = setOf(empty.md5),
            sha1 = setOf(empty.sha1),
            sha256 = setOf(empty.sha256),
        )
        scanner.replaceFilters(filters, verified = true, lastUpdated = 1L, reason = null)
        val outcome = scanner.scanHashes(empty)
        assertEquals(SignatureScanStatus.NO_MATCH_FOUND, outcome.status)
    }

    @Test
    fun hashFailureDoesNotBecomeNoMatch() {
        val outcome = scanner.mapHashFailure(IllegalStateException("boom"))
        assertEquals(SignatureScanStatus.SCAN_FAILED, outcome.status)
    }

    @Test
    fun bloomRoundTripMatchesHypatiaFormat() {
        val original = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.US_ASCII), 32, 0.0001)
        original.put("deadbeefdeadbeefdeadbeefdeadbeef")
        val bytes = ByteArrayOutputStream().also { original.writeTo(it) }.toByteArray()
        val restored = HypatiaBloomStore.read(ByteArrayInputStream(bytes))
        assertTrue(restored.mightContain("deadbeefdeadbeefdeadbeefdeadbeef"))
    }

    private fun testFilters(
        md5: Set<String> = emptySet(),
        sha1: Set<String> = emptySet(),
        sha256: Set<String> = emptySet(),
    ): LoadedBloomFilters {
        fun bloom(values: Set<String>): BloomFilter<CharSequence> {
            val filter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.US_ASCII), 64, 0.0001)
            values.forEach { filter.put(it) }
            return filter
        }
        return LoadedBloomFilters(bloom(md5), bloom(sha1), bloom(sha256), 64, "test-fixture")
    }
}
