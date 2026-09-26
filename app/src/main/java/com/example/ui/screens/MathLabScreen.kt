package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.StudySphereViewModel
import com.example.ui.components.LabQuickNavBar
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MathLabScreen(
    viewModel: StudySphereViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Step-by-Step Solver, 1: Scientific Calculator, 2: 2D Graph Plotter
    val equationInput by viewModel.mathEquationInput.collectAsState()
    val topic by viewModel.mathTopic.collectAsState()
    val solutionSteps by viewModel.mathSolutionSteps.collectAsState()
    val isSolving by viewModel.isMathSolving.collectAsState()

    val topics = listOf("Algebra", "Calculus (Derivatives)", "Integrals", "Trigonometry", "Linear Equations", "Matrices")

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        // Quick Lab Navigation Bar
        LabQuickNavBar(
            currentScreen = AppScreen.MATH_LAB,
            onNavigate = { viewModel.navigateTo(it) }
        )

        // Subtabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("📐 Step Solver", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("🔢 Calculator", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("📈 Graph Plotter", fontWeight = FontWeight.Bold) }
            )
        }

        when (selectedTab) {
            0 -> StepSolverView(
                viewModel = viewModel,
                equationInput = equationInput,
                topic = topic,
                topics = topics,
                solutionSteps = solutionSteps,
                isSolving = isSolving
            )
            1 -> ScientificCalculatorView(viewModel = viewModel)
            2 -> FunctionGraphPlotterView()
        }
    }
}

@Composable
fun StepSolverView(
    viewModel: StudySphereViewModel,
    equationInput: String,
    topic: String,
    topics: List<String>,
    solutionSteps: String?,
    isSolving: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Step-by-Step AI Equation Solver 🧠",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        // Topic chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(topics) { t ->
                val isSelected = topic == t
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.mathTopic.value = t },
                    label = { Text(t, fontSize = 11.sp) }
                )
            }
        }

        // Equation Input Field
        OutlinedTextField(
            value = equationInput,
            onValueChange = { viewModel.mathEquationInput.value = it },
            label = { Text("Enter Math Equation or Problem") },
            placeholder = { Text("e.g. 3x^2 - 12 = 0 or d/dx(x * sin(x))") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("math_equation_input"),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            trailingIcon = {
                IconButton(
                    onClick = { viewModel.solveMath() },
                    enabled = equationInput.isNotBlank() && !isSolving
                ) {
                    if (isSolving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Solve", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        )

        // Quick Preset Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "2x + 5 = 15",
                "x^2 - 5x + 6 = 0",
                "d/dx(x^3 * cos(x))",
                "integral(3x^2 + 2x, x)",
                "sin^2(x) + cos^2(x)"
            ).forEach { preset ->
                FilledTonalButton(
                    onClick = {
                        viewModel.mathEquationInput.value = preset
                        viewModel.solveMath()
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(preset, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Solution Steps Card
        Card(
            shape = RoundedCornerShape(16.dp),
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
                        text = "Derivation & Solution Steps",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(10.dp))

                if (solutionSteps != null) {
                    Text(
                        text = solutionSteps,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 24.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Text(
                        text = """
                        **Given Equation:** 2x + 5 = 15
                        
                        1. **Subtract 5 from both sides:**
                           2x = 15 - 5
                           2x = 10
                        
                        2. **Divide both sides by 2:**
                           x = 10 / 2
                           x = 5
                        
                        3. **Verification:**
                           2(5) + 5 = 10 + 5 = 15 (Checked ✓)
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 22.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun ScientificCalculatorView(viewModel: StudySphereViewModel) {
    val display by viewModel.calcDisplay.collectAsState()
    val history by viewModel.calcHistory.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Display Screen
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(text = history, color = Color(0xFF94A3B8), fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                Text(
                    text = display,
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Keypad Grid
        val buttonRows = listOf(
            listOf("C", "DEL", "(", ")"),
            listOf("sin", "cos", "^", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "π", "=")
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            buttonRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { btn ->
                        val isOp = btn in listOf("÷", "×", "-", "+", "=", "^")
                        val isSpecial = btn in listOf("C", "DEL", "sin", "cos", "(", ")", "π")

                        Button(
                            onClick = { viewModel.onCalcButton(btn) },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    btn == "=" -> Color(0xFF10B981)
                                    isOp -> MaterialTheme.colorScheme.primary
                                    isSpecial -> MaterialTheme.colorScheme.surfaceVariant
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                contentColor = when {
                                    btn == "=" || isOp -> Color.White
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        ) {
                            Text(text = btn, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FunctionGraphPlotterView() {
    var selectedFunc by remember { mutableStateOf("sin(x)") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "2D Interactive Function Plotter 📈",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        // Select function
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("sin(x)", "cos(x)", "x^2 / 4", "x * sin(x)").forEach { fn ->
                val isSelected = selectedFunc == fn
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFunc = fn },
                    label = { Text("f(x) = $fn", fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
                )
            }
        }

        // Plot Canvas
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                val width = size.width
                val height = size.height
                val midX = width / 2
                val midY = height / 2

                // Draw Grid & Axes
                drawLine(
                    color = Color(0xFF334155),
                    start = Offset(0f, midY),
                    end = Offset(width, midY),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color(0xFF334155),
                    start = Offset(midX, 0f),
                    end = Offset(midX, height),
                    strokeWidth = 2f
                )

                // Draw Function Path
                val path = Path()
                val scaleX = 40f // pixels per unit
                val scaleY = 40f

                var isFirst = true
                for (px in 0..width.toInt()) {
                    val x = (px - midX) / scaleX
                    val y = when (selectedFunc) {
                        "sin(x)" -> sin(x)
                        "cos(x)" -> cos(x)
                        "x^2 / 4" -> (x * x) / 4.0
                        "x * sin(x)" -> x * sin(x)
                        else -> sin(x)
                    }
                    val py = midY - (y.toFloat() * scaleY)

                    if (isFirst) {
                        path.moveTo(px.toFloat(), py)
                        isFirst = false
                    } else {
                        path.lineTo(px.toFloat(), py)
                    }
                }

                drawPath(
                    path = path,
                    color = Color(0xFF38BDF8),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }

        // Coordinates & Equation summary
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Plotted Function: f(x) = $selectedFunc",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Domain: [-10, 10] • Continuous • Real Domain 📐",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
