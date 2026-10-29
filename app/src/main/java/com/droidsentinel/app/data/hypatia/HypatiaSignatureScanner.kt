package com.droidsentinel.app.data.hypatia

import com.droidsentinel.app.domain.hash.FileHasher
import com.droidsentinel.app.domain.hash.FileTooLargeException
import com.droidsentinel.app.domain.hash.ScanCancelledException
import com.droidsentinel.app.domain.model.FileHashes
import com.droidsentinel.app.domain.model.ScannerAvailability
import com.droidsentinel.app.domain.model.SignatureMatch
import com.droidsentinel.app.domain.model.SignatureScanOutcome
import com.droidsentinel.app.domain.model.SignatureScanStatus
import com.droidsentinel.app.domain.scanner.SignatureScanner
import java.io.InputStream
import java.util.concurrent.atomic.AtomicReference

class HypatiaSignatureScanner : SignatureScanner {
    private val loaded = AtomicReference<LoadedBloomFilters?>(null)
    private val availabilityState = AtomicReference(
        ScannerAvailability(
            engineReady = true,
            databaseAvailable = false,
            databaseVerified = false,
            databaseLabel = null,
            lastUpdatedEpochMs = null,
            signatureCount = null,
            reason = "No signature database has been loaded yet.",
        ),
    )

    fun replaceFilters(filters: LoadedBloomFilters?, verified: Boolean, lastUpdated: Long?, reason: String?) {
        loaded.set(filters)
        availabilityState.set(
            ScannerAvailability(
                engineReady = true,
                databaseAvailable = filters != null,
                databaseVerified = verified && filters != null,
                databaseLabel = filters?.sourceLabel,
                lastUpdatedEpochMs = lastUpdated,
                signatureCount = filters?.approximateCount,
                reason = reason,
            ),
        )
    }

    override fun availability(): ScannerAvailability = availabilityState.get()

    override fun scanHashes(hashes: FileHashes): SignatureScanOutcome {
        val filters = loaded.get()
        val availability = availability()
        if (filters == null || !availability.databaseAvailable) {
            return SignatureScanOutcome(
                status = SignatureScanStatus.DATABASE_UNAVAILABLE,
                hashes = hashes,
                message = availability.reason ?: "Signature database is not available.",
            )
        }
        if (!availability.databaseVerified) {
            return SignatureScanOutcome(
                status = SignatureScanStatus.DATABASE_UNAVAILABLE,
                hashes = hashes,
                databaseLabel = filters.sourceLabel,
                signatureCount = filters.approximateCount,
                verified = false,
                message = "Signature database is present but not verified. It will not be used for detections.",
            )
        }

        val matches = mutableListOf<SignatureMatch>()
        if (hashes.md5 != HypatiaConstants.EMPTY_MD5 && filters.md5.mightContain(hashes.md5)) {
            matches += SignatureMatch("MD5", hashes.md5, "hypatia-md5-bloom.bin")
        }
        if (hashes.sha1 != HypatiaConstants.EMPTY_SHA1 && filters.sha1.mightContain(hashes.sha1)) {
            matches += SignatureMatch("SHA-1", hashes.sha1, "hypatia-sha1-bloom.bin")
        }
        if (hashes.sha256 != HypatiaConstants.EMPTY_SHA256 && filters.sha256.mightContain(hashes.sha256)) {
            matches += SignatureMatch("SHA-256", hashes.sha256, "hypatia-sha256-bloom.bin")
        }

        return if (matches.isEmpty()) {
            SignatureScanOutcome(
                status = SignatureScanStatus.NO_MATCH_FOUND,
                hashes = hashes,
                databaseLabel = filters.sourceLabel,
                signatureCount = filters.approximateCount,
                verified = true,
                message = "No Hypatia signature match was found for this file.",
            )
        } else {
            SignatureScanOutcome(
                status = SignatureScanStatus.THREAT_DETECTED,
                matches = matches,
                hashes = hashes,
                databaseLabel = filters.sourceLabel,
                signatureCount = filters.approximateCount,
                verified = true,
                message = "Hypatia BloomFilter reported a possible signature match. This is a probabilistic hit, not a malware family name.",
            )
        }
    }

    override fun hashStream(stream: InputStream, declaredSize: Long?, cancelled: () -> Boolean): FileHashes {
        return FileHasher.hash(stream, declaredSize, cancelled)
    }

    fun mapHashFailure(throwable: Throwable, hashes: FileHashes? = null): SignatureScanOutcome {
        return when (throwable) {
            is ScanCancelledException -> SignatureScanOutcome(
                status = SignatureScanStatus.SCAN_INCOMPLETE,
                hashes = hashes,
                message = "Scan was cancelled.",
            )
            is FileTooLargeException -> SignatureScanOutcome(
                status = SignatureScanStatus.NOT_SCANNED,
                hashes = hashes,
                message = "File exceeds the ${FileHasher.MAX_SCAN_BYTES} byte scan limit.",
            )
            else -> SignatureScanOutcome(
                status = SignatureScanStatus.SCAN_FAILED,
                hashes = hashes,
                message = throwable.message ?: "Hashing failed.",
            )
        }
    }
}
