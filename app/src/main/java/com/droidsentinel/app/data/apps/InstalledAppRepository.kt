package com.droidsentinel.app.data.apps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.droidsentinel.app.domain.hash.FileHasher
import com.droidsentinel.app.domain.model.InstalledAppInfo
import java.io.File
import java.security.MessageDigest
import java.util.Locale

class InstalledAppRepository(private val context: Context) {
    private val packageManager = context.packageManager

    fun listInstalledApps(): List<InstalledAppInfo> {
        val flags = PackageManager.GET_PERMISSIONS or
            PackageManager.GET_SERVICES or
            PackageManager.GET_RECEIVERS or
            signingFlags()
        val packages = try {
            packageManager.getInstalledPackages(flags)
        } catch (t: Throwable) {
            emptyList()
        }
        return packages.map { toInfo(it) }.sortedBy { it.label.lowercase(Locale.getDefault()) }
    }

    fun getApp(packageName: String): InstalledAppInfo? {
        return try {
            val flags = PackageManager.GET_PERMISSIONS or
                PackageManager.GET_SERVICES or
                PackageManager.GET_RECEIVERS or
                signingFlags()
            toInfo(packageManager.getPackageInfo(packageName, flags))
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }

    fun visibilityLimited(listedCount: Int): Boolean = listedCount <= 2

    private fun toInfo(info: PackageInfo): InstalledAppInfo {
        val applicationInfo = info.applicationInfo
        val apkPath = applicationInfo?.sourceDir ?: applicationInfo?.publicSourceDir
        val apkFile = apkPath?.let { File(it) }
        val readable = apkFile?.canRead() == true && (apkFile.length() in 1..FileHasher.MAX_SCAN_BYTES)
        val label = applicationInfo?.loadLabel(packageManager)?.toString() ?: info.packageName
        return InstalledAppInfo(
            packageName = info.packageName,
            label = label,
            versionName = info.versionName,
            versionCode = packageLongVersion(info),
            targetSdk = applicationInfo?.targetSdkVersion ?: 0,
            minSdk = if (Build.VERSION.SDK_INT >= 24) applicationInfo?.minSdkVersion else null,
            installerPackage = installer(info.packageName),
            requestedPermissions = info.requestedPermissions?.toList().orEmpty(),
            hasAccessibilityService = hasAccessibilityService(info),
            isDeviceAdmin = hasDeviceAdmin(info),
            apkPath = apkPath,
            apkReadable = readable,
            apkSizeBytes = apkFile?.takeIf { it.exists() }?.length(),
            signingCertSha256 = signingHashes(info),
            firstInstallTime = info.firstInstallTime,
            lastUpdateTime = info.lastUpdateTime,
            isSystemApp = applicationInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM) != 0,
            isSelf = info.packageName == context.packageName,
        )
    }

    private fun hasAccessibilityService(info: PackageInfo): Boolean {
        return info.services.orEmpty().any { service ->
            service.permission == android.Manifest.permission.BIND_ACCESSIBILITY_SERVICE
        }
    }

    private fun hasDeviceAdmin(info: PackageInfo): Boolean {
        return info.receivers.orEmpty().any { receiver ->
            receiver.permission == android.Manifest.permission.BIND_DEVICE_ADMIN
        }
    }

    private fun installer(packageName: String): String? {
        return try {
            if (Build.VERSION.SDK_INT >= 30) {
                packageManager.getInstallSourceInfo(packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstallerPackageName(packageName)
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun signingHashes(info: PackageInfo): List<String> {
        val signatures = if (Build.VERSION.SDK_INT >= 28) {
            info.signingInfo?.apkContentsSigners
        } else {
            @Suppress("DEPRECATION")
            info.signatures
        }
        val digest = MessageDigest.getInstance("SHA-256")
        return signatures.orEmpty().map { signature ->
            digest.reset()
            digest.digest(signature.toByteArray()).joinToString(":") { "%02X".format(it) }
        }
    }

    private fun signingFlags(): Int {
        return if (Build.VERSION.SDK_INT >= 28) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            @Suppress("DEPRECATION")
            PackageManager.GET_SIGNATURES
        }
    }

    private fun packageLongVersion(info: PackageInfo): Long {
        return if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else @Suppress("DEPRECATION") info.versionCode.toLong()
    }
}
