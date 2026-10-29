package com.droidsentinel.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.droidsentinel.app.domain.model.DatabaseStatus
import com.droidsentinel.app.domain.model.ScannerAvailability
import com.droidsentinel.app.ui.theme.LocalReduceMotion
import com.droidsentinel.app.domain.model.ScanFinding
import com.droidsentinel.app.domain.model.Severity
import com.droidsentinel.app.domain.model.SignatureScanStatus
import com.droidsentinel.app.ui.theme.Amber
import com.droidsentinel.app.ui.theme.CardShape
import com.droidsentinel.app.ui.theme.Cyan
import com.droidsentinel.app.ui.theme.Danger
import com.droidsentinel.app.ui.theme.Indigo
import com.droidsentinel.app.ui.theme.PillShape
import com.droidsentinel.app.ui.theme.SurfaceCard
import com.droidsentinel.app.ui.theme.TextMuted
import com.droidsentinel.app.ui.theme.TextPrimary

fun SignatureScanStatus.label(): String = when (this) {
    SignatureScanStatus.THREAT_DETECTED -> "Possible signature match"
    SignatureScanStatus.NO_MATCH_FOUND -> "No signature match"
    SignatureScanStatus.NOT_SCANNED -> "Not scanned"
    SignatureScanStatus.SCAN_FAILED -> "Scan failed"
    SignatureScanStatus.DATABASE_UNAVAILABLE -> "Database unavailable"
    SignatureScanStatus.SCAN_INCOMPLETE -> "Scan incomplete"
}

fun SignatureScanStatus.color(): Color = when (this) {
    SignatureScanStatus.THREAT_DETECTED -> Danger
    SignatureScanStatus.NO_MATCH_FOUND -> Cyan
    SignatureScanStatus.NOT_SCANNED, SignatureScanStatus.DATABASE_UNAVAILABLE,
    SignatureScanStatus.SCAN_FAILED, SignatureScanStatus.SCAN_INCOMPLETE,
    -> Amber
}

fun Severity.color(): Color = when (this) {
    Severity.INFO -> Indigo
    Severity.LOW -> Cyan
    Severity.MEDIUM -> Amber
    Severity.HIGH, Severity.CRITICAL -> Danger
}

@Composable
fun StatusChip(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = color,
        modifier = modifier
            .background(color.copy(alpha = 0.14f), PillShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Composable
fun DatabaseStatusBanner(
    availability: ScannerAvailability,
    database: DatabaseStatus,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val ready = availability.databaseVerified
    val updating = database.updating
    val accent = when {
        ready -> Cyan
        updating -> Indigo
        else -> Amber
    }
    val label = when {
        updating -> "Downloading Hypatia database"
        ready -> "Hypatia database ready"
        else -> "Hypatia database not ready"
    }
    val reduceMotion = LocalReduceMotion.current
    val pulse by rememberInfiniteTransition(label = "db-pulse").animateFloat(
        0.45f,
        1f,
        infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Reverse),
        label = "db-pulse-alpha",
    )
    Row(
        modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(accent.copy(alpha = 0.12f), PillShape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) {
            when {
                updating -> CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = accent,
                    strokeWidth = 2.dp,
                    trackColor = accent.copy(alpha = 0.2f),
                )
                ready -> Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(22.dp),
                )
                else -> Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier
                        .size(22.dp)
                        .alpha(if (reduceMotion) 1f else pulse),
                )
            }
        }
        Text(
            text = label,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (!ready && !updating) {
            Icon(
                imageVector = Icons.Outlined.CloudDownload,
                contentDescription = "Download Hypatia database",
                tint = accent,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
fun SentinelCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = CardShape,
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(Modifier.padding(20.dp)) { content() }
    }
}

@Composable
fun StatTile(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(SurfaceCard, CardShape)
            .padding(16.dp),
    ) {
        Text(title, color = TextMuted)
        Spacer(Modifier.height(6.dp))
        Text(value, color = TextPrimary)
    }
}

@Composable
fun RowScope.StatTile(title: String, value: String) {
    StatTile(title, value, Modifier.weight(1f))
}

@Composable
fun FindingCard(finding: ScanFinding, modifier: Modifier = Modifier) {
    SentinelCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusChip(finding.severity.name, finding.severity.color())
            Spacer(Modifier.width(8.dp))
            StatusChip(finding.source.name.lowercase(), Indigo)
        }
        Spacer(Modifier.height(12.dp))
        Text(finding.title, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text(finding.explanation, color = TextMuted)
        Spacer(Modifier.height(8.dp))
        finding.evidence.take(4).forEach {
            Text("• $it", color = TextPrimary)
        }
        Spacer(Modifier.height(8.dp))
        Text(finding.recommendedAction, color = Cyan)
        finding.confidence?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = TextMuted)
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, color = TextPrimary, modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
fun TwoPaneStats(leftTitle: String, left: String, rightTitle: String, right: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatTile(leftTitle, left)
        StatTile(rightTitle, right)
    }
}
