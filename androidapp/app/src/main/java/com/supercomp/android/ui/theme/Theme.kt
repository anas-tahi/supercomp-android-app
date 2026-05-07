package com.supercomp.android.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Brand Colors ──────────────────────────────────────────────────────────────
val SuperGreen       = Color(0xFF00E676)   // vibrant green accent
val SuperGreenDark   = Color(0xFF00C853)
val SuperNavy        = Color(0xFF0A0E1A)   // deep navy background
val SuperSurface     = Color(0xFF121828)   // card surface
val SuperSurface2    = Color(0xFF1C2436)   // elevated card
val SuperBorder      = Color(0xFF2A3447)   // subtle border
val SuperTextPrimary = Color(0xFFF0F4FF)   // near white
val SuperTextSecond  = Color(0xFF8A99B8)   // muted blue-grey
val SuperRed         = Color(0xFFFF5252)
val SuperOrange      = Color(0xFFFF6D00)
val SuperYellow      = Color(0xFFFFD740)

// Supermarket brand colors
val MercadonaGreen   = Color(0xFF00A650)
val LidlBlue         = Color(0xFF0050AA)
val DiaRed           = Color(0xFFDD1122)
val CarrefourBlue    = Color(0xFF004A99)
val AlcampoOrange    = Color(0xFFFF6600)

private val DarkColorScheme = darkColorScheme(
    primary          = SuperGreen,
    onPrimary        = Color(0xFF003314),
    primaryContainer = Color(0xFF00452A),
    onPrimaryContainer = Color(0xFF9BFFC4),

    secondary        = Color(0xFF4FC3F7),
    onSecondary      = Color(0xFF003352),
    secondaryContainer = Color(0xFF004A75),
    onSecondaryContainer = Color(0xFFCBE6FF),

    tertiary         = SuperOrange,
    onTertiary       = Color(0xFF2A1600),

    background       = SuperNavy,
    onBackground     = SuperTextPrimary,

    surface          = SuperSurface,
    onSurface        = SuperTextPrimary,
    surfaceVariant   = SuperSurface2,
    onSurfaceVariant = SuperTextSecond,

    outline          = SuperBorder,
    outlineVariant   = Color(0xFF1E2A3D),

    error            = SuperRed,
    onError          = Color.White,
)

@Composable
fun SuperCompTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = Typography(),
        content     = content
    )
}