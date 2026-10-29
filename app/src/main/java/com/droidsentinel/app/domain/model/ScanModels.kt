package com.droidsentinel.app.domain.model

data class FileHashes(
    val md5: String,
    val sha1: String,
    val sha256: String,
    val byteCount: Long,
)

enum class SignatureScanStatus {
    THREAT_DETECTED,
    NO_MATCH_FOUND,
    NOT_SCANNED,
    SCAN_FAILED,
    DATABASE_UNAVAILABLE,
    SCAN_INCOMPLETE,
}

enum class Severity {
    INFO,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL,
    ;

    fun atLeast(other: Severity): Boolean = ordinal >= other.ordinal
}

enum class AnalysisSource {
    SIGNATURE,
    METADATA,
    AI_EXPLANATION,
}

enum class ScanTargetType {
    INSTALLED_APP,
    APK_FILE,
    DEVICE,
}

enum class ScanStage {
    PREPARING,
    LOADING_DATABASE,
    READING_METADATA,
    HASHING,
    CHECKING_SIGNATURES,
    ANALYZING_METADATA,
    COMPLETING,
    CANCELLED,
}

data class ScanFinding(
    val id: String,
    val ruleId: String,
    val title: String,
    val severity: Severity,
    val evidence: List<String>,
    val explanation: String,
    val recommendedAction: String,
    val confidence: String? = null,
    val source: AnalysisSource,
)

data class SignatureMatch(
    val hashType: String,
    val hash: String,
    val databaseName: String,
)

data class SignatureScanOutcome(
    val status: SignatureScanStatus,
    val matches: List<SignatureMatch> = emptyList(),
    val hashes: FileHashes? = null,
    val databaseLabel: String? = null,
    val signatureCount: Long? = null,
    val verified: Boolean = false,
    val message: String? = null,
)

data class InstalledAppInfo(
    val packageName: String,
    val label: String,
    val versionName: String?,
    val versionCode: Long,
    val targetSdk: Int,
    val minSdk: Int?,
    val installerPackage: String?,
    val requestedPermissions: List<String>,
    val hasAccessibilityService: Boolean,
    val isDeviceAdmin: Boolean,
    val apkPath: String?,
    val apkReadable: Boolean,
    val apkSizeBytes: Long?,
    val signingCertSha256: List<String>,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val isSystemApp: Boolean,
    val isSelf: Boolean,
)

data class AppScanSummary(
    val app: InstalledAppInfo,
    val signature: SignatureScanOutcome,
    val findings: List<ScanFinding>,
    val metadataRisk: Severity,
)

data class ScanProgress(
    val stage: ScanStage,
    val current: Int = 0,
    val total: Int = 0,
    val label: String = "",
    val packageName: String? = null,
)

data class ScanResult(
    val id: String,
    val targetName: String,
    val targetPackage: String?,
    val targetType: ScanTargetType,
    val startedAt: Long,
    val completedAt: Long?,
    val durationMs: Long,
    val signatureStatus: SignatureScanStatus,
    val databaseLabel: String?,
    val databaseVerified: Boolean,
    val signatureCount: Long?,
    val hashes: FileHashes?,
    val findings: List<ScanFinding>,
    val appSummaries: List<AppScanSummary>,
    val appsInspected: Int,
    val appsSignatureScanned: Int,
    val appsSkipped: Int,
    val appsFailed: Int,
    val errorMessage: String?,
    val cancelled: Boolean,
    val limitations: List<String>,
) {
    val threatCount: Int
        get() = findings.count { it.source == AnalysisSource.SIGNATURE && it.severity.atLeast(Severity.HIGH) } +
            appSummaries.count { it.signature.status == SignatureScanStatus.THREAT_DETECTED }

    val metadataFindingCount: Int
        get() = findings.count { it.source == AnalysisSource.METADATA } +
            appSummaries.sumOf { summary -> summary.findings.count { it.source == AnalysisSource.METADATA } }
}

data class ScannerAvailability(
    val engineReady: Boolean,
    val databaseAvailable: Boolean,
    val databaseVerified: Boolean,
    val databaseLabel: String?,
    val lastUpdatedEpochMs: Long?,
    val signatureCount: Long?,
    val reason: String?,
)

data class DatabaseStatus(
    val available: Boolean,
    val verified: Boolean,
    val updating: Boolean,
    val files: List<DatabaseFileStatus>,
    val lastSuccessEpochMs: Long?,
    val lastError: String?,
    val sourceUrl: String,
    val signatureCount: Long?,
)

data class DatabaseFileStatus(
    val name: String,
    val present: Boolean,
    val verified: Boolean,
    val bytes: Long,
    val lastModifiedEpochMs: Long?,
)
