package com.droidsentinel.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.droidsentinel.app.domain.advisor.AdvisorResponse
import com.droidsentinel.app.domain.model.AppScanSummary
import com.droidsentinel.app.domain.model.InstalledAppInfo
import com.droidsentinel.app.domain.model.ScanResult
import com.droidsentinel.app.domain.model.SignatureScanStatus
import com.droidsentinel.app.ui.components.FindingCard
import com.droidsentinel.app.ui.components.SectionTitle
import com.droidsentinel.app.ui.components.SentinelCard
import com.droidsentinel.app.ui.components.StatusChip
import com.droidsentinel.app.ui.components.color
import com.droidsentinel.app.ui.components.label
import com.droidsentinel.app.ui.theme.Cyan
import com.droidsentinel.app.ui.theme.TextMuted
import com.droidsentinel.app.ui.theme.TextPrimary

@Composable
fun AppsScreen(
    apps: List<InstalledAppInfo>,
    lastResult: ScanResult?,
    query: String,
    onQuery: (String) -> Unit,
    onOpen: (String) -> Unit,
) {
    val filtered = apps.filter {
        query.isBlank() || it.label.contains(query, true) || it.packageName.contains(query, true)
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(12.dp))
        Text("Installed apps", color = TextPrimary)
        Text("${apps.size} visible to DroidSentinel", color = TextMuted)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = query,
            onValueChange = onQuery,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search name or package") },
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        if (filtered.isEmpty()) {
            Text("No apps matched. Android may be hiding packages until QUERY_ALL_PACKAGES is granted.", color = TextMuted)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered, key = { it.packageName }) { app ->
                val summary = lastResult?.appSummaries?.firstOrNull { it.app.packageName == app.packageName }
                SentinelCard(Modifier.clickable { onOpen(app.packageName) }) {
                    Text(app.label, color = TextPrimary)
                    Text(app.packageName, color = TextMuted)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val status = summary?.signature?.status ?: SignatureScanStatus.NOT_SCANNED
                        StatusChip(status.label(), status.color())
                        if (summary != null && summary.findings.isNotEmpty()) {
                            StatusChip("${summary.findings.size} findings", Cyan)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun AppDetailScreen(summary: AppScanSummary?, advisor: AdvisorResponse?) {
    if (summary == null) {
        Column(Modifier.padding(20.dp)) {
            Text("Select an app to inspect.", color = TextMuted)
        }
        return
    }
    val app = summary.app
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Spacer(Modifier.height(12.dp))
            Text(app.label, color = TextPrimary)
            Text(app.packageName, color = TextMuted)
        }
        item {
            SentinelCard {
                SectionTitle("Identity")
                Text("Version ${app.versionName ?: "unknown"} (${app.versionCode})", color = TextPrimary)
                Text("Target SDK ${app.targetSdk}", color = TextMuted)
                Text("Installer: ${app.installerPackage ?: "unknown"}", color = TextMuted)
                Text("APK readable: ${app.apkReadable}", color = TextMuted)
                app.signingCertSha256.firstOrNull()?.let { Text("Cert SHA-256: $it", color = TextMuted) }
            }
        }
        item {
            SentinelCard {
                SectionTitle("Signature scan")
                StatusChip(summary.signature.status.label(), summary.signature.status.color())
                Spacer(Modifier.height(8.dp))
                Text(summary.signature.message ?: "No additional scanner message.", color = TextMuted)
                summary.signature.hashes?.sha256?.let { Text("SHA-256: $it", color = TextPrimary) }
            }
        }
        advisor?.let {
            item {
                SentinelCard {
                    SectionTitle("Advisor")
                    Text(it.summary, color = TextPrimary)
                    it.nextSteps.forEach { step -> Text("• $step", color = TextMuted) }
                    it.caveats.forEach { caveat -> Text(caveat, color = TextMuted) }
                }
            }
        }
        item { SectionTitle("Findings") }
        if (summary.findings.isEmpty()) {
            item { Text("No metadata findings. That is not a malware clearance.", color = TextMuted) }
        }
        items(summary.findings, key = { it.id }) { FindingCard(it) }
        item { SectionTitle("Permissions") }
        item {
            SentinelCard {
                if (app.requestedPermissions.isEmpty()) {
                    Text("No permissions were returned for this package.", color = TextMuted)
                } else {
                    app.requestedPermissions.sorted().forEach { Text(it.substringAfterLast('.'), color = TextPrimary) }
                }
            }
        }
        item { Spacer(Modifier.height(32.dp)) }
    }
}
