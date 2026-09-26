package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudySphereTopBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    userXp: Int = 1850,
    userStreak: Int = 14,
    notifications: List<com.example.data.local.NotificationEntity> = emptyList(),
    onMarkNotificationRead: (String) -> Unit = {},
    isDarkMode: Boolean = false,
    onToggleTheme: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showNotificationsDialog by remember { mutableStateOf(false) }
    val unreadCount = notifications.count { !it.isRead }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onNavigate(AppScreen.HOME) }
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "StudySphere Logo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "StudySphere",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "AI + Social Learning",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Badges & Action Icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Streak Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDarkMode) Color(0xFF78350F).copy(alpha = 0.45f) else Color(0xFFFEF3C7),
                    border = if (isDarkMode) BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.5f)) else null,
                    modifier = Modifier.clickable { onNavigate(AppScreen.STUDY_DASHBOARD) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = if (isDarkMode) Color(0xFFFBBF24) else Color(0xFFD97706),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${userStreak}d",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isDarkMode) Color(0xFFFDE68A) else Color(0xFF92400E)
                        )
                    }
                }

                // XP Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDarkMode) Color(0xFF312E81).copy(alpha = 0.45f) else Color(0xFFEEF2FF),
                    border = if (isDarkMode) BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.5f)) else null,
                    modifier = Modifier.clickable { onNavigate(AppScreen.PROFILE) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = "XP",
                            tint = if (isDarkMode) Color(0xFFA5B4FC) else Color(0xFF4F46E5),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$userXp XP",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isDarkMode) Color(0xFFE0E7FF) else Color(0xFF3730A3)
                        )
                    }
                }

                // Global Theme Toggle (Sun ☀️ / Moon 🌙)
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        contentDescription = if (isDarkMode) "Switch to Light Mode" else "Switch to Night Study Dark Mode",
                        tint = if (isDarkMode) Color(0xFFFBBF24) else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Notifications Bell
                IconButton(
                    onClick = { showNotificationsDialog = true },
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("notifications_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge { Text("$unreadCount") }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (unreadCount > 0) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Chat Icon
                IconButton(
                    onClick = { onNavigate(AppScreen.CHAT) },
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("chat_nav_button")
                ) {
                    Icon(
                        imageVector = if (currentScreen == AppScreen.CHAT) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Messages",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Admin Dashboard trigger
                IconButton(
                    onClick = { onNavigate(AppScreen.ADMIN) },
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("admin_nav_button")
                ) {
                    Icon(
                        imageVector = if (currentScreen == AppScreen.ADMIN) Icons.Filled.AdminPanelSettings else Icons.Outlined.AdminPanelSettings,
                        contentDescription = "Admin Panel",
                        tint = if (currentScreen == AppScreen.ADMIN) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Notifications Dialog
    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Notifications 🔔")
                    if (unreadCount > 0) {
                        Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                            Text(
                                text = "$unreadCount new",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            },
            text = {
                if (notifications.isEmpty()) {
                    Text("No notifications yet. You're all caught up! ✨", style = MaterialTheme.typography.bodyMedium)
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp)
                    ) {
                        items(notifications.size) { index ->
                            val notif = notifications[index]
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (notif.isRead) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onMarkNotificationRead(notif.id)
                                        when (notif.targetScreen) {
                                            "STUDY_DASHBOARD" -> { showNotificationsDialog = false; onNavigate(AppScreen.STUDY_DASHBOARD) }
                                            "HOME" -> { showNotificationsDialog = false; onNavigate(AppScreen.HOME) }
                                            "PROFILE" -> { showNotificationsDialog = false; onNavigate(AppScreen.PROFILE) }
                                        }
                                    }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = notif.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = notif.body,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showNotificationsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun StudySphereBottomNav(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val navItems = listOf(
            NavItem(AppScreen.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
            NavItem(AppScreen.EXPLORE, "Explore", Icons.Filled.Explore, Icons.Outlined.Explore),
            NavItem(AppScreen.CREATE, "Create", Icons.Filled.AddCircle, Icons.Outlined.AddCircleOutline),
            NavItem(AppScreen.VIDEOS, "Videos", Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary),
            NavItem(AppScreen.AI_TUTOR, "AI Tutor", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
            NavItem(AppScreen.CODING_LAB, "Code", Icons.Filled.Code, Icons.Outlined.Code),
            NavItem(AppScreen.MATH_LAB, "Math", Icons.Filled.Calculate, Icons.Outlined.Calculate),
            NavItem(AppScreen.STUDY_DASHBOARD, "Study", Icons.Filled.Timer, Icons.Outlined.Timer),
            NavItem(AppScreen.PROFILE, "Profile", Icons.Filled.Person, Icons.Outlined.PersonOutline)
        )

        // Show top 5 on primary bottom bar + quick pill bar for labs
        val primaryNavs = listOf(
            NavItem(AppScreen.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
            NavItem(AppScreen.EXPLORE, "Explore", Icons.Filled.Explore, Icons.Outlined.Explore),
            NavItem(AppScreen.CREATE, "Create", Icons.Filled.AddCircle, Icons.Outlined.AddCircleOutline),
            NavItem(AppScreen.AI_TUTOR, "AI Tutor", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
            NavItem(AppScreen.STUDY_DASHBOARD, "Study", Icons.Filled.Timer, Icons.Outlined.Timer),
            NavItem(AppScreen.PROFILE, "Profile", Icons.Filled.Person, Icons.Outlined.PersonOutline)
        )

        primaryNavs.forEach { item ->
            val selected = currentScreen == item.screen
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
fun LabQuickNavBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    ScrollableTabRow(
        selectedTabIndex = when (currentScreen) {
            AppScreen.HOME -> 0
            AppScreen.VIDEOS -> 1
            AppScreen.AI_TUTOR -> 2
            AppScreen.CODING_LAB -> 3
            AppScreen.MATH_LAB -> 4
            AppScreen.WEB_DEV_LAB -> 5
            AppScreen.PROJECTS -> 6
            AppScreen.STUDY_DASHBOARD -> 7
            else -> 0
        },
        edgePadding = 12.dp,
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = modifier.fillMaxWidth()
    ) {
        val labs = listOf(
            LabItem(AppScreen.HOME, "Feed", Icons.Default.DynamicFeed),
            LabItem(AppScreen.VIDEOS, "Videos & Shorts", Icons.Default.OndemandVideo),
            LabItem(AppScreen.AI_TUTOR, "AI Tutor", Icons.Default.AutoAwesome),
            LabItem(AppScreen.CODING_LAB, "Coding Lab", Icons.Default.Terminal),
            LabItem(AppScreen.MATH_LAB, "Math Lab", Icons.Default.Calculate),
            LabItem(AppScreen.WEB_DEV_LAB, "Web Lab", Icons.Default.Web),
            LabItem(AppScreen.PROJECTS, "Projects Hub", Icons.Default.Workspaces),
            LabItem(AppScreen.STUDY_DASHBOARD, "Focus & Notes", Icons.Default.HourglassTop),
            LabItem(AppScreen.ADMIN, "⚡ Backend Console", Icons.Default.Dns)
        )

        labs.forEachIndexed { index, lab ->
            val isSelected = currentScreen == lab.screen
            Tab(
                selected = isSelected,
                onClick = { onNavigate(lab.screen) },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = lab.icon,
                            contentDescription = lab.name,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = lab.name,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    }
                }
            )
        }
    }
}

private data class NavItem(
    val screen: AppScreen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private data class LabItem(
    val screen: AppScreen,
    val name: String,
    val icon: ImageVector
)
