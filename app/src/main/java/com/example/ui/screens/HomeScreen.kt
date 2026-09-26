package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PostEntity
import com.example.data.local.StoryEntity
import com.example.ui.AppScreen
import com.example.ui.StudySphereViewModel
import com.example.ui.components.*

@Composable
fun HomeScreen(
    viewModel: StudySphereViewModel,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.posts.collectAsState()
    val stories by viewModel.stories.collectAsState()
    val selectedFilter by viewModel.selectedSubjectFilter.collectAsState()
    val activeStory by viewModel.activeStory.collectAsState()
    val activePostForComments by viewModel.activePostForComments.collectAsState()
    val comments by viewModel.commentsForActivePost.collectAsState()

    var showAddStoryDialog by remember { mutableStateOf(false) }
    var storyCaptionInput by remember { mutableStateOf("") }
    var storySubjectInput by remember { mutableStateOf("DSA") }

    val subjects = listOf("All", "AI & Machine Learning", "Data Structures & Algos", "Mathematics", "App Development", "Web Dev", "Physics")

    val filteredPosts = if (selectedFilter == "All") {
        posts
    } else {
        posts.filter { it.subjectTag.contains(selectedFilter, ignoreCase = true) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Lab Quick Navigation Bar
            item {
                LabQuickNavBar(
                    currentScreen = AppScreen.HOME,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }

            // Stories Row
            item {
                StoriesRow(
                    stories = stories,
                    onAddStoryClick = { showAddStoryDialog = true },
                    onStoryClick = { viewModel.activeStory.value = it }
                )
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            }

            // Backend & Cloud Console Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { viewModel.navigateTo(AppScreen.ADMIN) }
                        .testTag("backend_console_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudQueue,
                                    contentDescription = "Backend",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Backend & Cloud Console",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Text(
                                    text = "Firebase Firestore • Gemini AI • Room DB (Online)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open Backend Console",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Quick Create Post Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { viewModel.navigateTo(AppScreen.CREATE) }
                        .testTag("quick_create_post_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("AR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Share a code snippet, math proof, or study tip...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Attach media",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Subject Filter Chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(subjects) { subject ->
                        val isSelected = selectedFilter == subject
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectedSubjectFilter.value = subject },
                            label = { Text(subject, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // Feed Posts
            if (filteredPosts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No posts found for '$selectedFilter'",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredPosts, key = { it.id }) { post ->
                    PostCard(
                        post = post,
                        onLikeClick = { viewModel.toggleLike(post) },
                        onCommentClick = { viewModel.activePostForComments.value = post },
                        onBookmarkClick = { viewModel.toggleBookmark(post) },
                        onShareClick = { /* Share link */ }
                    )
                }
            }
        }

        // Story Viewer Dialog
        activeStory?.let { story ->
            StoryViewerDialog(
                story = story,
                onDismiss = { viewModel.activeStory.value = null },
                onReply = { reply ->
                    viewModel.sendChat(reply)
                }
            )
        }

        // Comments Bottom Sheet
        activePostForComments?.let { post ->
            CommentsSheetDialog(
                post = post,
                comments = comments,
                onDismiss = { viewModel.activePostForComments.value = null },
                onSendComment = { text ->
                    viewModel.sendComment(post.id, text)
                }
            )
        }

        // Add Story Dialog
        if (showAddStoryDialog) {
            AlertDialog(
                onDismissRequest = { showAddStoryDialog = false },
                title = { Text("Post a Study Story 📸") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Stories disappear automatically. Share your daily revision goal or coding victory!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = storyCaptionInput,
                            onValueChange = { storyCaptionInput = it },
                            label = { Text("What did you learn today?") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = storySubjectInput,
                            onValueChange = { storySubjectInput = it },
                            label = { Text("Subject (e.g. AI, Math, DSA)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (storyCaptionInput.isNotBlank()) {
                                viewModel.createNewStory(storyCaptionInput, storySubjectInput)
                                storyCaptionInput = ""
                                showAddStoryDialog = false
                            }
                        }
                    ) {
                        Text("Publish Story")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddStoryDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
