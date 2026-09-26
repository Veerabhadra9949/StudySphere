package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
fun CodingLabScreen(
    viewModel: StudySphereViewModel,
    modifier: Modifier = Modifier
) {
    val selectedLanguage by viewModel.selectedCodeLanguage.collectAsState()
    val codeText by viewModel.codeEditorText.collectAsState()
    val codeOutput by viewModel.codeOutputText.collectAsState()
    val isRunning by viewModel.isCodeRunning.collectAsState()

    val languages = listOf("Python", "Java", "C++", "Dart", "SQL")

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        // Quick Lab Navigation Bar
        LabQuickNavBar(
            currentScreen = AppScreen.CODING_LAB,
            onNavigate = { viewModel.navigateTo(it) }
        )

        // Top Toolbar: Language Selector & Run Action
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Language Dropdown / Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    languages.forEach { lang ->
                        val isSelected = selectedLanguage == lang
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.selectedCodeLanguage.value = lang
                                viewModel.codeEditorText.value = getStarterTemplateFor(lang)
                            },
                            label = { Text(lang, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                // Run Code Button
                Button(
                    onClick = { viewModel.runCodeExecution() },
                    enabled = !isRunning,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("run_code_button")
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Run", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Code Editor View Area
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxWidth()
                .background(Color(0xFF0B0F19))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "main.${getExtension(selectedLanguage)}",
                    color = Color(0xFF94A3B8),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
                Text(
                    text = "UTF-8 • Sandbox v2.4",
                    color = Color(0xFF64748B),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            // Code Text Input
            OutlinedTextField(
                value = codeText,
                onValueChange = { viewModel.codeEditorText.value = it },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("code_editor_field"),
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

        // AI Assistant Action Strip
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI Copilot:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                AssistChip(
                    onClick = { viewModel.requestAiCodeReview("optimize") },
                    label = { Text("⚡ Optimize (O(N))", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )
                AssistChip(
                    onClick = { viewModel.requestAiCodeReview("debug") },
                    label = { Text("🐞 Find Bugs", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )
                AssistChip(
                    onClick = { viewModel.requestAiCodeReview("complexity") },
                    label = { Text("📊 Big-O Analysis", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.QueryStats, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )
                AssistChip(
                    onClick = { viewModel.requestAiCodeReview("testcases") },
                    label = { Text("🧪 Edge Test Cases", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )
            }
        }

        // Terminal Console Output
        Column(
            modifier = Modifier
                .weight(0.9f)
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Terminal",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "TERMINAL CONSOLE",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(
                    onClick = { viewModel.codeOutputText.value = "Console cleared." },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Divider(color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = codeOutput,
                    color = Color(0xFF4ADE80),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

private fun getExtension(lang: String): String = when (lang) {
    "Python" -> "py"
    "Java" -> "java"
    "C++" -> "cpp"
    "Dart" -> "dart"
    "SQL" -> "sql"
    else -> "txt"
}

private fun getStarterTemplateFor(lang: String): String = when (lang) {
    "Python" -> """# Python 3 Data Structure & Algorithm
def reverse_words(s: str) -> str:
    words = s.strip().split()
    return " ".join(reversed(words))

msg = "StudySphere AI Super App"
print(f"Original: {msg}")
print(f"Reversed: {reverse_words(msg)}")
"""
    "Java" -> """// Java 21 LTS
import java.util.*;

public class Solution {
    public static void main(String[] args) {
        System.out.println("Executing Java in StudySphere Sandbox...");
        int[] data = {5, 2, 9, 1, 5, 6};
        Arrays.sort(data);
        System.out.println("Sorted Array: " + Arrays.toString(data));
    }
}
"""
    "C++" -> """// C++20 Standard
#include <iostream>
#include <vector>
#include <numeric>

int main() {
    std::vector<int> nums = {10, 20, 30, 40, 50};
    int total = std::accumulate(nums.begin(), nums.end(), 0);
    std::cout << "Sum of elements: " << total << std::endl;
    return 0;
}
"""
    "Dart" -> """// Dart SDK
void main() {
  var student = {"name": "Alex", "streak": 14, "xp": 1850};
  print("Student Status: " + student.toString());
}
"""
    "SQL" -> """-- PostgreSQL 16
SELECT user_id, username, xp, streak_days 
FROM users 
WHERE xp > 1000 
ORDER BY xp DESC 
LIMIT 5;
"""
    else -> "// Code here"
}
