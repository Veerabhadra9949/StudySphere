package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AchievementEntity
import com.example.data.local.ProjectEntity
import com.example.ui.AppScreen
import com.example.ui.StudySphereViewModel
import com.example.ui.components.PostCard

@Composable
fun ProfileScreen(
    viewModel: StudySphereViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()
    val authUser by viewModel.authUser.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val authErrorMessage by viewModel.authErrorMessage.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val achievements by viewModel.achievements.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Posts, 1: Projects, 2: Badges

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var isPrivateAccount by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Action Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "@${user?.username ?: "alex_dev"}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (authUser != null) "🟢 Connected to Firebase" else "🟡 Offline / Guest Mode",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (authUser != null) Color(0xFF10B981) else MaterialTheme.colorScheme.tertiary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (authUser == null) {
                        FilledTonalButton(
                            onClick = { showAuthDialog = true },
                            modifier = Modifier.testTag("sign_in_button")
                        ) {
                            Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sign In", fontSize = 12.sp)
                        }
                    } else {
                        IconButton(onClick = { viewModel.signOut() }) {
                            Icon(imageVector = Icons.Default.Logout, contentDescription = "Sign Out", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            }
        }

        // Profile Header Info
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (user?.fullName ?: "Alex").take(2).uppercase(),
                                color = Color.White,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatColumn(label = "Posts", value = "${posts.size}")
                            StatColumn(label = "Followers", value = "${user?.followersCount ?: 428}")
                            StatColumn(label = "Following", value = "${user?.followingCount ?: 192}")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = user?.fullName ?: "Alex Rivera",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = user?.education ?: "Computer Science & Engineering",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = user?.bio ?: "CS Undergrad @ MIT | Exploring AI/ML & Jetpack Compose 🚀",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // XP & Streak Level Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Stars, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Level ${user?.level ?: 6} Scholar",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "${user?.xp ?: 1850} XP Earned",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEF3C7)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "${user?.streakDays ?: 14}d Streak", color = Color(0xFF92400E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Profile Tabs
        item {
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(selected = activeTab == 0, onClick = { activeTab = 0 }, text = { Text("Posts (${posts.size})", fontSize = 12.sp) })
                Tab(selected = activeTab == 1, onClick = { activeTab = 1 }, text = { Text("Projects (${projects.size})", fontSize = 12.sp) })
                Tab(selected = activeTab == 2, onClick = { activeTab = 2 }, text = { Text("Badges (${achievements.size})", fontSize = 12.sp) })
            }
        }

        if (activeTab == 0) {
            items(posts, key = { it.id }) { post ->
                PostCard(
                    post = post,
                    onLikeClick = { viewModel.toggleLike(post) },
                    onCommentClick = { viewModel.activePostForComments.value = post },
                    onBookmarkClick = { viewModel.toggleBookmark(post) },
                    onShareClick = { }
                )
            }
        } else if (activeTab == 1) {
            items(projects, key = { it.id }) { project ->
                ProjectCard(
                    project = project,
                    onProgressChange = { viewModel.updateProjectProgress(project, it) },
                    onDelete = { viewModel.deleteProject(project.id) }
                )
            }
        } else {
            items(achievements, key = { it.id }) { ach ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (ach.isUnlocked) Color(0xFF10B981).copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (ach.isUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (ach.isUnlocked) Color(0xFF10B981) else Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = ach.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = ach.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "+${ach.xpReward} XP",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Authentication Dialog
    if (showAuthDialog) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isSignUpMode by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAuthDialog = false },
            title = { Text(if (isSignUpMode) "Create Student Account 🎓" else "Student Sign In 🔐") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Sign in to sync your study progress, posts, coding projects, and streak across devices with Firebase.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            viewModel.signInWithGoogle()
                            showAuthDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign in with Google")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text(" or ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (authErrorMessage != null) {
                        Text(text = authErrorMessage ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    TextButton(onClick = { isSignUpMode = !isSignUpMode }) {
                        Text(if (isSignUpMode) "Already have an account? Sign In" else "Need an account? Sign Up")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isSignUpMode) {
                            viewModel.signUpWithEmail(email, password, onSuccess = { showAuthDialog = false })
                        } else {
                            viewModel.signInWithEmail(email, password, onSuccess = { showAuthDialog = false })
                        }
                    }
                ) {
                    Text(if (isSignUpMode) "Sign Up" else "Sign In")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAuthDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Settings Modal
    if (showSettingsDialog) {
        val themeMode by viewModel.themeMode.collectAsState()
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Settings & Preferences")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Night Study / Dark Mode section
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (themeMode == "dark") Icons.Filled.DarkMode else Icons.Filled.LightMode,
                                        contentDescription = null,
                                        tint = if (themeMode == "dark") Color(0xFFFBBF24) else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            "Night Study Theme 🌙",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            "Low-glare palette for night reading",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Switch(
                                    checked = themeMode == "dark",
                                    onCheckedChange = { isDark ->
                                        viewModel.setThemeMode(if (isDark) "dark" else "light")
                                    },
                                    modifier = Modifier.testTag("dark_mode_switch")
                                )
                            }

                            // Theme Selector Chips (Day, Night, Auto)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = themeMode == "light",
                                    onClick = { viewModel.setThemeMode("light") },
                                    label = { Text("☀️ Day", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = themeMode == "dark",
                                    onClick = { viewModel.setThemeMode("dark") },
                                    label = { Text("🌙 Night", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = themeMode == "system",
                                    onClick = { viewModel.setThemeMode("system") },
                                    label = { Text("⚙️ Auto", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Private Student Profile")
                        Switch(checked = isPrivateAccount, onCheckedChange = { isPrivateAccount = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Study Notifications & Reminders")
                        Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
                    }
                    HorizontalDivider()
                    TextButton(onClick = {
                        showSettingsDialog = false
                        viewModel.navigateTo(AppScreen.ADMIN)
                    }) {
                        Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Moderation & Admin Panel")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showSettingsDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
