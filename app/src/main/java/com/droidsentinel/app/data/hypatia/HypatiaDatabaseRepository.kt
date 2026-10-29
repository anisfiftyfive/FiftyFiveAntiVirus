package com.droidsentinel.app.data.hypatia

import android.content.Context
import com.droidsentinel.app.domain.model.DatabaseFileStatus
import com.droidsentinel.app.domain.model.DatabaseStatus
import com.droidsentinel.app.domain.scanner.SignatureDatabaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

class HypatiaDatabaseRepository(
    context: Context,
    private val scanner: HypatiaSignatureScanner,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .followRedirects(true)
        .build(),
    private val verifier: HypatiaGpgVerifier = HypatiaGpgVerifier(),
    private val baseUrl: String = HypatiaConstants.DATABASE_BASE_URL,
) : SignatureDatabaseRepository {
    private val filesDir = File(context.filesDir, "signatures").apply { mkdirs() }
    private val prefs = context.getSharedPreferences("hypatia_db", Context.MODE_PRIVATE)
    private val mutex = Mutex()
    private val _status = MutableStateFlow(readStatus())
    val statusFlow: StateFlow<DatabaseStatus> = _status

    override fun status(): DatabaseStatus = _status.value

    override suspend fun load(force: Boolean) {
        mutex.withLock {
            val current = readStatus()
            _status.value = current.copy()
            if (!current.available) {
                scanner.replaceFilters(
                    filters = null,
                    verified = false,
                    lastUpdated = current.lastSuccessEpochMs,
                    reason = current.lastError ?: "Download the Hypatia signature databases in Settings before scanning files.",
                )
                return
            }
            try {
                val filters = withContext(Dispatchers.IO) {
                    val publicKey = File(filesDir, HypatiaConstants.PUBLIC_KEY_FILE)
                    HypatiaConstants.DATABASE_FILES.forEach { name ->
                        val data = File(filesDir, name)
                        val sig = File(filesDir, "$name.sig")
                        if (!sig.exists() || !verifier.verify(data, sig, publicKey)) {
                            throw IllegalStateException("GPG verification failed for $name")
                        }
                    }
                    HypatiaBloomStore.loadFromDirectory(filesDir, "Hypatia MaintainTeam")
                }
                val selfTest = HypatiaBloomStore.passesSelfTest(filters)
                if (!selfTest) {
                    scanner.replaceFilters(
                        filters = null,
                        verified = false,
                        lastUpdated = current.lastSuccessEpochMs,
                        reason = "Database self-test failed. Official Hypatia canary hashes were not present.",
                    )
                    _status.value = current.copy(
                        verified = false,
                        lastError = "Self-test failed for loaded BloomFilters.",
                    )
                    return
                }
                if (prefs.getLong(KEY_LAST_SUCCESS, 0L) == 0L) {
                    prefs.edit().putLong(KEY_LAST_SUCCESS, System.currentTimeMillis()).apply()
                }
                scanner.replaceFilters(
                    filters = filters,
                    verified = true,
                    lastUpdated = current.lastSuccessEpochMs ?: System.currentTimeMillis(),
                    reason = null,
                )
                _status.value = readStatus().copy(
                    signatureCount = filters.approximateCount,
                    lastError = null,
                    verified = true,
                )
            } catch (t: Throwable) {
                scanner.replaceFilters(
                    filters = null,
                    verified = false,
                    lastUpdated = current.lastSuccessEpochMs,
                    reason = "Failed to load BloomFilters: ${t.message}",
                )
                _status.value = current.copy(lastError = t.message)
            }
        }
    }

    override suspend fun update(onProgress: (String) -> Unit) {
        mutex.withLock {
            _status.value = readStatus().copy(updating = true, lastError = null)
            try {
                withContext(Dispatchers.IO) {
                    download("gpg.key", onProgress)
                    HypatiaConstants.DATABASE_FILES.forEach { name ->
                        download(name, onProgress)
                        download("$name.sig", onProgress)
                    }
                    onProgress("Verifying GPG signatures")
                    val publicKey = File(filesDir, HypatiaConstants.PUBLIC_KEY_FILE)
                    if (!publicKey.exists()) {
                        throw IllegalStateException("Public key gpg.key was not downloaded.")
                    }
                    HypatiaConstants.DATABASE_FILES.forEach { name ->
                        val data = File(filesDir, name)
                        val sig = File(filesDir, "$name.sig")
                        if (!verifier.verify(data, sig, publicKey)) {
                            data.delete()
                            throw IllegalStateException("GPG verification failed for $name. File discarded.")
                        }
                    }
                }
                prefs.edit().putLong(KEY_LAST_SUCCESS, System.currentTimeMillis()).apply()
                _status.value = readStatus().copy(updating = false, lastError = null)
            } catch (t: Throwable) {
                _status.value = readStatus().copy(updating = false, lastError = t.message)
                throw t
            }
        }
        load(force = true)
    }

    private fun download(name: String, onProgress: (String) -> Unit) {
        onProgress("Downloading $name")
        val target = File(filesDir, name)
        val temp = File(filesDir, "$name.new")
        val request = Request.Builder()
            .url(baseUrl + name)
            .header("User-Agent", HypatiaConstants.USER_AGENT)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Download $name failed: HTTP ${response.code}")
            }
            val body = response.body ?: throw IllegalStateException("Empty body for $name")
            temp.outputStream().use { out ->
                body.byteStream().copyTo(out)
            }
        }
        if (target.exists()) target.delete()
        if (!temp.renameTo(target)) {
            throw IllegalStateException("Could not replace $name")
        }
    }

    private fun readStatus(): DatabaseStatus {
        val files = HypatiaConstants.DATABASE_FILES.map { name ->
            val data = File(filesDir, name)
            val sig = File(filesDir, "$name.sig")
            DatabaseFileStatus(
                name = name,
                present = data.exists() && data.length() > 0,
                verified = data.exists() && sig.exists(),
                bytes = if (data.exists()) data.length() else 0L,
                lastModifiedEpochMs = if (data.exists()) data.lastModified() else null,
            )
        }
        val keyPresent = File(filesDir, HypatiaConstants.PUBLIC_KEY_FILE).exists()
        val allPresent = files.all { it.present } && keyPresent
        val allSigned = files.all { it.verified } && keyPresent
        return DatabaseStatus(
            available = allPresent,
            verified = allPresent && allSigned && prefs.getLong(KEY_LAST_SUCCESS, 0L) > 0L,
            updating = false,
            files = files,
            lastSuccessEpochMs = prefs.getLong(KEY_LAST_SUCCESS, 0L).takeIf { it > 0L },
            lastError = null,
            sourceUrl = baseUrl,
            signatureCount = scanner.availability().signatureCount,
        )
    }

    companion object {
        private const val KEY_LAST_SUCCESS = "last_success"
    }
}
