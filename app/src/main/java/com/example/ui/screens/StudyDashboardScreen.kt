package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.NoteEntity
import com.example.ui.AppScreen
import com.example.ui.StudySphereViewModel
import com.example.ui.components.LabQuickNavBar

@Composable
fun StudyDashboardScreen(
    viewModel: StudySphereViewModel,
    modifier: Modifier = Modifier
) {
    var subTab by remember { mutableStateOf(0) } // 0: Focus Timer, 1: AI Study Planner, 2: Smart Notes

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        // Quick Lab Nav
        LabQuickNavBar(
            currentScreen = AppScreen.STUDY_DASHBOARD,
            onNavigate = { viewModel.navigateTo(it) }
        )

        // Subtabs (Pomodoro, AI Planner, Notes)
        TabRow(
            selectedTabIndex = subTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = subTab == 0,
                onClick = { subTab = 0 },
                text = { Text("⏱️ Focus Timer", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = subTab == 1,
                onClick = { subTab = 1 },
                text = { Text("📅 AI Planner", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = subTab == 2,
                onClick = { subTab = 2 },
                text = { Text("📝 Smart Notes", fontWeight = FontWeight.Bold) }
            )
        }

        when (subTab) {
            0 -> PomodoroFocusView(viewModel = viewModel)
            1 -> AiStudyPlannerView(viewModel = viewModel)
            2 -> SmartNotesView(viewModel = viewModel)
        }
    }
}

@Composable
fun PomodoroFocusView(viewModel: StudySphereViewModel) {
    val minutes by viewModel.pomodoroMinutes.collectAsState()
    val seconds by viewModel.pomodoroSeconds.collectAsState()
    val isRunning by viewModel.isTimerRunning.collectAsState()
    val mode by viewModel.currentTimerMode.collectAsState()
    val subject by viewModel.selectedSubjectForTimer.collectAsState()

    val totalSeconds = if (mode.contains("25")) 25 * 60 else 5 * 60
    val currentRemaining = minutes * 60 + seconds
    val progress = 1f - (currentRemaining.toFloat() / totalSeconds.toFloat())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Mode & Subject Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = mode,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Subject: $subject",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = { viewModel.resetPomodoro(25, "Focus (25m)") },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("25m", fontSize = 11.sp)
                    }
                    FilledTonalButton(
                        onClick = { viewModel.resetPomodoro(5, "Short Break (5m)") },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("5m", fontSize = 11.sp)
                    }

                    val themeMode by viewModel.themeMode.collectAsState()
                    IconButton(
                        onClick = {
                            val next = if (themeMode == "dark") "light" else "dark"
                            viewModel.setThemeMode(next)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("pomodoro_theme_toggle")
                    ) {
                        Icon(
                            imageVector = if (themeMode == "dark") Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "Night Study Mode Toggle",
                            tint = if (themeMode == "dark") Color(0xFFFBBF24) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Circular Timer Display
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 10.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isRunning) "Deep Focus in Progress 🧘" else "Paused",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Control Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.togglePomodoroTimer() },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .height(56.dp)
                    .width(180.dp)
                    .testTag("pomodoro_toggle_button")
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRunning) "Pause Focus" else "Start Focus",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(
                onClick = { viewModel.resetPomodoro(25, "Focus (25m)") },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset")
            }
        }
    }
}

@Composable
fun AiStudyPlannerView(viewModel: StudySphereViewModel) {
    val subject by viewModel.plannerSubject.collectAsState()
    val examDate by viewModel.plannerExamDate.collectAsState()
    val hoursPerDay by viewModel.plannerHoursPerDay.collectAsState()
    val targetScore by viewModel.plannerTargetScore.collectAsState()
    val plan by viewModel.generatedStudyPlan.collectAsState()
    val isGenerating by viewModel.isGeneratingPlan.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "AI Personalized Study Schedule Generator 🤖",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            OutlinedTextField(
                value = subject,
                onValueChange = { viewModel.plannerSubject.value = it },
                label = { Text("Target Exam / Subject") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = examDate,
                    onValueChange = { viewModel.plannerExamDate.value = it },
                    label = { Text("Time Remaining") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = targetScore,
                    onValueChange = { viewModel.plannerTargetScore.value = it },
                    label = { Text("Target Score") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Button(
                onClick = { viewModel.generateStudyPlan() },
                enabled = !isGenerating,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generating Schedule with Gemini AI...")
                } else {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Generate Optimal Plan")
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📅 Structured Milestone Roadmap",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = plan ?: """
                        ### 🗓️ Recommended 4-Week Schedule for $subject
                        
                        - **Week 1 (Foundations):** Asymptotic Notation, Arrays, Two-Pointers, HashMaps (3 hrs/day)
                        - **Week 2 (Non-Linear Structures):** Trees, BST, Heaps, Graph BFS/DFS
                        - **Week 3 (Algorithms):** Dynamic Programming (0/1 Knapsack, LCS), Greedy Methods
                        - **Week 4 (Mock Exams & Timed Practice):** 5 full mock assessments, active recall flashcards.
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun SmartNotesView(viewModel: StudySphereViewModel) {
    val notes by viewModel.notes.collectAsState()
    var showCreateNoteDialog by remember { mutableStateOf(false) }
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var noteSubject by remember { mutableStateOf("DSA") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Smart Notes & Summaries 📓",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Button(
                    onClick = { showCreateNoteDialog = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Note")
                }
            }
        }

        items(notes, key = { it.id }) { note ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = note.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { viewModel.deleteNote(note.id) }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalButton(
                            onClick = { viewModel.askAiTutor("Summarize this note and create 3 quiz questions:\n${note.content}") },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI Quiz / Summary", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    if (showCreateNoteDialog) {
        AlertDialog(
            onDismissRequest = { showCreateNoteDialog = false },
            title = { Text("Add Smart Study Note ✍️") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("Note Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text("Note Content") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                    OutlinedTextField(
                        value = noteSubject,
                        onValueChange = { noteSubject = it },
                        label = { Text("Subject / Tags") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteTitle.isNotBlank()) {
                            viewModel.saveNote(noteTitle, noteContent, noteSubject, noteSubject)
                            noteTitle = ""
                            noteContent = ""
                            showCreateNoteDialog = false
                        }
                    }
                ) {
                    Text("Save Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateNoteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
