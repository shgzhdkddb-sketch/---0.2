package com.art.yaroslavl.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.*
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.art.yaroslavl.ui.screen.*

sealed class Screen(val route: String) {
    object Events      : Screen("events")
    object Chats       : Screen("chats")
    object Marketplace : Screen("marketplace")
    object Profile     : Screen("profile")
    object Chat        : Screen("chat/{chatId}") { fun go(id: String) = "chat/$id" }
    object EventDetail : Screen("event/{eventId}") { fun go(id: String) = "event/$id" }
}

private data class NavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val iconFilled: ImageVector
)

private val navItems = listOf(
    NavItem(Screen.Events,      "События",   Icons.Outlined.LocationCity,      Icons.Filled.LocationCity),
    NavItem(Screen.Chats,       "Чаты",      Icons.Outlined.ChatBubbleOutline,  Icons.Filled.ChatBubble),
    NavItem(Screen.Marketplace, "Барахолка", Icons.Outlined.ShoppingBag,        Icons.Filled.ShoppingBag),
    NavItem(Screen.Profile,     "Профиль",   Icons.Outlined.AccountCircle,       Icons.Filled.AccountCircle),
)

@Composable
fun ArtNavigation() {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val current = entry?.destination?.route
    val rootRoutes = navItems.map { it.screen.route }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(current in rootRoutes) {
                NavigationBar {
                    navItems.forEach { item ->
                        val sel = current == item.screen.route
                        NavigationBarItem(
                            selected = sel,
                            onClick = {
                                nav.navigate(item.screen.route) {
                                    popUpTo(Screen.Events.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(if (sel) item.iconFilled else item.icon, item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(nav, Screen.Events.route, Modifier.padding(padding)) {
            composable(Screen.Events.route) {
                CityFeedScreen(
                    onEventClick = { nav.navigate(Screen.EventDetail.go(it)) }
                )
            }
            composable(Screen.Chats.route) {
                ChatListScreen(onChatClick = { nav.navigate(Screen.Chat.go(it)) })
            }
            composable(Screen.Marketplace.route) {
                MarketplaceScreen(onChatClick = { nav.navigate(Screen.Chat.go(it)) })
            }
            composable(Screen.Profile.route) {
                ProfileScreen()
            }
            composable(Screen.Chat.route,
                arguments = listOf(navArgument("chatId") { type = NavType.StringType })
            ) { back ->
                ChatScreen(back.arguments!!.getString("chatId")!!, onBack = { nav.popBackStack() })
            }
            composable(Screen.EventDetail.route,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { back ->
                EventDetailScreen(back.arguments!!.getString("eventId")!!, onBack = { nav.popBackStack() })
            }
        }
    }
}
