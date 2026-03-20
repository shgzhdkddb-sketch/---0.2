package com.art.yaroslavl.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.art.yaroslavl.data.model.*
import com.art.yaroslavl.ui.components.*
import com.art.yaroslavl.ui.theme.*
import com.art.yaroslavl.viewmodel.CityFeedViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityFeedScreen(
    onEventClick: (String) -> Unit,
    vm: CityFeedViewModel = viewModel()
) {
    val alerts by vm.alerts.collectAsState()
    val events by vm.events.collectAsState()
    val selectedDistrict by vm.selectedDistrict.collectAsState()
    val selectedCategory by vm.selectedCategory.collectAsState()
    val attending by vm.attending.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🐻", fontSize = 22.sp)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "АРТ",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Ярославль — на связи",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton({}) {
                        Icon(Icons.Default.Add, "Добавить событие", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {

            // ── Оповещения МЧС ───────────────────────────
            if (alerts.isNotEmpty()) {
                item {
                    Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(8.dp).clip(CircleShape).background(DangerRed)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "ЭКСТРЕННЫЕ ОПОВЕЩЕНИЯ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DangerRed,
                                letterSpacing = 1.sp
                            )
                        }
                        alerts.forEach { AlertBanner(it) }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }

            // ── Фильтр по районам ─────────────────────────
            item {
                Column(Modifier.padding(top = 12.dp)) {
                    Text(
                        "РАЙОНЫ",
                        Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedDistrict == null,
                            onClick = { vm.setDistrict(null) },
                            label = { Text("🐻 Весь город", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                        District.values().filter { it != District.ALL_CITY }.forEach { d ->
                            FilterChip(
                                selected = selectedDistrict == d,
                                onClick = { vm.setDistrict(if (selectedDistrict == d) null else d) },
                                label = { Text("${d.emoji} ${d.displayName}", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
            }

            // ── Фильтр по категориям ──────────────────────
            item {
                Column(Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                    Text(
                        "КАТЕГОРИИ",
                        Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { vm.setCategory(null) },
                            label = { Text("Все", fontSize = 12.sp) }
                        )
                        EventCategory.values().forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { vm.setCategory(if (selectedCategory == cat) null else cat) },
                                label = { Text("${cat.emoji} ${cat.displayName}", fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

            // ── Заголовок ─────────────────────────────────
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "БЛИЖАЙШИЕ СОБЫТИЯ",
                        fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "${events.size} событий",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ── Список событий ────────────────────────────
            if (events.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🗓️", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text("Событий не найдено", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Попробуй другой район или категорию", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(events, key = { it.id }) { event ->
                    EventCard(
                        event = event,
                        isAttending = event.id in attending,
                        onAttend = { vm.toggleAttend(event.id) },
                        onClick = { onEventClick(event.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun EventCard(
    event: CityEvent,
    isAttending: Boolean,
    onAttend: () -> Unit,
    onClick: () -> Unit
) {
    val borderColor = when {
        event.isEmergency -> DangerRed
        event.isOfficial  -> ArtOrange
        else              -> Color.Transparent
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 5.dp)
            .then(if (borderColor != Color.Transparent) Modifier.border(1.5.dp, borderColor.copy(.5f), RoundedCornerShape(16.dp)) else Modifier),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Цветная полоска сверху по категории
            Box(
                Modifier.fillMaxWidth().height(4.dp).background(
                    when (event.category) {
                        EventCategory.CLEANUP   -> YarGreen
                        EventCategory.CULTURE   -> Color(0xFF9C27B0)
                        EventCategory.SPORT     -> Color(0xFF1976D2)
                        EventCategory.MARKET    -> ArtOrange
                        EventCategory.FESTIVAL  -> Color(0xFFE91E63)
                        EventCategory.TRANSPORT -> Color(0xFF607D8B)
                        EventCategory.EMERGENCY -> DangerRed
                        EventCategory.COMMUNITY -> YarRiver
                    }
                )
            )

            Column(Modifier.padding(14.dp)) {
                // Категория + район + официальность
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("${event.category.emoji} ${event.category.displayName}", fontSize = 11.sp)
                        }
                        if (event.isOfficial) {
                            Box(
                                Modifier.clip(RoundedCornerShape(6.dp))
                                    .background(ArtOrange.copy(.12f))
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text("✓ Официальное", fontSize = 11.sp, color = ArtOrangeDark, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Text("${event.district.emoji} ${event.district.displayName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(Modifier.height(10.dp))

                // Название
                Text(event.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 22.sp)

                Spacer(Modifier.height(6.dp))

                // Описание
                Text(
                    event.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                Spacer(Modifier.height(10.dp))

                // Дата и место
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(4.dp))
                        Text(fmtShortDate(event.startTime), fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text(event.location, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                // Теги
                if (event.tags.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        event.tags.take(3).forEach { tag ->
                            Text(
                                "#$tag",
                                Modifier.clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(10.dp))

                // Организатор + кнопка
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.People, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${event.attendeesCount + if (isAttending) 1 else 0} пойдут",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("• ${event.organizerName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    Button(
                        onClick = onAttend,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAttending) YarGreen else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (isAttending) "✓ Пойду" else "Пойду", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
