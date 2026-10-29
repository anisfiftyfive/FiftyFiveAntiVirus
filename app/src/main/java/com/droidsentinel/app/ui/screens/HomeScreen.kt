package com.droidsentinel.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.droidsentinel.app.domain.model.ScanStage
import com.droidsentinel.app.ui.HomeUiState
import com.droidsentinel.app.ui.components.HeroVisualState
import com.droidsentinel.app.ui.components.SectionTitle
import com.droidsentinel.app.ui.components.SentinelCard
import com.droidsentinel.app.ui.components.ShieldHero
import com.droidsentinel.app.ui.components.StatTile
import com.droidsentinel.app.ui.components.DatabaseStatusBanner
import com.droidsentinel.app.ui.components.StatusChip
import com.droidsentinel.app.ui.components.color
import com.droidsentinel.app.ui.components.heroVisualState
import com.droidsentinel.app.ui.components.label
import com.droidsentinel.app.ui.theme.Cyan
import com.droidsentinel.app.ui.theme.Navy
import com.droidsentinel.app.ui.theme.TextMuted
import com.droidsentinel.app.ui.theme.TextPrimary
import java.text.DateFormat
import java.util.Date

@Composable
fun HomeScreen(
    state: HomeUiState,
    onScanApps: () -> Unit,
    onScanApk: () -> Unit,
    onCancel: () -> Unit,
    onOpenResults: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val hero = heroVisualState(state.lastResult?.signatureStatus, state.scanning)
    Column(
        Modifier
            .fillMaxSize()
            .testTag("home_screen")
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("DroidSentinel", color = TextPrimary)
        Text("Know what is happening on your Android device.", color = TextMuted, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        ShieldHero(
            state = hero,
            scannedPackageName = if (state.scanning) state.progress?.packageName else null,
            contentDescription = when (hero) {
                HeroVisualState.Scanning -> "Scan in progress"
                HeroVisualState.Threat -> "Possible signature match found"
                HeroVisualState.NoMatch -> "No signature match in the last completed scan"
                HeroVisualState.DatabaseUnavailable -> "Signature database unavailable"
                HeroVisualState.Failed -> "Last scan failed"
                HeroVisualState.Incomplete -> "Last scan incomplete"
                HeroVisualState.Idle -> "Ready to scan"
            },
        )
        AnimatedContent(targetState = hero, label = "hero-label") { visual ->
            Text(
                text = when (visual) {
                    HeroVisualState.Scanning -> state.progress?.label ?: "Scanning"
                    HeroVisualState.NoMatch -> "No signature match found"
                    HeroVisualState.Threat -> "Possible signature match"
                    HeroVisualState.DatabaseUnavailable -> "Signature database unavailable"
                    HeroVisualState.Failed -> "Scan failed"
                    HeroVisualState.Incomplete -> "Scan incomplete"
                    HeroVisualState.Idle -> "Ready when you are"
                },
                color = TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 24.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        DatabaseStatusBanner(
            availability = state.availability,
            database = state.database,
            onClick = onOpenSettings,
        )
        AnimatedVisibility(state.scanning) {
            Column(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                val progress = state.progress
                if (progress != null && progress.total > 0) {
                    LinearProgressIndicator(
                        progress = { progress.current.toFloat() / progress.total.toFloat() },
                        modifier = Modifier.fillMaxWidth(),
                        color = Cyan,
                        trackColor = TextMuted.copy(alpha = 0.2f),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${progress.current} / ${progress.total} • ${stageLabel(progress.stage)}",
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                } else {
                    LinearProgressIndicator(Modifier.fillMaxWidth(), color = Cyan, trackColor = TextMuted.copy(alpha = 0.2f))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        progress?.label ?: "Working",
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = if (state.scanning) onCancel else onScanApps,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Navy),
            enabled = true,
        ) {
            Text(if (state.scanning) "Cancel scan" else "Scan installed apps")
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onScanApk, modifier = Modifier.fillMaxWidth().height(54.dp), enabled = !state.scanning) {
            Text("Scan an APK file")
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("Last scan", state.lastResult?.completedAt?.let { DateFormat.getTimeInstance().format(Date(it)) } ?: "Never")
            StatTile("Apps seen", (state.lastResult?.appsInspected ?: 0).toString())
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("Signature scanned", (state.lastResult?.appsSignatureScanned ?: 0).toString())
            StatTile("Metadata flags", (state.lastResult?.metadataFindingCount ?: 0).toString())
        }
        Spacer(Modifier.height(16.dp))
        SentinelCard {
            SectionTitle("Privacy")
            Text(
                "Scans stay on this device. Files are not uploaded. Internet is used only when you update Hypatia signature databases.",
                color = TextMuted,
            )
            TextButton(onClick = onOpenSettings) { Text("Database and privacy settings") }
        }
        state.lastResult?.let { result ->
            Spacer(Modifier.height(16.dp))
            SentinelCard {
                SectionTitle("Latest result")
                StatusChip(result.signatureStatus.label(), result.signatureStatus.color())
                Spacer(Modifier.height(8.dp))
                Text(state.advisor?.summary ?: "Open the full report for evidence and recommended actions.", color = TextMuted)
                TextButton(onClick = onOpenResults) { Text("Open full report") }
            }
        }
        state.error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = com.droidsentinel.app.ui.theme.Danger)
        }
        Spacer(Modifier.height(24.dp))
        Text("DroidSentinel does not claim that a device is 100% safe.", color = TextMuted, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
    }
}

private fun stageLabel(stage: ScanStage): String = when (stage) {
    ScanStage.PREPARING -> "Preparing"
    ScanStage.LOADING_DATABASE -> "Loading database"
    ScanStage.READING_METADATA -> "Reading metadata"
    ScanStage.HASHING -> "Hashing"
    ScanStage.CHECKING_SIGNATURES -> "Checking signatures"
    ScanStage.ANALYZING_METADATA -> "Analyzing permissions"
    ScanStage.COMPLETING -> "Completing"
    ScanStage.CANCELLED -> "Cancelled"
}
