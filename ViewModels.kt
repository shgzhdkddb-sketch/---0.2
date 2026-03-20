package com.art.yaroslavl.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.art.yaroslavl.data.model.*
import com.art.yaroslavl.data.repository.ArtRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ── Лента событий (главный экран) ───────────────
class CityFeedViewModel(
    private val repo: ArtRepository = ArtRepository()
) : ViewModel() {

    private val _district = MutableStateFlow<District?>(null)
    val selectedDistrict = _district.asStateFlow()

    private val _category = MutableStateFlow<EventCategory?>(null)
    val selectedCategory = _category.asStateFlow()

    val alerts: StateFlow<List<CityAlert>> = repo.getAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val events: StateFlow<List<CityEvent>> = combine(
        repo.getEvents(), _district, _category
    ) { events, district, category ->
        events
            .filter { district == null || it.district == district || it.district == District.ALL_CITY }
            .filter { category == null || it.category == category }
            .sortedWith(compareByDescending<CityEvent> { it.isEmergency }.thenBy { it.startTime })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _attending = MutableStateFlow<Set<String>>(emptySet())
    val attending = _attending.asStateFlow()

    fun setDistrict(d: District?) { _district.value = d }
    fun setCategory(c: EventCategory?) { _category.value = c }

    fun toggleAttend(eventId: String) {
        val current = _attending.value
        if (eventId in current) {
            _attending.value = current - eventId
        } else {
            _attending.value = current + eventId
            repo.attendEvent(eventId)
        }
    }
}

// ── Детали события ───────────────────────────────
class EventDetailViewModel(
    val eventId: String,
    private val repo: ArtRepository = ArtRepository()
) : ViewModel() {

    val event: StateFlow<CityEvent?> = repo.getEvents()
        .map { it.find { e -> e.id == eventId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _attending = MutableStateFlow(false)
    val attending = _attending.asStateFlow()

    fun toggleAttend() {
        _attending.value = !_attending.value
        if (_attending.value) repo.attendEvent(eventId)
    }
}

// ── Список чатов ─────────────────────────────────
class ChatListViewModel(
    private val repo: ArtRepository = ArtRepository()
) : ViewModel() {

    val me = repo.getMe()

    private val _search = MutableStateFlow("")
    val search = _search.asStateFlow()

    val chats: StateFlow<List<Chat>> = combine(repo.getChats(), _search) { chats, q ->
        chats.filter { q.isBlank() || it.name.contains(q, true) }
            .sortedWith(compareByDescending<Chat> { it.isPinned }.thenByDescending { it.lastMessage?.timestamp })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalUnread: StateFlow<Int> = repo.getChats()
        .map { it.sumOf { c -> c.unreadCount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val alerts: StateFlow<List<CityAlert>> = repo.getAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearch(q: String) { _search.value = q }
    fun getUser(id: String) = repo.getUserById(id)
    fun getOtherUser(chat: Chat) = chat.participants.firstOrNull { it != me.id }?.let { repo.getUserById(it) }
}

// ── Экран чата ────────────────────────────────────
class ChatViewModel(
    val chatId: String,
    private val repo: ArtRepository = ArtRepository()
) : ViewModel() {

    val me = repo.getMe()

    val chat: StateFlow<Chat?> = repo.getChats()
        .map { it.find { c -> c.id == chatId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val messages: StateFlow<List<Message>> = repo.getMessages(chatId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _input = MutableStateFlow("")
    val input = _input.asStateFlow()

    private val _sending = MutableStateFlow(false)
    val sending = _sending.asStateFlow()

    val canSend = _input.map { it.isNotBlank() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init { repo.markRead(chatId) }

    fun onInput(v: String) { _input.value = v }

    fun send() {
        val txt = _input.value.trim().ifBlank { return }
        _input.value = ""
        viewModelScope.launch {
            _sending.value = true
            repo.sendMessage(chatId, MessageContent.Text(txt))
            _sending.value = false
        }
    }

    fun isMe(msg: Message) = msg.senderId == me.id
    fun getSender(id: String) = repo.getUserById(id)
    fun getOtherUser() = chat.value?.participants?.firstOrNull { it != me.id }?.let { repo.getUserById(it) }
}

// ── Барахолка ─────────────────────────────────────
class MarketplaceViewModel(
    private val repo: ArtRepository = ArtRepository()
) : ViewModel() {

    private val _cat = MutableStateFlow<ListingCategory?>(null)
    val selectedCat = _cat.asStateFlow()

    private val _search = MutableStateFlow("")
    val search = _search.asStateFlow()

    val listings: StateFlow<List<Listing>> = combine(repo.getListings(), _cat, _search) { all, cat, q ->
        all.filter { cat == null || it.category == cat }
           .filter { q.isBlank() || it.title.contains(q, true) || it.description.contains(q, true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getUser(id: String) = repo.getUserById(id)
    fun setCat(c: ListingCategory?) { _cat.value = c }
    fun setSearch(q: String) { _search.value = q }
}

// ── Профиль ───────────────────────────────────────
class ProfileViewModel(
    private val repo: ArtRepository = ArtRepository()
) : ViewModel() {

    val me = repo.getMe()
    private val _dark = MutableStateFlow(false)
    val darkTheme = _dark.asStateFlow()
    private val _notif = MutableStateFlow(true)
    val notifications = _notif.asStateFlow()
    private val _emergency = MutableStateFlow(true)
    val emergencyAlerts = _emergency.asStateFlow()

    fun toggleDark() { _dark.value = !_dark.value }
    fun toggleNotif() { _notif.value = !_notif.value }
    fun toggleEmergency() { _emergency.value = !_emergency.value }
}
