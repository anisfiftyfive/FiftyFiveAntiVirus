package com.droidsentinel.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Navy = Color(0xFF070B1A)
val NavyElevated = Color(0xFF10182E)
val SurfaceCard = Color(0xFF161F38)
val Cyan = Color(0xFF2EE6D6)
val CyanDim = Color(0xFF1AAFA3)
val Indigo = Color(0xFF6C7BFF)
val Amber = Color(0xFFF5B942)
val Danger = Color(0xFFFF4D6D)
val TextPrimary = Color(0xFFF4F7FF)
val TextMuted = Color(0xFF9AA6C8)
val OutlineSoft = Color(0xFF2A365C)

val SentinelColors = darkColorScheme(
    primary = Cyan,
    onPrimary = Navy,
    secondary = Indigo,
    onSecondary = TextPrimary,
    background = Navy,
    onBackground = TextPrimary,
    surface = NavyElevated,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextMuted,
    error = Danger,
    onError = TextPrimary,
    outline = OutlineSoft,
    tertiary = Amber,
)

val SentinelTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        color = TextPrimary,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        color = TextPrimary,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        color = TextPrimary,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        color = TextPrimary,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = TextPrimary,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = TextMuted,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = TextPrimary,
    ),
)

object Motion {
    val ease = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
    fun micro(duration: Int = 160) = tween<Float>(duration, easing = ease)
    fun component(duration: Int = 280) = tween<Float>(duration, easing = ease)
    fun screen(duration: Int = 380) = tween<Float>(duration, easing = ease)
    const val AmbientMillis = 2400
}

val CardShape = RoundedCornerShape(24.dp)
val PillShape = RoundedCornerShape(100.dp)

val LocalReduceMotion = staticCompositionLocalOf { false }

@Composable
fun DroidSentinelTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SentinelColors,
        typography = SentinelTypography,
        content = content,
    )
}
