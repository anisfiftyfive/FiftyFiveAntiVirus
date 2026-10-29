package com.droidsentinel.app.domain.analysis

import com.droidsentinel.app.domain.model.InstalledAppInfo
import com.droidsentinel.app.domain.model.Severity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MetadataRiskEngineTest {
    private val engine = MetadataRiskEngine()

    @Test
    fun emptyPermissionsProduceNoHighRisk() {
        val findings = engine.analyze(baseApp())
        assertTrue(findings.none { it.severity.atLeast(Severity.HIGH) })
        assertTrue(findings.none { it.title.contains("malware", ignoreCase = true) })
    }

    @Test
    fun smsAndAccessibilityIsHighButNotMalwareVerdict() {
        val findings = engine.analyze(
            baseApp(
                permissions = listOf("android.permission.RECEIVE_SMS", "android.permission.READ_SMS"),
                accessibility = true,
            ),
        )
        assertTrue(findings.any { it.ruleId == "META_ACCESSIBILITY" })
        assertTrue(findings.any { it.ruleId == "META_SMS" })
        assertTrue(findings.none { it.explanation.contains("is malware") })
    }

    @Test
    fun legitimateLookingCameraAppIsNotAutomaticallyMalicious() {
        val findings = engine.analyze(
            baseApp(
                packageName = "com.example.camera",
                label = "Camera",
                permissions = listOf("android.permission.CAMERA", "android.permission.RECORD_AUDIO"),
            ),
        )
        assertTrue(findings.none { it.ruleId == "META_MIC_CAMERA" })
    }

    @Test
    fun malformedMissingPackageStillReturnsDeterministicResult() {
        val findings = engine.analyze(baseApp(packageName = "", label = "", permissions = emptyList()))
        assertTrue(findings.all { it.ruleId.isNotBlank() })
    }

    @Test
    fun sideloadedInstallerIsMedium() {
        val findings = engine.analyze(
            baseApp(installer = "com.google.android.packageinstaller", system = false),
        )
        assertEquals(Severity.MEDIUM, findings.first { it.ruleId == "META_SIDELOADED" }.severity)
    }

    private fun baseApp(
        packageName: String = "com.example.safe",
        label: String = "Safe App",
        permissions: List<String> = emptyList(),
        accessibility: Boolean = false,
        installer: String? = "com.android.vending",
        system: Boolean = false,
    ) = InstalledAppInfo(
        packageName = packageName,
        label = label,
        versionName = "1.0",
        versionCode = 1,
        targetSdk = 34,
        minSdk = 26,
        installerPackage = installer,
        requestedPermissions = permissions,
        hasAccessibilityService = accessibility,
        isDeviceAdmin = false,
        apkPath = "/data/app/base.apk",
        apkReadable = true,
        apkSizeBytes = 1000,
        signingCertSha256 = listOf("AA"),
        firstInstallTime = 1,
        lastUpdateTime = 1,
        isSystemApp = system,
        isSelf = false,
    )
}
