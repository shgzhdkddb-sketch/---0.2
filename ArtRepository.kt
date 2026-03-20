package com.art.yaroslavl.data.repository

import com.art.yaroslavl.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import java.util.UUID

class ArtRepository {

    // ── Текущий пользователь (демо, без авторизации) ────
    private val me = User(
        id = "me",
        name = "Алексей Громов",
        username = "aleksey_g",
        phone = "+7 (910) 555-12-34",
        isOnline = true,
        district = District.KIROVSKY,
        bio = "Кировский район. Люблю свой город 🐻",
        isVerified = true
    )

    private val users = listOf(
        User("u1", "Марина Волкова",    "marina_v",   district = District.KIROVSKY,   isOnline = true,  role = UserRole.VOLUNTEER, isVerified = true),
        User("u2", "Сергей Лебедев",   "s_lebedev",  district = District.ZAVOLZHSKY, isOnline = false, lastSeen = LocalDateTime.now().minusHours(3)),
        User("u3", "МЧС Ярославль",    "mchs_yar",   role = UserRole.EMERGENCY,      isOnline = true,  isVerified = true),
        User("u4", "Администрация Яр.", "yar_admin",  role = UserRole.OFFICIAL,       isOnline = true,  isVerified = true),
        User("u5", "Ольга Птицына",    "olga_pt",    district = District.LENINSKY,   isOnline = true),
        User("u6", "Дмитрий Орлов",    "d_orlov",    district = District.FRUNZENSKY, isOnline = false, lastSeen = LocalDateTime.now().minusDays(1)),
        User("u7", "Кафе «Медведь»",   "cafe_medved",role = UserRole.BUSINESS,       isOnline = true,  district = District.KIROVSKY),
    )

    // ── Оповещения МЧС ──────────────────────────────────
    private val _alerts = MutableStateFlow(listOf(
        CityAlert(
            "a1", "Паводок: подъём уровня Волги",
            "Уровень воды в Волге 580 см. Жителям прибрежных улиц Заволжского района рекомендуется перенести ценные вещи на верхние этажи. Бригады МЧС дежурят на берегу.",
            AlertType.FLOOD, AlertSeverity.WARNING, District.ZAVOLZHSKY,
            LocalDateTime.now().minusHours(2), LocalDateTime.now().plusHours(48),
            "u3", "МЧС Ярославль"
        ),
        CityAlert(
            "a2", "Плановое отключение электроэнергии",
            "15 апреля с 09:00 до 17:00 — отключение на улицах Свободы, Победы и Революционной. Причина: плановая замена трансформатора.",
            AlertType.POWER, AlertSeverity.INFO, District.KIROVSKY,
            LocalDateTime.now().minusHours(10), LocalDateTime.now().plusDays(2),
            "u4", "Администрация Ярославля"
        )
    ))
    val alerts: StateFlow<List<CityAlert>> = _alerts.asStateFlow()

    // ── События города ───────────────────────────────────
    private val _events = MutableStateFlow(listOf(
        CityEvent(
            "e1", "Субботник на набережной Волги",
            "Большой весенний субботник! Убираем берег Волги вместе. Инвентарь предоставляем, кофе-пауза от кафе «Медведь». Приходи с семьёй — дети в приоритете!",
            EventCategory.CLEANUP, District.KIROVSKY,
            LocalDateTime.now().plusDays(3).withHour(10).withMinute(0),
            LocalDateTime.now().plusDays(3).withHour(14).withMinute(0),
            "Набережная Волги, у Волжской башни", "u1", "Марина Волкова",
            attendeesCount = 47, isOfficial = false,
            tags = listOf("эко", "волонтёрство", "семья")
        ),
        CityEvent(
            "e2", "Ярмарка мастеров на Советской площади",
            "Ежегодная весенняя ярмарка. Более 60 местных мастеров: гончары, кузнецы, ювелиры, ткачи. Бесплатный вход, мастер-классы для детей.",
            EventCategory.MARKET, District.KIROVSKY,
            LocalDateTime.now().plusDays(1).withHour(11).withMinute(0),
            LocalDateTime.now().plusDays(1).withHour(20).withMinute(0),
            "Советская площадь", "u4", "Администрация Ярославля",
            attendeesCount = 320, isOfficial = true,
            tags = listOf("ремёсла", "ярмарка", "дети")
        ),
        CityEvent(
            "e3", "Бесплатная экскурсия «Ярославль купеческий»",
            "Прогулка с историком по центру города. Узнаем о купеческом Ярославле XIX века, зайдём в исторические дворы. Группа до 20 человек.",
            EventCategory.CULTURE, District.KIROVSKY,
            LocalDateTime.now().plusDays(2).withHour(14).withMinute(0), null,
            "Площадь Богоявления (у фонтана)", "u5", "Ольга Птицына",
            attendeesCount = 18,
            tags = listOf("история", "экскурсия", "бесплатно")
        ),
        CityEvent(
            "e4", "Дворовой футбол на Ленина, 27",
            "Дружеский матч жителей двора. Играем по средам — все желающие от 12 лет. Форма не нужна, мяч есть.",
            EventCategory.SPORT, District.FRUNZENSKY,
            LocalDateTime.now().plusDays(1).withHour(18).withMinute(0), null,
            "ул. Ленина, 27 — спортплощадка во дворе", "u6", "Дмитрий Орлов",
            attendeesCount = 14,
            tags = listOf("спорт", "двор", "еженедельно")
        ),
        CityEvent(
            "e5", "Обсуждение маршрута 4-го трамвая",
            "Встреча жителей Заволжья с представителем мэрии по поводу изменения маршрута трамвая №4. Ваш голос важен — приходите!",
            EventCategory.TRANSPORT, District.ZAVOLZHSKY,
            LocalDateTime.now().plusDays(4).withHour(19).withMinute(0), null,
            "ДК Заволжского района, зал №2", "u4", "Администрация Ярославля",
            attendeesCount = 63, isOfficial = true,
            tags = listOf("транспорт", "трамвай", "жкх")
        ),
        CityEvent(
            "e6", "Фестиваль уличной еды «Вкусный Ярославль»",
            "Более 30 локальных фудтраков и кафе. Конкурс на лучший ярославский рецепт, живая музыка, детская зона.",
            EventCategory.FESTIVAL, District.KIROVSKY,
            LocalDateTime.now().plusDays(7).withHour(12).withMinute(0),
            LocalDateTime.now().plusDays(7).withHour(22).withMinute(0),
            "Стрелка (у слияния Волги и Которосли)", "u4", "Администрация Ярославля",
            attendeesCount = 540, isOfficial = true,
            tags = listOf("еда", "фестиваль", "музыка")
        ),
        CityEvent(
            "e7", "Субботник в Фрунзенском сквере",
            "Весенняя уборка сквера. Нужны руки, перчатки и хорошее настроение. После — чай и печенье!",
            EventCategory.CLEANUP, District.FRUNZENSKY,
            LocalDateTime.now().plusDays(5).withHour(10).withMinute(0), null,
            "Сквер на ул. Чехова", "u6", "Дмитрий Орлов",
            attendeesCount = 22,
            tags = listOf("эко", "двор")
        ),
    ))
    val events: StateFlow<List<CityEvent>> = _events.asStateFlow()

    // ── Сообщения ────────────────────────────────────────
    private val _messages = MutableStateFlow<Map<String, List<Message>>>(mapOf(
        "chat_kirovsky" to listOf(
            Message("m1", "chat_kirovsky", "u1", MessageContent.Text("Соседи, когда починят лифт на Свободы 12?"), LocalDateTime.now().minusHours(4), MessageStatus.READ),
            Message("m2", "chat_kirovsky", "u5", MessageContent.Text("Написала в ЖЭК — обещали до пятницы 🙏"), LocalDateTime.now().minusHours(3), MessageStatus.READ),
            Message("m3", "chat_kirovsky", "me", MessageContent.Text("Спасибо! Буду знать 👍"), LocalDateTime.now().minusHours(3), MessageStatus.READ),
            Message("m4", "chat_kirovsky", "u7", MessageContent.Text("Кафе «Медведь» угощает всех волонтёров субботника горячим чаем ☕"), LocalDateTime.now().minusMinutes(30), MessageStatus.DELIVERED),
        ),
        "chat_zavolzhsky" to listOf(
            Message("m5", "chat_zavolzhsky", "u3", MessageContent.Alert(
                CityAlert("a1", "Паводок: подъём Волги", "Уровень 580 см. Будьте осторожны.", AlertType.FLOOD, AlertSeverity.WARNING, District.ZAVOLZHSKY, LocalDateTime.now().minusHours(2), null, "u3", "МЧС")
            ), LocalDateTime.now().minusHours(2), MessageStatus.READ),
            Message("m6", "chat_zavolzhsky", "u2", MessageContent.Text("У меня уже вода в подвале, МЧС вызвал"), LocalDateTime.now().minusHours(1), MessageStatus.READ),
            Message("m7", "chat_zavolzhsky", "u3", MessageContent.Text("Бригада выехала, ~20 минут"), LocalDateTime.now().minusMinutes(55), MessageStatus.READ),
            Message("m8", "chat_zavolzhsky", "u2", MessageContent.Text("Спасибо! Очень быстро!"), LocalDateTime.now().minusMinutes(50), MessageStatus.DELIVERED),
        ),
        "chat_private_u1" to listOf(
            Message("m9", "chat_private_u1", "u1", MessageContent.Text("Алексей, ты идёшь на субботник?"), LocalDateTime.now().minusHours(1), MessageStatus.READ),
            Message("m10", "chat_private_u1", "me", MessageContent.Text("Да! Возьму перчатки 💪"), LocalDateTime.now().minusMinutes(50), MessageStatus.READ),
            Message("m11", "chat_private_u1", "u1", MessageContent.Text("Встречаемся у спуска в 10:00 🙌"), LocalDateTime.now().minusMinutes(40), MessageStatus.DELIVERED),
        ),
        "chat_emergency" to listOf(
            Message("m12", "chat_emergency", "u4", MessageContent.Text("Ярославцы, в случае ЧС — звоните 112. Мы всегда на связи."), LocalDateTime.now().minusHours(6), MessageStatus.READ),
            Message("m13", "chat_emergency", "u3", MessageContent.Alert(
                CityAlert("a1", "Паводок: уровень Волги", "Уровень 580 см, ситуация под контролем МЧС", AlertType.FLOOD, AlertSeverity.WARNING, District.ALL_CITY, LocalDateTime.now().minusHours(2), null, "u3", "МЧС Ярославль")
            ), LocalDateTime.now().minusHours(2), MessageStatus.READ),
        ),
        "chat_marketplace" to listOf(
            Message("m14", "chat_marketplace", "u6", MessageContent.Text("Продаю велосипед горный, 2021, почти новый. 12 000 руб. Фрунзенский"), LocalDateTime.now().minusHours(3), MessageStatus.READ),
            Message("m15", "chat_marketplace", "u5", MessageContent.Text("Нашли рыжего кота в Кировском, ищем хозяев! ❤️"), LocalDateTime.now().minusHours(1), MessageStatus.READ),
            Message("m16", "chat_marketplace", "u1", MessageContent.Text("Отдам детские книги Носова — полное собрание, только самовывоз"), LocalDateTime.now().minusMinutes(15), MessageStatus.SENT),
        ),
    ))

    // ── Чаты ─────────────────────────────────────────────
    private val _chats = MutableStateFlow(listOf(
        Chat("chat_emergency", ChatType.EMERGENCY, "🆘 Экстренный канал", "МЧС и администрация — официальные оповещения", District.ALL_CITY, memberCount = 47823, isOfficial = true, isPinned = true,
            lastMessage = _messages.value["chat_emergency"]?.last(), unreadCount = 0),
        Chat("chat_kirovsky", ChatType.DISTRICT, "Кировский район 🏛️", "Чат жителей Кировского", District.KIROVSKY, memberCount = 3241, isPinned = true,
            lastMessage = _messages.value["chat_kirovsky"]?.last(), unreadCount = 2),
        Chat("chat_zavolzhsky", ChatType.DISTRICT, "Заволжский район 🌊", "Чат жителей Заволжья", District.ZAVOLZHSKY, memberCount = 2876,
            lastMessage = _messages.value["chat_zavolzhsky"]?.last(), unreadCount = 0),
        Chat("chat_private_u1", ChatType.PRIVATE, "Марина Волкова", participants = listOf("me", "u1"),
            lastMessage = _messages.value["chat_private_u1"]?.last(), unreadCount = 1),
        Chat("chat_marketplace", ChatType.MARKETPLACE, "Барахолка 🛒", "Купля-продажа для ярославцев", District.ALL_CITY, memberCount = 12540,
            lastMessage = _messages.value["chat_marketplace"]?.last(), unreadCount = 2),
    ))

    // ── Барахолка ─────────────────────────────────────────
    private val _listings = MutableStateFlow(listOf(
        Listing("l1", "Детские книги Носова — полное собрание", "Состояние хорошее. Только самовывоз, Кировский.", null, ListingCategory.GIVE_FREE, District.KIROVSKY, "u1", "Марина Волкова", true),
        Listing("l2", "Горный велосипед 2021", "Почти не использовался. Фрунзенский, самовывоз.", 12000, ListingCategory.SELL, District.FRUNZENSKY, "u6", "Дмитрий Орлов"),
        Listing("l3", "Мастер — сантехника и электрика", "Ремонт кранов, труб, розеток. Работаю по всему Ярославлю.", null, ListingCategory.SERVICE, District.ALL_CITY, "u2", "Сергей Лебедев"),
        Listing("l4", "🔍 Найден рыжий кот, ищем хозяев", "Нашли в Кировском у Волжской башни. Здоров, ухожен.", null, ListingCategory.LOST_FOUND, District.KIROVSKY, "u5", "Ольга Птицына", true),
        Listing("l5", "Комната 18 кв.м. в Заволжском", "Светлая, рядом трамвай. 12 000 руб./мес. + ком. услуги.", 12000, ListingCategory.RENT, District.ZAVOLZHSKY, "u2", "Сергей Лебедев"),
        Listing("l6", "Куплю советские монеты", "Любое состояние, любые номиналы. Звоните или пишите.", null, ListingCategory.BUY, District.ALL_CITY, "u6", "Дмитрий Орлов"),
    ))

    // ── Public API ────────────────────────────────────────
    fun getMe() = me
    fun getUserById(id: String) = if (id == "me") me else users.find { it.id == id }
    fun getChats(): Flow<List<Chat>> = _chats.asStateFlow()
    fun getMessages(chatId: String): Flow<List<Message>> = _messages.map { it[chatId] ?: emptyList() }
    fun getChatById(id: String) = _chats.value.find { it.id == id }
    fun getEvents(): Flow<List<CityEvent>> = _events.asStateFlow()
    fun getAlerts(): Flow<List<CityAlert>> = _alerts.map { it.filter { a -> a.isActive } }
    fun getListings(): Flow<List<Listing>> = _listings.map { it.filter { l -> l.isActive } }

    fun attendEvent(eventId: String) {
        _events.value = _events.value.map { if (it.id == eventId) it.copy(attendeesCount = it.attendeesCount + 1) else it }
    }

    fun markRead(chatId: String) {
        _chats.value = _chats.value.map { if (it.id == chatId) it.copy(unreadCount = 0) else it }
    }

    suspend fun sendMessage(chatId: String, content: MessageContent) {
        val msg = Message(UUID.randomUUID().toString(), chatId, "me", content, LocalDateTime.now(), MessageStatus.SENDING)
        val m = _messages.value.toMutableMap()
        m[chatId] = (m[chatId] ?: emptyList()) + msg
        _messages.value = m

        _chats.value = _chats.value.map { if (it.id == chatId) it.copy(lastMessage = msg) else it }

        delay(450)
        val m2 = _messages.value.toMutableMap()
        m2[chatId] = (m2[chatId] ?: emptyList()).map { if (it.id == msg.id) it.copy(status = MessageStatus.DELIVERED) else it }
        _messages.value = m2
    }
}
