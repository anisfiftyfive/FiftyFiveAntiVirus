package com.droidsentinel.app.domain.hash

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class FileHasherTest {
    @Test
    fun hashesKnownEmptyInput() {
        val hashes = FileHasher.hash(ByteArrayInputStream(ByteArray(0)))
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", hashes.md5)
        assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709", hashes.sha1)
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", hashes.sha256)
        assertEquals(0, hashes.byteCount)
    }

    @Test
    fun hashesKnownPayload() {
        val hashes = FileHasher.hash(ByteArrayInputStream("DroidSentinel".toByteArray()))
        assertEquals(13, hashes.byteCount)
        assertEquals(32, hashes.md5.length)
        assertEquals(40, hashes.sha1.length)
        assertEquals(64, hashes.sha256.length)
    }

    @Test
    fun rejectsDeclaredOversize() {
        try {
            FileHasher.hash(ByteArrayInputStream(ByteArray(1)), declaredSize = FileHasher.MAX_SCAN_BYTES + 1)
            throw AssertionError("expected FileTooLargeException")
        } catch (expected: FileTooLargeException) {
            assertTrue(expected.bytes > FileHasher.MAX_SCAN_BYTES)
        }
    }

    @Test
    fun cancellationStopsHashing() {
        try {
            FileHasher.hash(ByteArrayInputStream("abc".toByteArray()), cancelled = { true })
            throw AssertionError("expected ScanCancelledException")
        } catch (_: ScanCancelledException) {
        }
    }
}
