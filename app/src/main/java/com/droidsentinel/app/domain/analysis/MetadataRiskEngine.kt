package com.droidsentinel.app.domain.analysis

import com.droidsentinel.app.domain.model.AnalysisSource
import com.droidsentinel.app.domain.model.InstalledAppInfo
import com.droidsentinel.app.domain.model.ScanFinding
import com.droidsentinel.app.domain.model.Severity
import java.util.UUID

class MetadataRiskEngine {
    fun analyze(app: InstalledAppInfo): List<ScanFinding> {
        val findings = mutableListOf<ScanFinding>()
        val permissions = app.requestedPermissions.toSet()

        if (app.hasAccessibilityService) {
            findings += finding(
                ruleId = "META_ACCESSIBILITY",
                title = "Accessibility service declared",
                severity = Severity.HIGH,
                evidence = listOf("${app.packageName} declares an accessibility service"),
                explanation = "Accessibility services can observe screen contents and perform actions on a user's behalf. This is legitimate for some assistive apps, but it is also a common malware capability.",
                recommendedAction = "Open the app's system settings and confirm you intentionally granted Accessibility access. Uninstall the app if you do not recognize it.",
            )
        }

        if (app.isDeviceAdmin) {
            findings += finding(
                ruleId = "META_DEVICE_ADMIN",
                title = "Device administration capability",
                severity = Severity.HIGH,
                evidence = listOf("${app.packageName} can request device-admin privileges"),
                explanation = "Device-admin apps can impose lock-screen policies and are harder to remove. Only keep this if you installed a trusted device-management app.",
                recommendedAction = "Review Device admin apps in system settings and disable any you do not recognize.",
            )
        }

        val sms = permissions.filter { it.contains("SMS") || it.contains("RECEIVE_MMS") || it.endsWith("RECEIVE_WAP_PUSH") }
        if (sms.isNotEmpty()) {
            findings += finding(
                ruleId = "META_SMS",
                title = "SMS-related permissions",
                severity = if (app.hasAccessibilityService) Severity.HIGH else Severity.MEDIUM,
                evidence = sms,
                explanation = "SMS permissions can be used to read one-time codes or send premium messages. Banks and messengers may need them; unknown apps usually do not.",
                recommendedAction = "If this app is not your messenger or bank, deny SMS access and consider uninstalling it.",
            )
        }

        val phone = permissions.filter {
            it.endsWith("READ_PHONE_STATE") || it.endsWith("CALL_PHONE") || it.endsWith("PROCESS_OUTGOING_CALLS") || it.endsWith("READ_CALL_LOG")
        }
        if (phone.isNotEmpty() && sms.isNotEmpty()) {
            findings += finding(
                ruleId = "META_SMS_PHONE_COMBO",
                title = "SMS and phone permissions together",
                severity = Severity.HIGH,
                evidence = sms + phone,
                explanation = "The combination of SMS and phone-state access is a classic pattern for spyware and premium-rate fraud. It is not proof of malware, but it deserves a closer look.",
                recommendedAction = "Confirm the developer and the app's purpose. If the app is not a phone or messaging client, uninstall it.",
            )
        }

        val micCam = permissions.filter { it.endsWith("RECORD_AUDIO") || it.endsWith("CAMERA") }
        if (micCam.size >= 2 && !looksLikeCommunicationApp(app)) {
            findings += finding(
                ruleId = "META_MIC_CAMERA",
                title = "Microphone and camera access",
                severity = Severity.MEDIUM,
                evidence = micCam,
                explanation = "Camera plus microphone access can enable covert recording. Video apps need this; utilities usually do not.",
                recommendedAction = "Review whether this app has a reason to capture audio and video. Revoke unused permissions.",
            )
        }

        val location = permissions.any { it.contains("LOCATION") }
        val contacts = permissions.any { it.endsWith("READ_CONTACTS") || it.endsWith("READ_PHONE_NUMBERS") }
        if (location && contacts && app.hasAccessibilityService) {
            findings += finding(
                ruleId = "META_SURVEILLANCE_CLUSTER",
                title = "Broad surveillance-capable permission cluster",
                severity = Severity.CRITICAL,
                evidence = listOf("Accessibility service") + permissions.filter {
                    it.contains("LOCATION") || it.endsWith("READ_CONTACTS") || it.contains("SMS")
                },
                explanation = "Accessibility plus contacts and location is a high-risk combination. This still does not prove malware, but it is a strong reason to inspect the app before leaving those permissions granted.",
                recommendedAction = "If you did not install this app for accessibility or parental-control reasons, uninstall it immediately.",
            )
        }

        val storage = permissions.filter {
            it.endsWith("READ_EXTERNAL_STORAGE") || it.endsWith("WRITE_EXTERNAL_STORAGE") || it.endsWith("MANAGE_EXTERNAL_STORAGE")
        }
        if (storage.any { it.endsWith("MANAGE_EXTERNAL_STORAGE") }) {
            findings += finding(
                ruleId = "META_ALL_FILES",
                title = "All-files access",
                severity = Severity.MEDIUM,
                evidence = storage,
                explanation = "All-files access lets an app read most shared storage. File managers may need it; most apps should use the system picker instead.",
                recommendedAction = "Revoke All files access unless this is a file manager you trust.",
            )
        }

        if (app.installerPackage.isNullOrBlank() && !app.isSystemApp) {
            findings += finding(
                ruleId = "META_UNKNOWN_INSTALLER",
                title = "Installer source unavailable",
                severity = Severity.LOW,
                evidence = listOf("Installer package is unknown"),
                explanation = "Android could not report which store installed this app. Sideloaded APKs are not automatically malicious, but they skip Play Protect review.",
                recommendedAction = "Prefer apps from a trusted store. If you did not sideload this APK, inspect it more carefully.",
            )
        } else if (app.installerPackage in SIDELOAD_INSTALLERS) {
            findings += finding(
                ruleId = "META_SIDELOADED",
                title = "Installed outside a major store",
                severity = Severity.MEDIUM,
                evidence = listOf("Installer: ${app.installerPackage}"),
                explanation = "This app was installed by a package installer rather than a major app store. Sideloading is a common malware delivery path.",
                recommendedAction = "Only keep sideloaded apps from people or projects you trust. Scan the APK if you still have the original file.",
            )
        }

        if (app.targetSdk != 0 && app.targetSdk < 26 && !app.isSystemApp) {
            findings += finding(
                ruleId = "META_OLD_TARGET_SDK",
                title = "Very old target SDK",
                severity = Severity.LOW,
                evidence = listOf("targetSdk=${app.targetSdk}"),
                explanation = "Apps targeting very old Android versions miss years of platform security improvements. Some malware also avoids newer platform restrictions this way.",
                recommendedAction = "Look for an updated version. If the developer has abandoned the app, consider replacing it.",
            )
        }

        if (app.signingCertSha256.isEmpty() && app.apkReadable) {
            findings += finding(
                ruleId = "META_NO_CERT",
                title = "Signing certificate could not be read",
                severity = Severity.LOW,
                evidence = listOf("No signing certificates were returned"),
                explanation = "Android did not expose a signing certificate for this package. That is unusual and may be a platform limitation rather than a threat.",
                recommendedAction = "Re-scan after granting package visibility. Treat the result as incomplete, not clean.",
            )
        }

        return findings
    }

    fun highestSeverity(findings: List<ScanFinding>): Severity {
        return findings.maxByOrNull { it.severity.ordinal }?.severity ?: Severity.INFO
    }

    private fun looksLikeCommunicationApp(app: InstalledAppInfo): Boolean {
        val blob = (app.packageName + app.label).lowercase()
        return listOf("camera", "photo", "video", "meet", "chat", "call", "messenger", "zoom", "teams").any { it in blob }
    }

    private fun finding(
        ruleId: String,
        title: String,
        severity: Severity,
        evidence: List<String>,
        explanation: String,
        recommendedAction: String,
    ): ScanFinding = ScanFinding(
        id = UUID.randomUUID().toString(),
        ruleId = ruleId,
        title = title,
        severity = severity,
        evidence = evidence,
        explanation = explanation,
        recommendedAction = recommendedAction,
        confidence = "Contextual indicator, not a malware verdict",
        source = AnalysisSource.METADATA,
    )

    companion object {
        private val SIDELOAD_INSTALLERS = setOf(
            "com.google.android.packageinstaller",
            "com.android.packageinstaller",
            "com.samsung.android.packageinstaller",
        )
    }
}
