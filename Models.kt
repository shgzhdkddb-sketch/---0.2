package com.art.yaroslavl.data.model

import java.time.LocalDateTime

// ════════════════════════════════════════════════
//  ПОЛЬЗОВАТЕЛЬ
// ════════════════════════════════════════════════
data class User(
    val id: String,
    val name: String,
    val username: String,
    val phone: String = "",
    val avatarUrl: String? = null,
    val isOnline: Boolean = false,
    val lastSeen: LocalDateTime? = null,
    val bio: String = "",
    val district: District? = null,
    val isVerified: Boolean = false,
    val role: UserRole = UserRole.CITIZEN
)

enum class UserRole(val label: String) {
    CITIZEN("Житель"),
    VOLUNTEER("Волонтёр"),
    BUSINESS("Бизнес"),
    OFFICIAL("Администрация"),
    EMERGENCY("МЧС / Службы")
}

// ════════════════════════════════════════════════
//  РАЙОНЫ ЯРОСЛАВЛЯ
// ════════════════════════════════════════════════
enum class District(val displayName: String, val emoji: String) {
    ALL_CITY("Весь город", "🐻"),
    KIROVSKY("Кировский", "🏛️"),
    LENINSKY("Ленинский", "🌉"),
    ZAVOLZHSKY("Заволжский", "🌊"),
    FRUNZENSKY("Фрунзенский", "🌳"),
    DZERZHINSKY("Дзержинский", "🏭"),
    KRASNOPEREKOPSKY("Красноперекопский", "🧵"),
    ZAVODSKY("Заводской", "⚙️")
}

// ════════════════════════════════════════════════
//  СОБЫТИЯ ГОРОДА (главный раздел)
// ════════════════════════════════════════════════
enum class EventCategory(val displayName: String, val emoji: String) {
    CULTURE("Культура", "🎭"),
    SPORT("Спорт", "⚽"),
    COMMUNITY("Сообщество", "🤝"),
    CLEANUP("Субботник", "🧹"),
    MARKET("Ярмарка", "🛒"),
    FESTIVAL("Фестиваль", "🎉"),
    TRANSPORT("Транспорт", "🚌"),
    EMERGENCY("Экстренное", "🆘")
}

data class CityEvent(
    val id: String,
    val title: String,
    val description: String,
    val category: EventCategory,
    val district: District,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val location: String,
    val organizerId: String,
    val organizerName: String,
    val attendeesCount: Int = 0,
    val isEmergency: Boolean = false,
    val isOfficial: Boolean = false,
    val tags: List<String> = emptyList()
)

// ════════════════════════════════════════════════
//  ОПОВЕЩЕНИЯ МЧС / АДМИНИСТРАЦИИ
// ════════════════════════════════════════════════
enum class AlertSeverity(val label: String) {
    INFO("Информация"),
    WARNING("Предупреждение"),
    CRITICAL("Критично")
}

enum class AlertType(val label: String, val emoji: String) {
    FLOOD("Паводок", "🌊"),
    FIRE("Пожар", "🔥"),
    GAS("Утечка газа", "⚠️"),
    POWER("Отключение света", "⚡"),
    ROAD("ДТП", "🚗"),
    MISSING("Пропавший человек", "🔍"),
    OTHER("Другое", "📢")
}

data class CityAlert(
    val id: String,
    val title: String,
    val description: String,
    val type: AlertType,
    val severity: AlertSeverity,
    val district: District,
    val issuedAt: LocalDateTime,
    val expiresAt: LocalDateTime? = null,
    val sourceId: String,
    val sourceName: String,
    val isActive: Boolean = true
)

// ════════════════════════════════════════════════
//  СООБЩЕНИЯ
// ════════════════════════════════════════════════
enum class MessageStatus { SENDING, SENT, DELIVERED, READ }

sealed class MessageContent {
    data class Text(val text: String) : MessageContent()
    data class Image(val url: String, val caption: String? = null) : MessageContent()
    data class Audio(val url: String, val durationSeconds: Int) : MessageContent()
    data class Location(val lat: Double, val lng: Double, val address: String) : MessageContent()
    data class Alert(val cityAlert: CityAlert) : MessageContent()
}

data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val content: MessageContent,
    val timestamp: LocalDateTime,
    val status: MessageStatus = MessageStatus.SENT,
    val replyToId: String? = null,
    val isEdited: Boolean = false
)

// ════════════════════════════════════════════════
//  ЧАТЫ
// ════════════════════════════════════════════════
enum class ChatType { PRIVATE, DISTRICT, EMERGENCY, MARKETPLACE }

data class Chat(
    val id: String,
    val type: ChatType,
    val name: String,
    val description: String = "",
    val district: District? = null,
    val participants: List<String> = emptyList(),
    val lastMessage: Message? = null,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val memberCount: Int = 0,
    val isOfficial: Boolean = false
)

// ════════════════════════════════════════════════
//  БАРАХОЛКА
// ════════════════════════════════════════════════
enum class ListingCategory(val displayName: String, val emoji: String) {
    GIVE_FREE("Отдам даром", "🎁"),
    SELL("Продам", "💰"),
    BUY("Куплю", "🛍️"),
    SERVICE("Услуги", "🔧"),
    RENT("Аренда", "🏠"),
    LOST_FOUND("Потеряли/Нашли", "🔍")
}

data class Listing(
    val id: String,
    val title: String,
    val description: String,
    val price: Int? = null,
    val category: ListingCategory,
    val district: District,
    val sellerId: String,
    val sellerName: String,
    val isVerifiedSeller: Boolean = false,
    val isActive: Boolean = true,
    val postedAt: LocalDateTime = LocalDateTime.now()
)
