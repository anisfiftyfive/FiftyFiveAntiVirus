package com.droidsentinel.app.ui.components

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.droidsentinel.app.domain.model.SignatureScanStatus
import com.droidsentinel.app.ui.theme.Amber
import com.droidsentinel.app.ui.theme.Cyan
import com.droidsentinel.app.ui.theme.Danger
import com.droidsentinel.app.ui.theme.Indigo
import com.droidsentinel.app.ui.theme.LocalReduceMotion
import com.droidsentinel.app.ui.theme.NavyElevated
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

enum class HeroVisualState { Idle, Scanning, NoMatch, Threat, Incomplete, Failed, DatabaseUnavailable }

fun heroVisualState(status: SignatureScanStatus?, scanning: Boolean): HeroVisualState {
    if (scanning) return HeroVisualState.Scanning
    return when (status) {
        null -> HeroVisualState.Idle
        SignatureScanStatus.THREAT_DETECTED -> HeroVisualState.Threat
        SignatureScanStatus.NO_MATCH_FOUND -> HeroVisualState.NoMatch
        SignatureScanStatus.DATABASE_UNAVAILABLE -> HeroVisualState.DatabaseUnavailable
        SignatureScanStatus.SCAN_FAILED -> HeroVisualState.Failed
        SignatureScanStatus.SCAN_INCOMPLETE, SignatureScanStatus.NOT_SCANNED -> HeroVisualState.Incomplete
    }
}

private val HexagonClip = GenericShape { size, _ ->
    addPath(hexagonPath(Offset(size.width / 2f, size.height / 2f), min(size.width, size.height) / 2f))
}

@Composable
fun ShieldHero(
    state: HeroVisualState,
    modifier: Modifier = Modifier,
    contentDescription: String,
    scannedPackageName: String? = null,
) {
    val context = LocalContext.current
    val reduceMotion = LocalReduceMotion.current
    val iconPackage = scannedPackageName ?: if (state == HeroVisualState.Scanning) null else context.packageName
    val icon = rememberAppIcon(iconPackage)
    val infinite = rememberInfiniteTransition(label = "shield")
    val pulse by infinite.animateFloat(
        0.96f,
        1.04f,
        infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse",
    )
    val sweep by infinite.animateFloat(
        0f,
        360f,
        infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "sweep",
    )
    val accent = when (state) {
        HeroVisualState.Threat -> Danger
        HeroVisualState.Failed, HeroVisualState.Incomplete, HeroVisualState.DatabaseUnavailable -> Amber
        else -> Cyan
    }
    val scale = if (reduceMotion) 1f else if (state == HeroVisualState.Idle || state == HeroVisualState.NoMatch) pulse else 1f
    Box(
        modifier
            .size(220.dp)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(220.dp)) {
            val radius = size.minDimension / 2.2f * scale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.28f), Color.Transparent),
                ),
                radius = radius * 1.45f,
            )
            drawCircle(
                color = accent.copy(alpha = 0.18f),
                radius = radius * 1.12f,
                style = Stroke(width = 3f),
            )
            if (state == HeroVisualState.Scanning && !reduceMotion) {
                rotate(sweep) {
                    drawArc(
                        color = accent,
                        startAngle = -18f,
                        sweepAngle = 70f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 10f, cap = StrokeCap.Round),
                    )
                }
            }
            val hex = hexagonPath(center, radius * 0.78f)
            drawPath(
                path = hex,
                brush = Brush.verticalGradient(listOf(accent.copy(alpha = 0.95f), Indigo.copy(alpha = 0.9f))),
                style = Stroke(width = 7f, cap = StrokeCap.Round),
            )
        }
        Box(
            Modifier
                .size(112.dp)
                .clip(HexagonClip)
                .background(NavyElevated),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = iconPackage to icon,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(180)) },
                label = "scanned-icon",
            ) { (_, bitmap) ->
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = null,
                        modifier = Modifier.size(112.dp),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Android,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(64.dp),
                    )
                }
            }
        }
        if (state == HeroVisualState.NoMatch || state == HeroVisualState.Threat) {
            Canvas(Modifier.size(220.dp)) {
                val radius = size.minDimension / 2.2f
                if (state == HeroVisualState.NoMatch) {
                    drawLine(
                        color = Cyan,
                        start = Offset(center.x - radius * 0.08f, center.y + radius * 0.42f),
                        end = Offset(center.x + radius * 0.02f, center.y + radius * 0.52f),
                        strokeWidth = 7f,
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = Cyan,
                        start = Offset(center.x + radius * 0.02f, center.y + radius * 0.52f),
                        end = Offset(center.x + radius * 0.18f, center.y + radius * 0.34f),
                        strokeWidth = 7f,
                        cap = StrokeCap.Round,
                    )
                } else {
                    drawLine(
                        color = Danger,
                        start = Offset(center.x, center.y + radius * 0.32f),
                        end = Offset(center.x, center.y + radius * 0.48f),
                        strokeWidth = 7f,
                        cap = StrokeCap.Round,
                    )
                    drawCircle(
                        color = Danger,
                        radius = 5f,
                        center = Offset(center.x, center.y + radius * 0.56f),
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberAppIcon(packageName: String?): ImageBitmap? {
    val context = LocalContext.current
    return remember(packageName) {
        if (packageName.isNullOrBlank()) return@remember null
        runCatching {
            val drawable = context.packageManager.getApplicationIcon(packageName)
            drawable.toSquareBitmap().asImageBitmap()
        }.getOrNull()
    }
}

private fun Drawable.toSquareBitmap(): android.graphics.Bitmap {
    val size = 192
    return if (this is BitmapDrawable && bitmap != null) {
        bitmap
    } else {
        toBitmap(size, size)
    }
}

private fun hexagonPath(center: Offset, radius: Float): Path {
    return Path().apply {
        for (i in 0..5) {
            val angle = Math.toRadians((-90.0 + i * 60.0))
            val x = center.x + radius * cos(angle).toFloat()
            val y = center.y + radius * sin(angle).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
}
