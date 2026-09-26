package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.ui.AppScreen
import com.example.ui.StudySphereViewModel
import com.example.ui.components.LabQuickNavBar

@Composable
fun WebDevLabScreen(
    viewModel: StudySphereViewModel,
    modifier: Modifier = Modifier
) {
    var editorMode by remember { mutableStateOf(0) } // 0: HTML, 1: CSS, 2: JS, 3: Live Preview
    val htmlCode by viewModel.webHtmlCode.collectAsState()
    val cssCode by viewModel.webCssCode.collectAsState()
    val jsCode by viewModel.webJsCode.collectAsState()
    val activeTemplateIndex by viewModel.activeWebProjectIndex.collectAsState()
    val consoleLogs by viewModel.webConsoleLogs.collectAsState()

    var simulatedButtonTriggered by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        // Quick Lab Nav
        LabQuickNavBar(
            currentScreen = AppScreen.WEB_DEV_LAB,
            onNavigate = { viewModel.navigateTo(it) }
        )

        // Template Selector Strip
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(viewModel.webProjects) { index, proj ->
                        val isSelected = activeTemplateIndex == index
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectWebProject(index) },
                            label = { Text(proj.title, fontSize = 11.sp) }
                        )
                    }
                }

                Button(
                    onClick = { viewModel.runWebCode() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Recompile", fontSize = 12.sp)
                }
            }
        }

        // Code Editor Tabs (HTML / CSS / JS / Preview)
        TabRow(
            selectedTabIndex = editorMode,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = editorMode == 0,
                onClick = { editorMode = 0 },
                text = { Text("📄 index.html", fontFamily = FontFamily.Monospace, fontSize = 12.sp) }
            )
            Tab(
                selected = editorMode == 1,
                onClick = { editorMode = 1 },
                text = { Text("🎨 style.css", fontFamily = FontFamily.Monospace, fontSize = 12.sp) }
            )
            Tab(
                selected = editorMode == 2,
                onClick = { editorMode = 2 },
                text = { Text("⚡ script.js", fontFamily = FontFamily.Monospace, fontSize = 12.sp) }
            )
            Tab(
                selected = editorMode == 3,
                onClick = { editorMode = 3 },
                text = { Text("🌐 Live Preview", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        if (editorMode == 3) {
            // Live Preview Canvas
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Browser URL Bar Mockup
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFEF4444)))
                            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFF59E0B)))
                            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFF10B981)))
                        }
                        Text(
                            text = "https://studysphere.localhost:8080/app",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Rendered Web Page Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = if (activeTemplateIndex == 0) "🎓 StudySphere Student Portal" else "Hi, I'm Alex 👋",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF818CF8)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (activeTemplateIndex == 0) "Track your GPA, active assignments, and code submissions." else "CS Student & Open-Source Contributor",
                            color = Color(0xFFE2E8F0),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (activeTemplateIndex == 0) {
                            Button(
                                onClick = { simulatedButtonTriggered = !simulatedButtonTriggered },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Calculate Study Streak", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (simulatedButtonTriggered) Color(0xFF065F46) else Color(0xFF312E81)
                            ) {
                                Text(
                                    text = if (simulatedButtonTriggered) "🎉 Boosted! New Streak: 15 Days!" else "Streak: 14 Days 🔥",
                                    color = if (simulatedButtonTriggered) Color(0xFF6EE7B7) else Color(0xFFC7D2FE),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Kotlin", "Python", "React", "PyTorch").forEach { tag ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.White.copy(alpha = 0.15f)
                                    ) {
                                        Text(text = tag, color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Console output
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "> DEV CONSOLE", color = Color(0xFF38BDF8), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        consoleLogs.forEach { log ->
                            Text(text = log, color = Color(0xFF4ADE80), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        } else {
            // Source Code Editor View
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF0B0F19))
                    .padding(12.dp)
            ) {
                val currentText = when (editorMode) {
                    0 -> htmlCode
                    1 -> cssCode
                    else -> jsCode
                }

                OutlinedTextField(
                    value = currentText,
                    onValueChange = {
                        when (editorMode) {
                            0 -> viewModel.webHtmlCode.value = it
                            1 -> viewModel.webCssCode.value = it
                            2 -> viewModel.webJsCode.value = it
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("web_code_editor"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFFE2E8F0),
                        unfocusedTextColor = Color(0xFFE2E8F0),
                        focusedBorderColor = Color(0xFF334155),
                        unfocusedBorderColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}
