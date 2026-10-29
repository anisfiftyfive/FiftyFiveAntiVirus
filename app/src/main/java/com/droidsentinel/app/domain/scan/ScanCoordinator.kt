package com.droidsentinel.app.domain.scan

import com.droidsentinel.app.data.apk.ApkInspector
import com.droidsentinel.app.data.apps.InstalledAppRepository
import com.droidsentinel.app.data.hypatia.HypatiaSignatureScanner
import com.droidsentinel.app.domain.analysis.MetadataRiskEngine
import com.droidsentinel.app.domain.model.AnalysisSource
import com.droidsentinel.app.domain.model.AppScanSummary
import com.droidsentinel.app.domain.model.ScanFinding
import com.droidsentinel.app.domain.model.ScanProgress
import com.droidsentinel.app.domain.model.ScanResult
import com.droidsentinel.app.domain.model.ScanStage
import com.droidsentinel.app.domain.model.ScanTargetType
import com.droidsentinel.app.domain.model.Severity
import com.droidsentinel.app.domain.model.SignatureScanOutcome
import com.droidsentinel.app.domain.model.SignatureScanStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.util.UUID

class ScanCoordinator(
    private val apps: InstalledAppRepository,
    private val scanner: HypatiaSignatureScanner,
    private val metadata: MetadataRiskEngine,
    private val apkInspector: ApkInspector,
) {
    private val _progress = MutableStateFlow(ScanProgress(ScanStage.PREPARING))
    val progress: StateFlow<ScanProgress> = _progress
    @Volatile var cancelled: Boolean = false

    fun cancel() {
        cancelled = true
        _progress.value = ScanProgress(ScanStage.CANCELLED, label = "Cancelling")
    }

    suspend fun scanInstalledApps(): ScanResult = withContext(Dispatchers.Default) {
        cancelled = false
        val started = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        emit(ScanStage.PREPARING, label = "Preparing installed-app inventory")
        val installed = apps.listInstalledApps()
        val limitations = mutableListOf<String>()
        if (apps.visibilityLimited(installed.size)) {
            limitations += "Android package visibility is limited. DroidSentinel can only inspect apps that the system exposed to it."
        }

        val availability = scanner.availability()
        if (!availability.databaseAvailable || !availability.databaseVerified) {
            limitations += availability.reason ?: "Signature scanning is unavailable until a verified Hypatia database is loaded."
        }

        val summaries = mutableListOf<AppScanSummary>()
        var scanned = 0
        var skipped = 0
        var failed = 0
        installed.forEachIndexed { index, app ->
            if (cancelled) {
                return@withContext buildDeviceResult(
                    id, started, summaries, scanned, skipped, failed, limitations, cancelled = true,
                )
            }
            emit(
                ScanStage.READING_METADATA,
                index + 1,
                installed.size,
                "Inspecting ${app.label}",
                app.packageName,
            )
            val findings = metadata.analyze(app)
            val signature = scanApkIfPossible(app.apkPath, app.apkReadable, app.apkSizeBytes) { stage, label ->
                emit(stage, index + 1, installed.size, label, app.packageName)
            }
            when (signature.status) {
                SignatureScanStatus.NO_MATCH_FOUND, SignatureScanStatus.THREAT_DETECTED -> scanned += 1
                SignatureScanStatus.NOT_SCANNED, SignatureScanStatus.DATABASE_UNAVAILABLE -> skipped += 1
                SignatureScanStatus.SCAN_FAILED, SignatureScanStatus.SCAN_INCOMPLETE -> failed += 1
            }
            summaries += AppScanSummary(
                app = app,
                signature = signature,
                findings = findings + signatureFindings(app.label, signature),
                metadataRisk = metadata.highestSeverity(findings),
            )
        }

        emit(ScanStage.COMPLETING, installed.size, installed.size, "Summarising results", summaries.lastOrNull()?.app?.packageName)
        buildDeviceResult(id, started, summaries, scanned, skipped, failed, limitations, cancelled = false)
    }

    suspend fun scanApk(uri: android.net.Uri): ScanResult = withContext(Dispatchers.Default) {
        cancelled = false
        val started = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        emit(ScanStage.PREPARING, label = "Opening selected APK")
        val meta = apkInspector.inspect(uri)
        val findings = mutableListOf<ScanFinding>()
        meta.notes.forEach { note ->
            findings += ScanFinding(
                id = UUID.randomUUID().toString(),
                ruleId = "APK_STRUCTURE",
                title = "APK structure note",
                severity = if (meta.malformed) Severity.MEDIUM else Severity.INFO,
                evidence = listOf(note),
                explanation = note,
                recommendedAction = "Use a file you trust. Malformed APKs should not be installed.",
                source = AnalysisSource.METADATA,
            )
        }
        emit(ScanStage.HASHING, label = "Hashing ${meta.displayName}")
        val signature = try {
            apkInspector.open(uri).use { stream ->
                val hashes = scanner.hashStream(stream, meta.sizeBytes) { cancelled }
                emit(ScanStage.CHECKING_SIGNATURES, label = "Checking Hypatia signatures")
                scanner.scanHashes(hashes)
            }
        } catch (t: Throwable) {
            scanner.mapHashFailure(t)
        }
        findings += signatureFindings(meta.displayName, signature)
        val completed = System.currentTimeMillis()
        ScanResult(
            id = id,
            targetName = meta.displayName,
            targetPackage = null,
            targetType = ScanTargetType.APK_FILE,
            startedAt = started,
            completedAt = completed,
            durationMs = completed - started,
            signatureStatus = signature.status,
            databaseLabel = signature.databaseLabel,
            databaseVerified = signature.verified,
            signatureCount = signature.signatureCount,
            hashes = signature.hashes,
            findings = findings,
            appSummaries = emptyList(),
            appsInspected = 0,
            appsSignatureScanned = if (signature.status == SignatureScanStatus.NO_MATCH_FOUND || signature.status == SignatureScanStatus.THREAT_DETECTED) 1 else 0,
            appsSkipped = if (signature.status == SignatureScanStatus.NOT_SCANNED || signature.status == SignatureScanStatus.DATABASE_UNAVAILABLE) 1 else 0,
            appsFailed = if (signature.status == SignatureScanStatus.SCAN_FAILED) 1 else 0,
            errorMessage = signature.message.takeIf { signature.status == SignatureScanStatus.SCAN_FAILED },
            cancelled = cancelled,
            limitations = listOfNotNull(
                "Selected using the system document picker. The APK was not uploaded.",
                signature.message.takeIf { signature.status != SignatureScanStatus.THREAT_DETECTED && signature.status != SignatureScanStatus.NO_MATCH_FOUND },
            ),
        )
    }

    private fun scanApkIfPossible(
        path: String?,
        readable: Boolean,
        size: Long?,
        onStage: (ScanStage, String) -> Unit,
    ): SignatureScanOutcome {
        if (path.isNullOrBlank() || !readable) {
            return SignatureScanOutcome(
                status = SignatureScanStatus.NOT_SCANNED,
                message = "The installed APK is not readable, so it was inspected from metadata only.",
            )
        }
        val file = File(path)
        if (!file.exists()) {
            return SignatureScanOutcome(
                status = SignatureScanStatus.NOT_SCANNED,
                message = "APK path is no longer available.",
            )
        }
        return try {
            onStage(ScanStage.HASHING, "Hashing ${file.name}")
            FileInputStream(file).use { stream ->
                val hashes = scanner.hashStream(stream, size) { cancelled }
                onStage(ScanStage.CHECKING_SIGNATURES, "Checking signatures for ${file.name}")
                scanner.scanHashes(hashes)
            }
        } catch (t: Throwable) {
            scanner.mapHashFailure(t)
        }
    }

    private fun signatureFindings(label: String, outcome: SignatureScanOutcome): List<ScanFinding> {
        return when (outcome.status) {
            SignatureScanStatus.THREAT_DETECTED -> listOf(
                ScanFinding(
                    id = UUID.randomUUID().toString(),
                    ruleId = "HYPATIA_MATCH",
                    title = "Possible Hypatia signature match",
                    severity = Severity.CRITICAL,
                    evidence = outcome.matches.map { "${it.hashType}: ${it.hash} in ${it.databaseName}" },
                    explanation = "The Hypatia BloomFilter reported that a file hash might be present in the malware signature set. BloomFilters can produce rare false positives and do not name a malware family.",
                    recommendedAction = "Do not install or keep this app if you were not expecting it. Compare the SHA-256 hash with an independent source and uninstall if you cannot verify it.",
                    confidence = "Probabilistic signature match",
                    source = AnalysisSource.SIGNATURE,
                ),
            )
            SignatureScanStatus.NO_MATCH_FOUND -> emptyList()
            else -> emptyList()
        }
    }

    private fun buildDeviceResult(
        id: String,
        started: Long,
        summaries: List<AppScanSummary>,
        scanned: Int,
        skipped: Int,
        failed: Int,
        limitations: List<String>,
        cancelled: Boolean,
    ): ScanResult {
        val completed = System.currentTimeMillis()
        val signatureStatus = when {
            cancelled -> SignatureScanStatus.SCAN_INCOMPLETE
            summaries.any { it.signature.status == SignatureScanStatus.THREAT_DETECTED } -> SignatureScanStatus.THREAT_DETECTED
            scanned == 0 && summaries.any { it.signature.status == SignatureScanStatus.DATABASE_UNAVAILABLE } -> SignatureScanStatus.DATABASE_UNAVAILABLE
            scanned == 0 && failed > 0 -> SignatureScanStatus.SCAN_FAILED
            scanned == 0 -> SignatureScanStatus.NOT_SCANNED
            else -> SignatureScanStatus.NO_MATCH_FOUND
        }
        val topFindings = summaries.flatMap { it.findings }
            .sortedByDescending { it.severity.ordinal }
            .take(40)
        return ScanResult(
            id = id,
            targetName = "Installed apps",
            targetPackage = null,
            targetType = ScanTargetType.DEVICE,
            startedAt = started,
            completedAt = completed,
            durationMs = completed - started,
            signatureStatus = signatureStatus,
            databaseLabel = scanner.availability().databaseLabel,
            databaseVerified = scanner.availability().databaseVerified,
            signatureCount = scanner.availability().signatureCount,
            hashes = null,
            findings = topFindings,
            appSummaries = summaries,
            appsInspected = summaries.size,
            appsSignatureScanned = scanned,
            appsSkipped = skipped,
            appsFailed = failed,
            errorMessage = if (cancelled) "Scan cancelled" else null,
            cancelled = cancelled,
            limitations = limitations,
        )
    }

    private fun emit(
        stage: ScanStage,
        current: Int = 0,
        total: Int = 0,
        label: String,
        packageName: String? = null,
    ) {
        _progress.value = ScanProgress(stage, current, total, label, packageName)
    }
}
