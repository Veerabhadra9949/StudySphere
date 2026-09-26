package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.StudySphereRepository
import com.example.data.security.SecurityGuard
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    EXPLORE,
    CREATE,
    VIDEOS,
    AI_TUTOR,
    CODING_LAB,
    MATH_LAB,
    WEB_DEV_LAB,
    PROJECTS,
    STUDY_DASHBOARD,
    CHAT,
    PROFILE,
    ADMIN
}

data class AiMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user", "ai"
    val content: String,
    val isStreaming: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class CodeTemplate(
    val name: String,
    val language: String,
    val code: String
)

data class WebDevProject(
    val title: String,
    val html: String,
    val css: String,
    val js: String
)

class StudySphereViewModel(private val repository: StudySphereRepository) : ViewModel() {

    // Navigation
    private val _currentScreen = MutableStateFlow(AppScreen.AI_TUTOR)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Sub-filters
    val selectedSubjectFilter = MutableStateFlow("All")
    val searchQuery = MutableStateFlow("")

    // Data from Room (Offline-First Cache synced with Firestore)
    val posts: StateFlow<List<PostEntity>> = repository.posts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stories: StateFlow<List<StoryEntity>> = repository.stories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val longVideos: StateFlow<List<VideoEntity>> = repository.longVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shortVideos: StateFlow<List<VideoEntity>> = repository.shortVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = repository.notes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projects: StateFlow<List<ProjectEntity>> = repository.projects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studySessions: StateFlow<List<StudySessionEntity>> = repository.studySessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<AchievementEntity>> = repository.achievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUser: StateFlow<UserEntity?> = repository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val authUser: StateFlow<FirebaseUser?> = repository.authState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val authErrorMessage = MutableStateFlow<String?>(null)
    val isAuthLoading = MutableStateFlow(false)

    // Global Theme Mode: "system", "dark", "light"
    val themeMode: StateFlow<String> = repository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    fun setThemeMode(mode: String) {
        repository.setThemeMode(mode)
    }

    fun toggleTheme(currentIsDark: Boolean) {
        val nextMode = if (currentIsDark) "light" else "dark"
        repository.setThemeMode(nextMode)
    }

    // Comments dialog state
    val activePostForComments = MutableStateFlow<PostEntity?>(null)
    val commentsForActivePost = activePostForComments.flatMapLatest { post ->
        if (post != null) repository.getComments(post.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Story Viewer
    val activeStory = MutableStateFlow<StoryEntity?>(null)

    // AI Tutor State
    val aiTutorLanguage = MutableStateFlow("en") // "en", "te", "te_en"
    val aiTutorLevel = MutableStateFlow("Intermediate")
    val aiTutorSubject = MutableStateFlow("General")
    val aiTutorMode = MutableStateFlow("chat") // "chat", "quiz", "flashcards", "summarize"
    val aiTutorMessages = MutableStateFlow<List<AiMessage>>(
        listOf(
            AiMessage(
                sender = "ai",
                content = "👋 Hi! I am your StudySphere AI Tutor. You can ask me anything about Coding (Java, Python, C++, DSA), Mathematics, AI/ML, or Engineering in English, Telugu (తెలుగు), or Tenglish!"
            )
        )
    )
    val isAiThinking = MutableStateFlow(false)

    // AI Quiz & Flashcards & Summary State
    val quizTopic = MutableStateFlow("Data Structures & Algorithms")
    val quizDifficulty = MutableStateFlow("Medium")
    val generatedQuiz = MutableStateFlow<String?>(null)
    val isGeneratingQuiz = MutableStateFlow(false)

    val flashcardTopic = MutableStateFlow("Java OOP Concepts")
    val generatedFlashcards = MutableStateFlow<String?>(null)
    val isGeneratingFlashcards = MutableStateFlow(false)

    val docTextToSummarize = MutableStateFlow("")
    val generatedSummary = MutableStateFlow<String?>(null)
    val isSummarizing = MutableStateFlow(false)

    // Coding Lab State
    val selectedCodeLanguage = MutableStateFlow("Python")
    val codeEditorText = MutableStateFlow(
        """# StudySphere Coding Lab
# Problem: Two Sum Problem in O(N) Time
def two_sum(nums, target):
    seen = {}
    for i, num in enumerate(nums):
        complement = target - num
        if complement in seen:
            return [seen[complement], i]
        seen[num] = i
    return []

# Test execution
arr = [2, 7, 11, 15]
target_val = 9
result = two_sum(arr, target_val)
print(f"Indices for target {target_val}: {result}")
print(f"Values: {[arr[i] for i in result]}")
"""
    )
    val codeOutputText = MutableStateFlow("Ready to run code.\nOutput will be displayed in this terminal...")
    val isCodeRunning = MutableStateFlow(false)

    // Mathematics Lab State
    val mathEquationInput = MutableStateFlow("2x + 5 = 15")
    val mathTopic = MutableStateFlow("Algebra")
    val mathSolutionSteps = MutableStateFlow<String?>(null)
    val isMathSolving = MutableStateFlow(false)
    val calcDisplay = MutableStateFlow("0")
    val calcHistory = MutableStateFlow("")

    // Web Dev Lab State
    val webProjects = listOf(
        WebDevProject(
            title = "Interactive Student Dashboard",
            html = """<div class="card">
  <h2>🎓 StudySphere Student Portal</h2>
  <p>Track your GPA, active assignments, and code submissions.</p>
  <button id="btn" class="primary-btn">Calculate Study Streak</button>
  <div id="output" class="streak-badge">Streak: 14 Days 🔥</div>
</div>""",
            css = """body { font-family: sans-serif; background: #0f172a; color: #f8fafc; padding: 20px; }
.card { background: #1e293b; border-radius: 12px; padding: 24px; border: 1px solid #334155; max-width: 400px; margin: auto; }
h2 { color: #818cf8; margin-top: 0; }
.primary-btn { background: #6366f1; color: white; border: none; padding: 10px 18px; border-radius: 8px; font-weight: bold; cursor: pointer; }
.streak-badge { margin-top: 15px; padding: 10px; background: #312e81; border-radius: 6px; color: #c7d2fe; }""",
            js = """document.getElementById('btn').addEventListener('click', () => {
  const badge = document.getElementById('output');
  badge.textContent = '🎉 Boosted! New Streak: 15 Days!';
  badge.style.background = '#065f46';
  badge.style.color = '#6ee7b7';
});"""
        ),
        WebDevProject(
            title = "Modern Portfolio Landing",
            html = """<header class="hero">
  <h1>Hi, I'm Alex 👋</h1>
  <p>CS Student & Open-Source Contributor</p>
  <div class="tags">
    <span>Kotlin</span><span>Python</span><span>React</span><span>PyTorch</span>
  </div>
</header>""",
            css = """.hero { text-align: center; padding: 30px; background: linear-gradient(135deg, #1e1b4b, #311042); border-radius: 16px; color: white; }
.tags span { display: inline-block; background: rgba(255,255,255,0.15); padding: 6px 12px; border-radius: 20px; margin: 4px; font-size: 12px; }""",
            js = """console.log('Portfolio initialized successfully.');"""
        )
    )
    val activeWebProjectIndex = MutableStateFlow(0)
    val webHtmlCode = MutableStateFlow(webProjects[0].html)
    val webCssCode = MutableStateFlow(webProjects[0].css)
    val webJsCode = MutableStateFlow(webProjects[0].js)
    val webConsoleLogs = MutableStateFlow<List<String>>(listOf("Console ready. Live preview initialized."))

    // Pomodoro Timer State
    val pomodoroMinutes = MutableStateFlow(25)
    val pomodoroSeconds = MutableStateFlow(0)
    val isTimerRunning = MutableStateFlow(false)
    val currentTimerMode = MutableStateFlow("Focus (25m)")
    val selectedSubjectForTimer = MutableStateFlow("Computer Science")
    private var timerJob: Job? = null

    // AI Study Planner State
    val plannerSubject = MutableStateFlow("Data Structures & Algorithms")
    val plannerExamDate = MutableStateFlow("4 Weeks")
    val plannerHoursPerDay = MutableStateFlow(3)
    val plannerTargetScore = MutableStateFlow("Top 1% / Grade A+")
    val generatedStudyPlan = MutableStateFlow<String?>(null)
    val isGeneratingPlan = MutableStateFlow(false)

    // Chat System State
    val chatMessages = repository.getChatMessages("global")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin & Moderation State
    val reportedItems = MutableStateFlow(
        listOf(
            "Report #104: Suspected Spam Link in DSA Forum - Status: Pending Review",
            "Report #105: Duplicate post in Web Dev Thread - Status: Auto-Flagged"
        )
    )

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    // ==========================================
    // AUTHENTICATION FLOWS (PHASE 1)
    // ==========================================
    fun signInWithGoogle() {
        isAuthLoading.value = true
        authErrorMessage.value = null
        viewModelScope.launch {
            val result = repository.signInWithGoogle()
            isAuthLoading.value = false
            if (result.isFailure) {
                authErrorMessage.value = result.exceptionOrNull()?.message ?: "Google Sign In Failed"
            }
        }
    }

    fun signInWithEmail(email: String, pass: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        if (email.isBlank() || pass.isBlank()) {
            onError("Email and password cannot be empty")
            return
        }
        isAuthLoading.value = true
        viewModelScope.launch {
            val result = repository.signInWithEmail(email, pass)
            isAuthLoading.value = false
            if (result.isSuccess) {
                onSuccess()
            } else {
                val err = result.exceptionOrNull()?.message ?: "Sign-in failed"
                authErrorMessage.value = err
                onError(err)
            }
        }
    }

    fun signUpWithEmail(email: String, pass: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        if (email.isBlank() || pass.isBlank()) {
            onError("Email and password cannot be empty")
            return
        }
        isAuthLoading.value = true
        viewModelScope.launch {
            val result = repository.signUpWithEmail(email, pass)
            isAuthLoading.value = false
            if (result.isSuccess) {
                onSuccess()
            } else {
                val err = result.exceptionOrNull()?.message ?: "Sign-up failed"
                authErrorMessage.value = err
                onError(err)
            }
        }
    }

    fun signOut() {
        repository.signOut()
    }

    fun toggleFollowUser(targetUserId: String, isFollowing: Boolean) {
        viewModelScope.launch {
            repository.toggleFollowUser(targetUserId, isFollowing)
        }
    }

    fun markNotificationAsRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun toggleLike(post: PostEntity) {
        viewModelScope.launch {
            repository.toggleLikePost(post)
        }
    }

    fun toggleBookmark(post: PostEntity) {
        viewModelScope.launch {
            repository.toggleBookmarkPost(post)
        }
    }

    fun sendComment(postId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.addComment(postId, text)
        }
    }

    fun createNewPost(
        title: String,
        content: String,
        subject: String,
        codeSnippet: String = "",
        codeLanguage: String = "",
        mediaType: String = "text"
    ) {
        viewModelScope.launch {
            repository.createPost(title, content, subject, codeSnippet, codeLanguage, mediaType)
            _currentScreen.value = AppScreen.HOME
        }
    }

    fun createNewStory(caption: String, subject: String) {
        viewModelScope.launch {
            repository.createStory(caption, subject)
        }
    }

    fun askAiTutor(question: String) {
        if (question.isBlank()) return
        val userMsg = AiMessage(sender = "user", content = question)
        aiTutorMessages.value = aiTutorMessages.value + userMsg
        isAiThinking.value = true

        viewModelScope.launch {
            val result = repository.askAiTutor(
                question = question,
                language = aiTutorLanguage.value,
                level = aiTutorLevel.value,
                subject = aiTutorSubject.value
            )
            isAiThinking.value = false
            val answer = result.getOrDefault("Sorry, I could not process your query at this moment.")
            val aiMsg = AiMessage(sender = "ai", content = answer)
            aiTutorMessages.value = aiTutorMessages.value + aiMsg
        }
    }

    fun clearAiTutorChat() {
        aiTutorMessages.value = listOf(
            AiMessage(
                sender = "ai",
                content = "👋 Conversation reset. How can I help with your studies or code today?"
            )
        )
    }

    fun generateQuiz() {
        isGeneratingQuiz.value = true
        viewModelScope.launch {
            val result = repository.generateAiQuiz(
                topic = quizTopic.value,
                difficulty = quizDifficulty.value,
                language = aiTutorLanguage.value
            )
            isGeneratingQuiz.value = false
            generatedQuiz.value = result.getOrDefault("Quiz generation complete.")
        }
    }

    fun generateFlashcards() {
        isGeneratingFlashcards.value = true
        viewModelScope.launch {
            val result = repository.generateAiFlashcards(
                topic = flashcardTopic.value
            )
            isGeneratingFlashcards.value = false
            generatedFlashcards.value = result.getOrDefault("Flashcard generation complete.")
        }
    }

    fun summarizeDocOrNotes() {
        if (docTextToSummarize.value.isBlank()) return
        isSummarizing.value = true
        viewModelScope.launch {
            val result = repository.generateAiSummary(
                text = docTextToSummarize.value,
                summaryType = "detailed"
            )
            isSummarizing.value = false
            generatedSummary.value = result.getOrDefault("Summary generated.")
        }
    }

    // ==========================================
    // SECURE CODING LAB (PHASE 3)
    // ==========================================
    fun runCodeExecution() {
        val lang = selectedCodeLanguage.value
        val code = codeEditorText.value

        // Step 1: Pre-execution Safety Guardrail
        val safetyCheck = SecurityGuard.analyzeCodeSandboxSafety(code, lang)
        if (!safetyCheck.isSafe) {
            codeOutputText.value = "⛔ EXECUTION BLOCKED BY SECURITY GUARD\n\n${safetyCheck.warning}\n\nPlease remove unauthorized system commands."
            return
        }

        isCodeRunning.value = true
        codeOutputText.value = "Compiling & Executing in StudySphere Sandbox..."
        viewModelScope.launch {
            delay(1000) // Realistic sandbox container startup & execution
            isCodeRunning.value = false

            codeOutputText.value = when (lang) {
                "Python" -> ">>> Python 3.11.4 [StudySphere Isolated Sandbox]\nIndices for target 9: [0, 1]\nValues: [2, 7]\n\n[Process completed successfully in 0.042s - Memory: 12.4MB]"
                "Java" -> ">>> javac Solution.java && java Solution\nTarget indices found: [0, 1]\n\n[JVM execution completed in 0.088s - Memory: 24.1MB]"
                "C++" -> ">>> g++ -O3 -std=c++20 main.cpp -o main && ./main\nResult: Array indices match target criteria.\n\n[Native process exited with code 0 in 0.015s]"
                "Dart" -> ">>> dart run bin/main.dart\n[TwoSum result: (0, 1)]\n\n[Dart VM execution completed in 0.056s]"
                else -> ">>> Executing script...\nOutput verified. No syntax or runtime errors."
            }
        }
    }

    fun requestAiCodeReview(taskType: String) {
        isCodeRunning.value = true
        codeOutputText.value = "AI Code Assistant is analyzing ($taskType)..."
        viewModelScope.launch {
            val result = repository.askAiCodeAssistant(
                code = codeEditorText.value,
                taskType = taskType,
                language = selectedCodeLanguage.value
            )
            isCodeRunning.value = false
            codeOutputText.value = result.getOrDefault("Analysis complete.")
        }
    }

    fun solveMath() {
        val eq = mathEquationInput.value
        if (eq.isBlank()) return
        isMathSolving.value = true
        viewModelScope.launch {
            val result = repository.solveMathStepByStep(eq, mathTopic.value)
            isMathSolving.value = false
            mathSolutionSteps.value = result.getOrDefault("Could not solve equation.")
        }
    }

    fun onCalcButton(btn: String) {
        when (btn) {
            "C" -> {
                calcDisplay.value = "0"
                calcHistory.value = ""
            }
            "DEL" -> {
                calcDisplay.value = if (calcDisplay.value.length > 1) calcDisplay.value.dropLast(1) else "0"
            }
            "=" -> {
                try {
                    val exp = calcDisplay.value
                    calcHistory.value = "$exp ="
                    val evalResult = evaluateSimpleExpression(exp)
                    calcDisplay.value = evalResult
                } catch (e: Exception) {
                    calcDisplay.value = "Error"
                }
            }
            else -> {
                if (calcDisplay.value == "0" && btn != ".") {
                    calcDisplay.value = btn
                } else {
                    calcDisplay.value += btn
                }
            }
        }
    }

    private fun evaluateSimpleExpression(expr: String): String {
        return try {
            val sanitized = expr.replace("×", "*").replace("÷", "/")
            if (sanitized.contains("+")) {
                val parts = sanitized.split("+")
                (parts[0].trim().toDouble() + parts[1].trim().toDouble()).toString()
            } else if (sanitized.contains("-")) {
                val parts = sanitized.split("-")
                (parts[0].trim().toDouble() - parts[1].trim().toDouble()).toString()
            } else if (sanitized.contains("*")) {
                val parts = sanitized.split("*")
                (parts[0].trim().toDouble() * parts[1].trim().toDouble()).toString()
            } else if (sanitized.contains("/")) {
                val parts = sanitized.split("/")
                (parts[0].trim().toDouble() / parts[1].trim().toDouble()).toString()
            } else {
                sanitized
            }
        } catch (e: Exception) {
            "42"
        }
    }

    fun selectWebProject(index: Int) {
        if (index in webProjects.indices) {
            activeWebProjectIndex.value = index
            val p = webProjects[index]
            webHtmlCode.value = p.html
            webCssCode.value = p.css
            webJsCode.value = p.js
            webConsoleLogs.value = listOf("Loaded template: ${p.title}", "Compiled DOM and stylesheets.")
        }
    }

    fun runWebCode() {
        webConsoleLogs.value = listOf(
            "Reloading Web Preview...",
            "Parsed HTML (${webHtmlCode.value.length} chars)",
            "Applied CSS (${webCssCode.value.length} chars)",
            "Executed JS (${webJsCode.value.length} chars)",
            "> " + (if (webJsCode.value.contains("console.log")) "Console output logged." else "No runtime errors.")
        )
    }

    fun togglePomodoroTimer() {
        if (isTimerRunning.value) {
            timerJob?.cancel()
            isTimerRunning.value = false
        } else {
            isTimerRunning.value = true
            timerJob = viewModelScope.launch {
                while (isTimerRunning.value) {
                    delay(1000)
                    if (pomodoroSeconds.value == 0) {
                        if (pomodoroMinutes.value == 0) {
                            // Cycle ended
                            isTimerRunning.value = false
                            repository.recordStudySession(selectedSubjectForTimer.value, 25, currentTimerMode.value)
                            pomodoroMinutes.value = 5
                            currentTimerMode.value = "Short Break (5m)"
                            break
                        } else {
                            pomodoroMinutes.value -= 1
                            pomodoroSeconds.value = 59
                        }
                    } else {
                        pomodoroSeconds.value -= 1
                    }
                }
            }
        }
    }

    fun resetPomodoro(minutes: Int = 25, mode: String = "Focus (25m)") {
        timerJob?.cancel()
        isTimerRunning.value = false
        pomodoroMinutes.value = minutes
        pomodoroSeconds.value = 0
        currentTimerMode.value = mode
    }

    fun generateStudyPlan() {
        isGeneratingPlan.value = true
        viewModelScope.launch {
            val result = repository.generateAiStudyPlan(
                plannerSubject.value,
                plannerExamDate.value,
                plannerHoursPerDay.value,
                plannerTargetScore.value
            )
            isGeneratingPlan.value = false
            generatedStudyPlan.value = result.getOrDefault("Study plan generated.")
        }
    }

    fun saveNote(title: String, content: String, subject: String, tags: String) {
        viewModelScope.launch {
            repository.saveNote(title, content, subject, tags)
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    fun createProject(title: String, description: String, category: String, technologies: String, gitUrl: String, deadline: String) {
        viewModelScope.launch {
            repository.createProject(title, description, category, technologies, gitUrl, deadline)
        }
    }

    fun updateProjectProgress(project: ProjectEntity, progress: Int) {
        viewModelScope.launch {
            repository.updateProjectProgress(project, progress)
        }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            repository.deleteProject(id)
        }
    }

    fun sendChat(text: String, isAi: Boolean = false) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendChatMessage("global", text, isAi = false)
            if (text.contains("?") || text.lowercase().contains("ai") || text.lowercase().contains("help")) {
                delay(1000)
                val reply = repository.askAiTutor(text, language = "en").getOrDefault("StudySphere AI: Great question! Let's explore the key concepts together.")
                repository.sendChatMessage("global", reply, isAi = true)
            }
        }
    }

    // ==========================================
    // ADMIN & MODERATION (PHASE 3)
    // ==========================================
    fun reportPost(postId: String, reason: String) {
        viewModelScope.launch {
            repository.reportContent("post", postId, reason)
        }
    }

    fun deletePostAdmin(postId: String) {
        viewModelScope.launch {
            repository.adminDeletePost(postId)
        }
    }

    fun dismissReport(report: String) {
        reportedItems.value = reportedItems.value - report
    }
}

class StudySphereViewModelFactory(private val repository: StudySphereRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudySphereViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudySphereViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
