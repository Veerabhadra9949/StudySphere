package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.ui.AppScreen
import com.example.ui.StudySphereViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    viewModel: StudySphereViewModel,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("Data Structures & Algos") }
    var selectedMediaType by remember { mutableStateOf("code") } // "text", "code", "math", "photo"
    var codeSnippet by remember {
        mutableStateOf(
            """// Quick algorithm tip
fun binarySearch(arr: IntArray, target: Int): Int {
    var low = 0; var high = arr.size - 1
    while (low <= high) {
        val mid = low + (high - low) / 2
        if (arr[mid] == target) return mid
        if (arr[mid] < target) low = mid + 1 else high = mid - 1
    }
    return -1
}"""
        )
    }
    var codeLanguage by remember { mutableStateOf("Kotlin") }
    var selectedAudioTrack by remember { mutableStateOf("Lo-Fi Study Beats (Royalty Free)") }
    var audience by remember { mutableStateOf("Public (All Students)") }
    var isCameraActive by remember { mutableStateOf(false) }

    val subjects = listOf("Data Structures & Algos", "AI & Machine Learning", "Mathematics", "App Development", "Web Dev", "Physics", "General")
    val languages = listOf("Kotlin", "Python", "Java", "C++", "JavaScript", "SQL", "Dart")
    val audioTracks = listOf("Lo-Fi Study Beats (Royalty Free)", "Ambient Synth Deep Focus", "Classical Baroque Flow", "None")

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateTo(AppScreen.HOME) }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Create Academic Post ✍️",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        viewModel.createNewPost(
                            title = title,
                            content = content,
                            subject = selectedSubject,
                            codeSnippet = if (selectedMediaType == "code" || selectedMediaType == "math") codeSnippet else "",
                            codeLanguage = if (selectedMediaType == "code") codeLanguage else "Math",
                            mediaType = selectedMediaType
                        )
                    }
                },
                shape = RoundedCornerShape(20.dp),
                enabled = title.isNotBlank() && content.isNotBlank(),
                modifier = Modifier.testTag("publish_post_button")
            ) {
                Icon(imageVector = Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Publish", fontWeight = FontWeight.Bold)
            }
        }

        // Post Format Selector (Text, Code, Math, Photo/Camera)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                FormatOption("code", "Code Snippet", Icons.Default.Code),
                FormatOption("text", "Discussion", Icons.Default.Article),
                FormatOption("math", "Math Formula", Icons.Default.Calculate),
                FormatOption("photo", "Camera / Media", Icons.Default.CameraAlt)
            ).forEach { opt ->
                val isSelected = selectedMediaType == opt.id
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            selectedMediaType = opt.id
                            if (opt.id == "photo") isCameraActive = true
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = opt.icon,
                            contentDescription = opt.label,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = opt.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // In-App Camera Simulation if selected
        if (selectedMediaType == "photo" || isCameraActive) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Camera Stream",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Camera Active (Front / Rear Live Preview)",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FilledTonalButton(onClick = { /* Capture */ }) {
                                Icon(imageVector = Icons.Default.Camera, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Snap Photo")
                            }
                            FilledTonalButton(onClick = { /* Pick gallery */ }) {
                                Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gallery")
                            }
                        }
                    }
                }
            }
        }

        // Title Input
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Post Title or Problem Summary") },
            placeholder = { Text("e.g. How to solve Dijkstra's Algorithm in Kotlin") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("create_post_title_input"),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        // Subject Tag Selection
        Text(
            text = "Select Academic Discipline:",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(subjects) { subj ->
                val isSelected = selectedSubject == subj
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedSubject = subj },
                    label = { Text(subj, fontSize = 12.sp) }
                )
            }
        }

        // Content Explanation Text
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Detailed Explanation / Notes / Solution Walkthrough") },
            placeholder = { Text("Describe the concept clearly for fellow students...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .testTag("create_post_content_input"),
            shape = RoundedCornerShape(14.dp)
        )

        // Code Snippet or Math Input if active
        if (selectedMediaType == "code" || selectedMediaType == "math") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedMediaType == "code") "Interactive Code Snippet:" else "Mathematical Formula:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (selectedMediaType == "code") {
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text("Lang: $codeLanguage")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            languages.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang) },
                                    onClick = {
                                        codeLanguage = lang
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0F172A),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = codeSnippet,
                    onValueChange = { codeSnippet = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF38BDF8),
                        unfocusedTextColor = Color(0xFF38BDF8),
                        focusedBorderColor = Color(0xFF334155),
                        unfocusedBorderColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                )
            }
        }

        // Background Audio / Focus Track Attachment
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Audio track",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Licensed Audio Background",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = selectedAudioTrack,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Audience Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Public, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Audience: $audience", style = MaterialTheme.typography.bodyMedium)
            }
            TextButton(onClick = { /* Change privacy */ }) {
                Text("Change")
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

private data class FormatOption(val id: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
