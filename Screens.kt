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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.art.yaroslavl.data.model.*
import com.art.yaroslavl.ui.components.*
import com.art.yaroslavl.ui.theme.*
import com.art.yaroslavl.viewmodel.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ════════════════════════════════════════════════
//  СПИСОК ЧАТОВ
// ════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(onChatClick: (String) -> Unit, vm: ChatListViewModel = viewModel()) {
    val chats by vm.chats.collectAsState()
    val search by vm.search.collectAsState()
    val unread by vm.totalUnread.collectAsState()
    val alerts by vm.alerts.collectAsState()
    var searchActive by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (searchActive) {
                Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = search, onValueChange = vm::onSearch, modifier = Modifier.weight(1f), singleLine = true,
                        placeholder = { Text("Поиск чатов...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = { if (search.isNotBlank()) IconButton({ vm.onSearch("") }) { Icon(Icons.Default.Clear, null) } },
                        shape = RoundedCornerShape(14.dp)
                    )
                    TextButton({ searchActive = false; vm.onSearch("") }) { Text("Отмена") }
                }
            } else {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🐻", fontSize = 20.sp)
                            Spacer(Modifier.width(6.dp))
                            Column {
                                Text("АРТ — Чаты", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                if (unread > 0) Text("$unread непрочитанных", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    actions = {
                        IconButton({ searchActive = true }) { Icon(Icons.Default.Search, null) }
                        IconButton({}) { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary) }
                    }
                )
            }
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            if (alerts.isNotEmpty()) {
                item {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        alerts.forEach { AlertBanner(it) }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
            item {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    District.values().take(5).forEach { d ->
                        FilterChip(selected = false, onClick = {}, label = { Text("${d.emoji} ${d.displayName}", fontSize = 11.sp) })
                    }
                }
            }
            items(chats, key = { it.id }) { chat ->
                ChatRow(chat, vm.getOtherUser(chat), vm.me.id, onChatClick)
                HorizontalDivider(Modifier.padding(start = 76.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun ChatRow(chat: Chat, other: User?, meId: String, onClick: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onClick(chat.id) }
            .background(if (chat.isPinned) MaterialTheme.colorScheme.surfaceVariant.copy(.4f) else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (chat.type == ChatType.PRIVATE) {
            ArtAvatar(other, chat.name, size = 52.dp)
        } else {
            Box(
                Modifier.size(52.dp).clip(CircleShape).background(
                    when (chat.type) {
                        ChatType.EMERGENCY   -> DangerRed.copy(.12f)
                        ChatType.DISTRICT    -> YarGreen.copy(.12f)
                        ChatType.MARKETPLACE -> ArtOrange.copy(.12f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ), contentAlignment = Alignment.Center
            ) {
                Text(when (chat.type) {
                    ChatType.EMERGENCY   -> "🆘"
                    ChatType.DISTRICT    -> "🏙️"
                    ChatType.MARKETPLACE -> "🛒"
                    else -> "💬"
                }, fontSize = 22.sp)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    if (chat.isPinned) { Icon(Icons.Default.PushPin, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(3.dp)) }
                    if (chat.isOfficial) Text("✓ ", fontSize = 13.sp, color = ArtOrange, fontWeight = FontWeight.Bold)
                    Text(chat.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                chat.lastMessage?.timestamp?.let {
                    Text(fmtTime(it), fontSize = 11.sp, color = if (chat.unreadCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(2.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                val preview = when (val c = chat.lastMessage?.content) {
                    is MessageContent.Text  -> if (chat.lastMessage.senderId == meId) "Вы: ${c.text}" else c.text
                    is MessageContent.Alert -> "🆘 ${c.cityAlert.title}"
                    is MessageContent.Audio -> "🎵 Голосовое"
                    is MessageContent.Location -> "📍 Геопозиция"
                    else -> "Нет сообщений"
                }
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    if (chat.lastMessage?.senderId == meId) { StatusIcon(chat.lastMessage.status); Spacer(Modifier.width(3.dp)) }
                    Text(preview, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (chat.unreadCount > 0) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier.clip(CircleShape).background(if (chat.type == ChatType.EMERGENCY) DangerRed else MaterialTheme.colorScheme.primary).padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (chat.unreadCount > 99) "99+" else "${chat.unreadCount}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════
//  ЧАТ
// ════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(chatId: String, onBack: () -> Unit, vm: ChatViewModel = viewModel(key = chatId) { ChatViewModel(chatId) }) {
    val chat by vm.chat.collectAsState()
    val messages by vm.messages.collectAsState()
    val input by vm.input.collectAsState()
    val sending by vm.sending.collectAsState()
    val canSend by vm.canSend.collectAsState()
    val listState = rememberLazyListState()
    val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
    val other = vm.getOtherUser()
    val isEmergency = chat?.type == ChatType.EMERGENCY

    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ArtAvatar(other, chat?.name ?: "", size = 36.dp)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (chat?.isOfficial == true) Text("✓ ", color = ArtOrange, fontSize = 13.sp)
                                Text(chat?.name ?: "", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            }
                            Text(
                                if (chat?.type == ChatType.PRIVATE) {
                                    if (other?.isOnline == true) "онлайн" else fmtLastSeen(other?.lastSeen)
                                } else "${fmtCount(chat?.memberCount ?: 0)} участников",
                                fontSize = 12.sp,
                                color = if (other?.isOnline == true && chat?.type == ChatType.PRIVATE) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (!isEmergency) IconButton({}) { Icon(Icons.Default.Call, null) }
                    IconButton({}) { Icon(Icons.Default.MoreVert, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = if (isEmergency) DangerRed.copy(.07f) else MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            if (isEmergency) {
                Surface(shadowElevation = 8.dp) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, null, tint = DangerRed, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Только для чтения • Экстренный вызов: 112", fontSize = 13.sp, color = DangerRed)
                    }
                }
            } else {
                Surface(shadowElevation = 8.dp) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.Bottom) {
                        IconButton({}) { Icon(Icons.Default.AttachFile, null, tint = MaterialTheme.colorScheme.primary) }
                        OutlinedTextField(
                            value = input, onValueChange = vm::onInput,
                            modifier = Modifier.weight(1f), maxLines = 5,
                            placeholder = { Text("Сообщение...") },
                            shape = RoundedCornerShape(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        AnimatedContent(canSend, label = "btn") { has ->
                            IconButton(
                                onClick = if (has) vm::send else ({}),
                                enabled = !sending,
                                modifier = Modifier.size(46.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)
                            ) {
                                if (sending) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                else Icon(if (has) Icons.Default.Send else Icons.Default.Mic, null, tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            messages.groupBy { it.timestamp.toLocalDate() }.forEach { (date, msgs) ->
                item(key = date.toString()) {
                    Box(Modifier.fillMaxWidth().padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                        Text(
                            when (date) {
                                LocalDate.now() -> "Сегодня"
                                LocalDate.now().minusDays(1) -> "Вчера"
                                else -> date.format(DateTimeFormatter.ofPattern("d MMMM"))
                            },
                            Modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 14.dp, vertical = 4.dp),
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(msgs, key = { it.id }) { msg ->
                    val isMe = vm.isMe(msg)
                    // Сообщение-оповещение
                    if (msg.content is MessageContent.Alert) {
                        val alert = msg.content.cityAlert
                        Box(Modifier.fillMaxWidth().padding(vertical = 3.dp), contentAlignment = Alignment.Center) {
                            Column(
                                Modifier.fillMaxWidth(.93f).clip(RoundedCornerShape(12.dp))
                                    .background(DangerRed.copy(.08f)).border(1.dp, DangerRed.copy(.3f), RoundedCornerShape(12.dp)).padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(alert.type.emoji, fontSize = 20.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Text(alert.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DangerRed)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(alert.description, fontSize = 12.sp)
                                Text("${alert.district.emoji} ${alert.district.displayName} • ${alert.sourceName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        return@items
                    }
                    // Обычное сообщение
                    val shape = if (isMe) RoundedCornerShape(18.dp, 4.dp, 18.dp, 18.dp) else RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp)
                    val bgColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    val txtColor = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    val sender = vm.getSender(msg.senderId)
                    val showName = chat?.type != ChatType.PRIVATE && !isMe

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start) {
                        if (!isMe && showName) { ArtAvatar(sender, sender?.name ?: "?", 26.dp, false); Spacer(Modifier.width(5.dp)) }
                        Box(Modifier.widthIn(max = 270.dp).clip(shape).background(bgColor).padding(horizontal = 11.dp, vertical = 8.dp)) {
                            Column {
                                if (showName) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(sender?.name ?: "?", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        sender?.role?.let { if (it != UserRole.CITIZEN) { Spacer(Modifier.width(4.dp)); RoleBadge(it) } }
                                    }
                                    Spacer(Modifier.height(2.dp))
                                }
                                when (val c = msg.content) {
                                    is MessageContent.Text -> Text(c.text, color = txtColor, fontSize = 14.sp)
                                    is MessageContent.Audio -> Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Mic, null, Modifier.size(15.dp), tint = txtColor); Spacer(Modifier.width(4.dp)); Text("${c.durationSeconds}с", color = txtColor, fontSize = 13.sp) }
                                    is MessageContent.Location -> Text("📍 ${c.address}", color = txtColor, fontSize = 13.sp)
                                    else -> {}
                                }
                                Row(Modifier.align(Alignment.End).padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(fmtTime(msg.timestamp), fontSize = 10.sp, color = txtColor.copy(.6f))
                                    if (isMe) { Spacer(Modifier.width(3.dp)); StatusIcon(msg.status) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════
//  БАРАХОЛКА
// ════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(onChatClick: (String) -> Unit, vm: MarketplaceViewModel = viewModel()) {
    val listings by vm.listings.collectAsState()
    val selectedCat by vm.selectedCat.collectAsState()
    val search by vm.search.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("Барахолка", fontWeight = FontWeight.Bold, fontSize = 20.sp); Text("Только для ярославцев", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
                actions = { IconButton({}) { Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary) } }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(value = search, onValueChange = vm::setSearch, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), placeholder = { Text("Поиск...") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true, shape = RoundedCornerShape(14.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 10.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = selectedCat == null, onClick = { vm.setCat(null) }, label = { Text("Все", fontSize = 12.sp) })
                ListingCategory.values().forEach { c -> FilterChip(selected = selectedCat == c, onClick = { vm.setCat(if (selectedCat == c) null else c) }, label = { Text("${c.emoji} ${c.displayName}", fontSize = 12.sp) }) }
            }
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (listings.isEmpty()) item {
                    Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text("Ничего не найдено", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                else items(listings, key = { it.id }) { listing ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(Modifier.padding(14.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Box(Modifier.clip(RoundedCornerShape(6.dp)).background(
                                    when (listing.category) { ListingCategory.GIVE_FREE -> YarGreen.copy(.12f); ListingCategory.SELL -> ArtOrange.copy(.12f); ListingCategory.LOST_FOUND -> DangerRed.copy(.12f); else -> MaterialTheme.colorScheme.surfaceVariant }
                                ).padding(horizontal = 8.dp, vertical = 3.dp)) {
                                    Text("${listing.category.emoji} ${listing.category.displayName}", fontSize = 11.sp, color = when(listing.category) { ListingCategory.GIVE_FREE -> YarGreen; ListingCategory.SELL -> ArtOrangeDark; ListingCategory.LOST_FOUND -> DangerRed; else -> MaterialTheme.colorScheme.onSurfaceVariant }, fontWeight = FontWeight.SemiBold)
                                }
                                Text("${listing.district.emoji} ${listing.district.displayName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(listing.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(listing.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(10.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(if (listing.price == null) "Договорная / бесплатно" else "${listing.price} ₽", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (listing.price == null) YarGreen else MaterialTheme.colorScheme.onSurface)
                                OutlinedButton(onClick = { onChatClick("chat_marketplace") }, shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp)) {
                                    Icon(Icons.Default.ChatBubbleOutline, null, Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Написать", fontSize = 13.sp)
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(listing.sellerName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (listing.isVerifiedSeller) { Spacer(Modifier.width(3.dp)); Text("✓", fontSize = 12.sp, color = ArtOrange, fontWeight = FontWeight.Bold) }
                                Spacer(Modifier.weight(1f))
                                Text(listing.postedAt.format(DateTimeFormatter.ofPattern("d MMM")), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════
//  ПРОФИЛЬ
// ════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(vm: ProfileViewModel = viewModel()) {
    val user = vm.me
    val dark by vm.darkTheme.collectAsState()
    val notif by vm.notifications.collectAsState()
    val emg by vm.emergencyAlerts.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Мой профиль", fontWeight = FontWeight.Bold) }, actions = { IconButton({}) { Icon(Icons.Default.Edit, null) } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            // Шапка
            Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary.copy(.07f)).padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ArtAvatar(user, user.name, 88.dp, false)
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(user.name, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        if (user.isVerified) { Spacer(Modifier.width(6.dp)); Text("✓", fontSize = 18.sp, color = ArtOrange, fontWeight = FontWeight.Bold) }
                    }
                    Text("@${user.username}", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    if (user.bio.isNotBlank()) { Spacer(Modifier.height(6.dp)); Text(user.bio, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center) }
                    user.district?.let { d ->
                        Spacer(Modifier.height(10.dp))
                        Box(Modifier.clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 14.dp, vertical = 5.dp)) {
                            Text("${d.emoji} ${d.displayName}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            fun sectionLabel(label: String) {}

            // Настройки
            Text("УВЕДОМЛЕНИЯ", Modifier.padding(horizontal = 16.dp, vertical = 6.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
            ToggleItem(Icons.Default.Notifications, "Push-уведомления", notif, vm::toggleNotif)
            ToggleItem(Icons.Default.Warning, "Экстренные оповещения МЧС", emg, vm::toggleEmergency, DangerRed)

            Spacer(Modifier.height(8.dp))
            Text("ПРИЛОЖЕНИЕ", Modifier.padding(horizontal = 16.dp, vertical = 6.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
            ToggleItem(Icons.Default.DarkMode, "Тёмная тема", dark, vm::toggleDark)
            ClickItem(Icons.Default.LocationCity, "Мой район", user.district?.displayName)
            ClickItem(Icons.Default.Security, "Безопасность")
            ClickItem(Icons.Default.Info, "О приложении АРТ")

            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Logout, null); Spacer(Modifier.width(8.dp)); Text("Выйти")
            }
            Spacer(Modifier.height(8.dp))
            Text("АРТ — Ярославль v1.0.0\nСделано с ❤️ для ярославцев", Modifier.fillMaxWidth().padding(16.dp), textAlign = TextAlign.Center, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ToggleItem(icon: ImageVector, label: String, checked: Boolean, toggle: () -> Unit, tint: Color? = null) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(20.dp), tint = tint ?: MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(14.dp)); Text(label, Modifier.weight(1f), fontSize = 14.sp)
        Switch(checked = checked, onCheckedChange = { toggle() })
    }
    HorizontalDivider(Modifier.padding(start = 50.dp))
}

@Composable
private fun ClickItem(icon: ImageVector, label: String, subtitle: String? = null) {
    Row(Modifier.fillMaxWidth().clickable {}.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(label, fontSize = 14.sp)
            if (subtitle != null) Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    HorizontalDivider(Modifier.padding(start = 50.dp))
}
