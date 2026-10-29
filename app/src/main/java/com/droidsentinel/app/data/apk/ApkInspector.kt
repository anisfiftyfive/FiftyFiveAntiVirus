package com.droidsentinel.app.data.apk

import android.content.Context
import android.net.Uri
import com.droidsentinel.app.domain.hash.FileHasher
import java.io.InputStream
import java.util.zip.ZipInputStream

data class ApkMetadata(
    val displayName: String,
    val sizeBytes: Long?,
    val zipEntryCount: Int,
    val hasManifest: Boolean,
    val hasDex: Boolean,
    val duplicateEntries: Boolean,
    val malformed: Boolean,
    val notes: List<String>,
)

class ApkInspector(private val context: Context) {
    fun open(uri: Uri): InputStream {
        return context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Unable to open selected file")
    }

    fun displayName(uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && index >= 0) return it.getString(index)
        }
        return uri.lastPathSegment ?: "selected.apk"
    }

    fun size(uri: Uri): Long? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val index = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
            if (it.moveToFirst() && index >= 0) return it.getLong(index)
        }
        return null
    }

    fun inspect(uri: Uri): ApkMetadata {
        val name = displayName(uri)
        val size = size(uri)
        if (size != null && size > FileHasher.MAX_SCAN_BYTES) {
            return ApkMetadata(name, size, 0, false, false, false, false, listOf("File exceeds the scan size limit."))
        }
        val names = mutableSetOf<String>()
        var duplicates = false
        var entries = 0
        var hasManifest = false
        var hasDex = false
        var malformed = false
        try {
            open(uri).use { input ->
                ZipInputStream(input).use { zip ->
                    while (true) {
                        val entry = zip.nextEntry ?: break
                        entries += 1
                        if (!names.add(entry.name)) duplicates = true
                        if (entry.name == "AndroidManifest.xml") hasManifest = true
                        if (entry.name.startsWith("classes") && entry.name.endsWith(".dex")) hasDex = true
                        if (entries > 20_000) {
                            malformed = true
                            break
                        }
                    }
                }
            }
        } catch (_: Throwable) {
            malformed = true
        }
        val notes = buildList {
            if (!hasManifest) add("No AndroidManifest.xml was found. This may not be a valid APK.")
            if (!hasDex) add("No classes.dex was found.")
            if (duplicates) add("The archive contains duplicate entries, which can be used to hide payloads.")
            if (malformed) add("The archive could not be fully parsed.")
        }
        return ApkMetadata(name, size, entries, hasManifest, hasDex, duplicates, malformed, notes)
    }
}
