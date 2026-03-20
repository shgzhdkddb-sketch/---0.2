package com.art.yaroslavl.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.art.yaroslavl.data.model.*
import com.art.yaroslavl.ui.theme.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// ── Аватар ──────────────────────────────────────
@Composable
fun ArtAvatar(user: User?, name: String, size: Dp = 48.dp, showOnline: Boolean = true) {
    Box(contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(nameColor(name))
                .then(
                    if (user?.role == UserRole.OFFICIAL || user?.role == UserRole.EMERGENCY)
                        Modifier.border(2.dp, ArtOrange, CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            val initials = name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
            Text(initials, color = Color.White, fontSize = (size.value * 0.33f).sp, fontWeight = FontWeight.Bold)
        }
        if (showOnline && user?.isOnline == true) {
            Box(
                Modifier.size(size * 0.27f).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background).padding(1.5.dp)
                    .clip(CircleShape).background(SuccessGreen)
            )
        }
    }
}

fun nameColor(name: String): Color {
    val palette = listOf(ArtOrangeDark, YarGreen, YarRiver, Color(0xFF6A1B9A), Color(0xFF00838F), Color(0xFF558B2F), Color(0xFF5D4037))
    return palette[name.hashCode().let { if (it < 0) -it else it } % palette.size]
}

// ── Бейдж роли ───────────────────────────────────
@Composable
fun RoleBadge(role: UserRole) {
    val (color, bg) = when (role) {
        UserRole.OFFICIAL  -> ArtOrange to ArtOrange.copy(.12f)
        UserRole.EMERGENCY -> DangerRed to DangerRed.copy(.12f)
        UserRole.VOLUNTEER -> YarGreen to YarGreen.copy(.12f)
        UserRole.BUSINESS  -> YarRiver to YarRiver.copy(.12f)
        else               -> return
    }
    Text(
        role.label,
        Modifier.clip(RoundedCornerShape(4.dp)).background(bg).padding(horizontal = 5.dp, vertical = 1.dp),
        color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold
    )
}

// ── Статус сообщения ─────────────────────────────
@Composable
fun StatusIcon(status: MessageStatus) {
    val (txt, col) = when (status) {
        MessageStatus.SENDING   -> "⏳" to Color.Gray
        MessageStatus.SENT      -> "✓"  to Color.Gray
        MessageStatus.DELIVERED -> "✓✓" to Color.Gray
        MessageStatus.READ      -> "✓✓" to YarRiver
    }
    Text(txt, color = col, fontSize = 11.sp)
}

// ── Баннер оповещения ────────────────────────────
@Composable
fun AlertBanner(alert: CityAlert, modifier: Modifier = Modifier) {
    val (bg, border) = when (alert.severity) {
        AlertSeverity.CRITICAL -> DangerRed.copy(.12f) to DangerRed
        AlertSeverity.WARNING  -> WarningAmber.copy(.12f) to WarningAmber
        AlertSeverity.INFO     -> YarRiver.copy(.10f) to YarRiver
    }
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, border.copy(.4f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(alert.type.emoji, fontSize = 26.sp)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(alert.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = border)
            Text(alert.description, fontSize = 12.sp, maxLines = 2)
            Text("${alert.district.emoji} ${alert.district.displayName} • ${alert.sourceName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── Форматирование времени ────────────────────────
fun fmtTime(t: LocalDateTime): String {
    val now = LocalDateTime.now()
    return when {
        ChronoUnit.MINUTES.between(t, now) < 1  -> "сейчас"
        ChronoUnit.HOURS.between(t, now) < 24   -> t.format(DateTimeFormatter.ofPattern("HH:mm"))
        ChronoUnit.DAYS.between(t, now) < 7     -> t.format(DateTimeFormatter.ofPattern("EEE"))
        else                                     -> t.format(DateTimeFormatter.ofPattern("dd.MM"))
    }
}

fun fmtLastSeen(t: LocalDateTime?): String {
    if (t == null) return "давно не был(а)"
    val now = LocalDateTime.now()
    return when {
        ChronoUnit.MINUTES.between(t, now) < 5  -> "только что"
        ChronoUnit.MINUTES.between(t, now) < 60 -> "был(а) ${ChronoUnit.MINUTES.between(t, now)} мин. назад"
        ChronoUnit.HOURS.between(t, now) < 24   -> "был(а) ${ChronoUnit.HOURS.between(t, now)} ч. назад"
        else                                     -> "был(а) ${t.format(DateTimeFormatter.ofPattern("d MMM"))}"
    }
}

fun fmtEventDate(t: LocalDateTime): String =
    t.format(DateTimeFormatter.ofPattern("d MMMM, EEEE, HH:mm"))

fun fmtShortDate(t: LocalDateTime): String =
    t.format(DateTimeFormatter.ofPattern("d MMM в HH:mm"))

fun fmtCount(n: Int): String = when {
    n >= 1000 -> "${n / 1000}.${(n % 1000) / 100}к"
    else -> "$n"
}
