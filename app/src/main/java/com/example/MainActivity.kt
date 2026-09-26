package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.repository.StudySphereRepository
import com.example.ui.AppScreen
import com.example.ui.StudySphereViewModel
import com.example.ui.StudySphereViewModelFactory
import com.example.ui.components.StudySphereBottomNav
import com.example.ui.components.StudySphereTopBar
import com.example.ui.screens.*
import com.example.ui.theme.StudySphereTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            com.example.data.firebase.FirebaseManager.init(context)
            com.example.data.firebase.StudySphereMessagingService.createNotificationChannels(context)
            val database = AppDatabase.getDatabase(context)
            val repository = remember { StudySphereRepository(database, context) }
            val viewModel: StudySphereViewModel = viewModel(
                factory = StudySphereViewModelFactory(repository)
            )

            val themeMode by viewModel.themeMode.collectAsState()
            val systemInDark = isSystemInDarkTheme()
            val isDarkMode = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> systemInDark
            }

            StudySphereTheme(darkTheme = isDarkMode) {
                StudySphereApp(
                    viewModel = viewModel,
                    isDarkMode = isDarkMode,
                    onToggleTheme = { viewModel.toggleTheme(isDarkMode) }
                )
            }
        }
    }
}

@Composable
fun StudySphereApp(
    viewModel: StudySphereViewModel,
    isDarkMode: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        viewModel.navigateTo(AppScreen.HOME)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (currentScreen != AppScreen.CHAT && currentScreen != AppScreen.ADMIN) {
                StudySphereTopBar(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) },
                    userXp = user?.xp ?: 1850,
                    userStreak = user?.streakDays ?: 14,
                    notifications = notifications,
                    onMarkNotificationRead = { viewModel.markNotificationAsRead(it) },
                    isDarkMode = isDarkMode,
                    onToggleTheme = onToggleTheme
                )
            }
        },
        bottomBar = {
            if (currentScreen != AppScreen.CHAT && currentScreen != AppScreen.ADMIN) {
                StudySphereBottomNav(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentScreen, label = "screen_crossfade") { screen ->
                when (screen) {
                    AppScreen.HOME -> HomeScreen(viewModel = viewModel)
                    AppScreen.EXPLORE -> ExploreScreen(viewModel = viewModel)
                    AppScreen.CREATE -> CreatePostScreen(viewModel = viewModel)
                    AppScreen.VIDEOS -> VideoPlatformScreen(viewModel = viewModel)
                    AppScreen.AI_TUTOR -> AiTutorScreen(viewModel = viewModel)
                    AppScreen.CODING_LAB -> CodingLabScreen(viewModel = viewModel)
                    AppScreen.MATH_LAB -> MathLabScreen(viewModel = viewModel)
                    AppScreen.WEB_DEV_LAB -> WebDevLabScreen(viewModel = viewModel)
                    AppScreen.PROJECTS -> ProjectWorkspaceScreen(viewModel = viewModel)
                    AppScreen.STUDY_DASHBOARD -> StudyDashboardScreen(viewModel = viewModel)
                    AppScreen.CHAT -> ChatScreen(viewModel = viewModel)
                    AppScreen.PROFILE -> ProfileScreen(viewModel = viewModel)
                    AppScreen.ADMIN -> AdminScreen(viewModel = viewModel)
                }
            }
        }
    }
}
