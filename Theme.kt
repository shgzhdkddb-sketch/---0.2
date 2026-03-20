package com.art.yaroslavl.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ════════════════════════════════════════════════
//  АРТ — Оранжевая палитра + Ярославль
// ════════════════════════════════════════════════

// Оранжевый — фирменный цвет АРТ
val ArtOrange       = Color(0xFFFF6B1A)
val ArtOrangeLight  = Color(0xFFFF8C4A)
val ArtOrangeDark   = Color(0xFFCC4E00)
val ArtAmber        = Color(0xFFFFB347)

// Ярославские акценты
val YarGold         = Color(0xFFD4A017)   // золото куполов
val YarGreen        = Color(0xFF2D6A4F)   // зелень Поволжья
val YarRiver        = Color(0xFF1565C0)   // синева Волги

// Служебные
val DangerRed       = Color(0xFFC62828)
val WarningAmber    = Color(0xFFEF6C00)
val SuccessGreen    = Color(0xFF2E7D32)

// ── Светлая тема ─────────────────────────────────
private val LightColors = lightColorScheme(
    primary             = ArtOrange,
    onPrimary           = Color.White,
    primaryContainer    = Color(0xFFFFE5D0),
    onPrimaryContainer  = Color(0xFF3A1500),
    secondary           = YarGreen,
    onSecondary         = Color.White,
    secondaryContainer  = Color(0xFFD2EBE0),
    onSecondaryContainer = Color(0xFF0C2D1E),
    tertiary            = YarRiver,
    onTertiary          = Color.White,
    background          = Color(0xFFFFF8F4),   // тёплый белый
    surface             = Color(0xFFFFFFFF),
    surfaceVariant      = Color(0xFFFFF0E6),   // светло-оранжевый
    onSurface           = Color(0xFF1C1208),
    onSurfaceVariant    = Color(0xFF7A5C40),
    outline             = Color(0xFFE8C8A8),
    outlineVariant      = Color(0xFFF5E0CC),
    error               = DangerRed,
    onError             = Color.White,
)

// ── Тёмная тема ──────────────────────────────────
private val DarkColors = darkColorScheme(
    primary             = ArtOrangeLight,
    onPrimary           = Color(0xFF4A2000),
    primaryContainer    = ArtOrangeDark,
    onPrimaryContainer  = Color(0xFFFFE5D0),
    secondary           = Color(0xFF52B788),
    onSecondary         = Color(0xFF0A2318),
    secondaryContainer  = YarGreen,
    onSecondaryContainer = Color(0xFFD2EBE0),
    tertiary            = Color(0xFF90CAF9),
    onTertiary          = Color(0xFF001F40),
    background          = Color(0xFF18100A),   // очень тёмный тёплый
    surface             = Color(0xFF251810),
    surfaceVariant      = Color(0xFF362418),
    onSurface           = Color(0xFFF0E0CC),
    onSurfaceVariant    = Color(0xFFCDA882),
    outline             = Color(0xFF5C3820),
    outlineVariant      = Color(0xFF3A2010),
    error               = Color(0xFFFF6B6B),
    onError             = Color(0xFF600000),
)

@Composable
fun ArtTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = ArtTypography,
        content = content
    )
}

val ArtTypography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, letterSpacing = (-1).sp),
    displayMedium = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 26.sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 20.sp),
    titleMedium   = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 16.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 13.sp, lineHeight = 19.sp),
    labelSmall    = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 11.sp, letterSpacing = 0.4.sp),
)
