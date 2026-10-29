package com.droidsentinel.app.data.hypatia

import java.nio.charset.StandardCharsets
import com.google.common.hash.BloomFilter
import com.google.common.hash.Funnels
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

data class LoadedBloomFilters(
    val md5: BloomFilter<CharSequence>,
    val sha1: BloomFilter<CharSequence>,
    val sha256: BloomFilter<CharSequence>,
    val approximateCount: Long,
    val sourceLabel: String,
)

object HypatiaBloomStore {
    fun read(stream: InputStream): BloomFilter<CharSequence> {
        return BloomFilter.readFrom(stream, Funnels.stringFunnel(StandardCharsets.US_ASCII))
    }

    fun loadFromDirectory(directory: File, label: String = directory.name): LoadedBloomFilters {
        val md5 = readNamed(directory, "hypatia-md5-bloom.bin")
        val sha1 = readNamed(directory, "hypatia-sha1-bloom.bin")
        val sha256 = readNamed(directory, "hypatia-sha256-bloom.bin")
        val count = md5.approximateElementCount() + sha1.approximateElementCount() + sha256.approximateElementCount()
        return LoadedBloomFilters(md5, sha1, sha256, count, label)
    }

    fun passesSelfTest(filters: LoadedBloomFilters): Boolean {
        return HypatiaConstants.SELF_TEST_MD5.all { filters.md5.mightContain(it) } &&
            HypatiaConstants.SELF_TEST_SHA1.all { filters.sha1.mightContain(it) } &&
            HypatiaConstants.SELF_TEST_SHA256.all { filters.sha256.mightContain(it) }
    }

    private fun readNamed(directory: File, name: String): BloomFilter<CharSequence> {
        val file = File(directory, name)
        if (!file.exists()) throw IllegalStateException("Missing database file $name")
        FileInputStream(file).use { return read(it) }
    }
}
