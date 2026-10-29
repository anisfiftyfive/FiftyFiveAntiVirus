package com.droidsentinel.app.domain.scanner

import com.droidsentinel.app.domain.model.FileHashes
import com.droidsentinel.app.domain.model.ScannerAvailability
import com.droidsentinel.app.domain.model.SignatureScanOutcome
import java.io.InputStream

interface SignatureScanner {
    fun availability(): ScannerAvailability

    fun scanHashes(hashes: FileHashes): SignatureScanOutcome

    fun hashStream(stream: InputStream, declaredSize: Long? = null, cancelled: () -> Boolean = { false }): FileHashes
}

interface SignatureDatabaseRepository {
    fun status(): com.droidsentinel.app.domain.model.DatabaseStatus
    suspend fun load(force: Boolean = false)
    suspend fun update(onProgress: (String) -> Unit = {})
}
