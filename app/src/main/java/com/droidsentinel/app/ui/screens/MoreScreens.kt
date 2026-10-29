package com.droidsentinel.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.droidsentinel.app.R
import com.droidsentinel.app.ui.theme.LocalReduceMotion
import com.droidsentinel.app.data.history.ScanHistoryEntity
import com.droidsentinel.app.data.history.ScanHistoryMapper
import com.droidsentinel.app.data.hypatia.HypatiaConstants
import com.droidsentinel.app.domain.advisor.AdvisorResponse
import com.droidsentinel.app.domain.model.ScanResult
import com.droidsentinel.app.ui.HomeUiState
import com.droidsentinel.app.ui.components.DatabaseStatusBanner
import com.droidsentinel.app.ui.components.FindingCard
import com.droidsentinel.app.ui.components.SectionTitle
import com.droidsentinel.app.ui.components.SentinelCard
import com.droidsentinel.app.ui.components.StatusChip
import com.droidsentinel.app.ui.components.color
import com.droidsentinel.app.ui.components.label
import com.droidsentinel.app.ui.theme.Cyan
import com.droidsentinel.app.ui.theme.Navy
import com.droidsentinel.app.ui.theme.TextMuted
import com.droidsentinel.app.ui.theme.TextPrimary
import java.text.DateFormat
import java.util.Date

@Composable
fun ResultsScreen(result: ScanResult?, advisor: AdvisorResponse?) {
    if (result == null) {
        Column(Modifier.padding(20.dp)) {
            Text("No scan report yet.", color = TextMuted)
            Text("Run an installed-app scan or pick an APK to generate evidence-backed results.", color = TextMuted)
        }
        return
    }
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Spacer(Modifier.height(12.dp))
            Text(
                result.targetName,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            StatusChip(result.signatureStatus.label(), result.signatureStatus.color())
        }
        item {
            SentinelCard {
                SectionTitle("Scan completeness")
                Text("Signature scanned: ${result.appsSignatureScanned}", color = TextPrimary)
                Text("Inspected: ${result.appsInspected}  Skipped: ${result.appsSkipped}  Failed: ${result.appsFailed}", color = TextMuted)
                Text("Duration: ${result.durationMs} ms", color = TextMuted)
                result.databaseLabel?.let { Text("Database: $it", color = TextMuted) }
                Text("Verified database: ${result.databaseVerified}", color = TextMuted)
            }
        }
        advisor?.let {
            item {
                SentinelCard {
                    SectionTitle("Advisor")
                    Text(it.summary, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    it.nextSteps.forEach { step -> Text("• $step", color = TextMuted) }
                }
            }
        }
        if (result.limitations.isNotEmpty()) {
            item {
                SentinelCard {
                    SectionTitle("Limitations")
                    result.limitations.forEach { Text("• $it", color = TextMuted) }
                }
            }
        }
        result.hashes?.let { hashes ->
            item {
                SentinelCard {
                    SectionTitle("Hashes")
                    Text("MD5 ${hashes.md5}", color = TextPrimary)
                    Text("SHA-1 ${hashes.sha1}", color = TextPrimary)
                    Text("SHA-256 ${hashes.sha256}", color = TextPrimary)
                }
            }
        }
        item { SectionTitle("Findings") }
        if (result.findings.isEmpty()) {
            item { Text("No findings were recorded. This is not a guarantee of safety.", color = TextMuted) }
        }
        items(result.findings, key = { it.id }) { FindingCard(it) }
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
fun HistoryScreen(items: List<ScanHistoryEntity>) {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(12.dp))
        Text("Scan history", color = TextPrimary)
        Text("Stored locally on this device.", color = TextMuted)
        Spacer(Modifier.height(12.dp))
        if (items.isEmpty()) {
            val reduceMotion = LocalReduceMotion.current
            val composition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(R.raw.shield_pulse)
            )
            val progress by animateLottieCompositionAsState(
                composition = composition,
                iterations = if (reduceMotion) 1 else LottieConstants.IterateForever,
            )
            SentinelCard {
                androidx.compose.foundation.layout.Box(
                    Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    LottieAnimation(composition, { progress }, Modifier.height(120.dp))
                }
                Text("No scans yet", color = TextPrimary)
                Text("Completed scans will appear here with their real status, not placeholder results.", color = TextMuted)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(items, key = { it.id }) { item ->
                    val status = ScanHistoryMapper.status(item)
                    SentinelCard {
                        Text(
                            item.targetName,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(DateFormat.getDateTimeInstance().format(Date(item.startedAt)), color = TextMuted)
                        Spacer(Modifier.height(8.dp))
                        StatusChip(status.label(), status.color())
                        Text("Findings ${item.metadataFindingCount} • signature alerts ${item.threatCount}", color = TextMuted)
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun SettingsScreen(state: HomeUiState, onUpdateDatabase: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Settings", color = TextPrimary)
        SentinelCard {
            SectionTitle("Hypatia signature database")
            DatabaseStatusBanner(
                availability = state.availability,
                database = state.database,
            )
            Spacer(Modifier.height(12.dp))
            Text(state.database.sourceUrl, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            state.availability.signatureCount?.let { Text("Approximate signatures: $it", color = TextMuted) }
            state.database.lastError?.let { Text(it, color = com.droidsentinel.app.ui.theme.Danger) }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onUpdateDatabase,
                enabled = !state.database.updating,
                colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Navy),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.database.updating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Navy,
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.size(10.dp))
                    Text("Updating…")
                } else {
                    Text("Download / update databases")
                }
            }
            Text("Downloads are GPG-verified. Unverified files are discarded and never treated as a clean scan.", color = TextMuted)
        }
        SentinelCard {
            SectionTitle("AI advisor")
            Text(
                "Core scanning never needs an API key. Explanations currently use an on-device evidence advisor so results cannot be invented by a remote model.",
                color = TextMuted,
            )
        }
        SentinelCard {
            SectionTitle("Privacy")
            Text("Installed-app inventory, APK bytes, and hashes stay on device.", color = TextMuted)
            Text("Network is used only for HTTPS signature-database updates you start yourself.", color = TextMuted)
            Text("No analytics, crash telemetry, or advertising SDKs are included.", color = TextMuted)
        }
        SentinelCard {
            SectionTitle("About")
            Text("DroidSentinel 0.1.0", color = TextPrimary)
            Text("Hypatia-compatible BloomFilter scanner", color = TextMuted)
            Text("Upstream ${HypatiaConstants.UPSTREAM_URL}", color = TextMuted)
            Text("Revision ${HypatiaConstants.UPSTREAM_COMMIT}", color = TextMuted)
            Text("Licensed under AGPL-3.0-or-later because Hypatia is AGPL.", color = TextMuted)
        }
        SentinelCard {
            SectionTitle("Limitations")
            Text("BloomFilter hits are possible matches, not malware family names.", color = TextMuted)
            Text("Permissions never prove malware by themselves.", color = TextMuted)
            Text("Android may hide packages from ordinary apps.", color = TextMuted)
            Text("This is not Play Protect, not real-time protection, and not a guarantee.", color = TextMuted)
        }
    }
}
