package com.art.yaroslavl.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.art.yaroslavl.data.model.*
import com.art.yaroslavl.ui.components.*
import com.art.yaroslavl.ui.theme.*
import com.art.yaroslavl.viewmodel.EventDetailViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    onBack: () -> Unit,
    vm: EventDetailViewModel = viewModel(key = eventId) { EventDetailViewModel(eventId) }
) {
    val event by vm.event.collectAsState()
    val attending by vm.attending.collectAsState()

    val e = event ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } },
                title = { Text("Событие", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton({}) { Icon(Icons.Default.Share, null) }
                    IconButton({}) { Icon(Icons.Default.BookmarkBorder, null) }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Поделиться")
                    }
                    Button(
                        onClick = vm::toggleAttend,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (attending) YarGreen else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            if (attending) "✓ Пойду!" else "Я пойду",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Цветной хедер категории
            Box(
                Modifier.fillMaxWidth().height(6.dp).background(
                    when (e.category) {
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

            Column(Modifier.padding(16.dp)) {
                // Категория + официальность
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("${e.category.emoji} ${e.category.displayName}", fontSize = 13.sp)
                    }
                    if (e.isOfficial) {
                        Box(
                            Modifier.clip(RoundedCornerShape(8.dp)).background(ArtOrange.copy(.12f)).padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("✓ Официальное", fontSize = 13.sp, color = ArtOrangeDark, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Заголовок
                Text(e.title, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, lineHeight = 28.sp)

                Spacer(Modifier.height(16.dp))

                // Инфо-блок
                InfoBlock(e)

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(16.dp))

                // Описание
                Text("О событии", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(8.dp))
                Text(e.description, fontSize = 14.sp, lineHeight = 22.sp, color = MaterialTheme.colorScheme.onSurface)

                // Теги
                if (e.tags.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        e.tags.forEach { tag ->
                            Text(
                                "#$tag",
                                Modifier.clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 13.sp, color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(16.dp))

                // Организатор
                Text("Организатор", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(e.organizerName.first().uppercaseChar().toString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(e.organizerName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        if (e.isOfficial) Text("✓ Верифицированный организатор", fontSize = 12.sp, color = ArtOrange)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Счётчик
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(.08f))
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.People, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${e.attendeesCount + if (attending) 1 else 0} ярославцев пойдут на это событие",
                            fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoBlock(e: CityEvent) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        InfoRow(Icons.Default.Schedule, "Начало", fmtEventDate(e.startTime))
        e.endTime?.let { InfoRow(Icons.Default.ScheduleSend, "Конец", fmtEventDate(it)) }
        InfoRow(Icons.Default.LocationOn, "Место", e.location)
        InfoRow(Icons.Default.LocationCity, "Район", "${e.district.emoji} ${e.district.displayName}")
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, Modifier.size(18.dp).padding(top = 1.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
