package com.droidsentinel.app.domain.hash

import com.droidsentinel.app.domain.model.FileHashes
import java.io.InputStream
import java.security.MessageDigest
import java.util.Locale

object FileHasher {
    const val MAX_SCAN_BYTES: Long = 500L * 1000L * 1000L
    private const val BUFFER_SIZE = 64 * 1024

    fun hash(stream: InputStream, declaredSize: Long? = null, cancelled: () -> Boolean = { false }): FileHashes {
        if (declaredSize != null && declaredSize > MAX_SCAN_BYTES) {
            throw FileTooLargeException(declaredSize)
        }
        val md5 = MessageDigest.getInstance("MD5")
        val sha1 = MessageDigest.getInstance("SHA-1")
        val sha256 = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(BUFFER_SIZE)
        var total = 0L
        while (true) {
            if (cancelled()) throw ScanCancelledException()
            val read = stream.read(buffer)
            if (read == -1) break
            if (read == 0) continue
            total += read
            if (total > MAX_SCAN_BYTES) throw FileTooLargeException(total)
            md5.update(buffer, 0, read)
            sha1.update(buffer, 0, read)
            sha256.update(buffer, 0, read)
        }
        return FileHashes(
            md5 = md5.digest().toHex(),
            sha1 = sha1.digest().toHex(),
            sha256 = sha256.digest().toHex(),
            byteCount = total,
        )
    }

    fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }.lowercase(Locale.US)
}

class FileTooLargeException(val bytes: Long) : Exception("File exceeds scan size limit ($bytes bytes)")

class ScanCancelledException : Exception("Scan cancelled")
