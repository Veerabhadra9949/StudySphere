package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.api.GeminiClient
import com.example.data.firebase.*
import com.example.data.local.*
import com.example.data.security.SecurityGuard
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class StudySphereRepository(
    private val database: AppDatabase,
    context: Context? = null
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val TAG = "StudySphereRepo"

    val authManager: FirebaseAuthManager? = context?.let { FirebaseAuthManager(it) }
    val firestoreService: FirestoreService = FirestoreService()
    val storageService: FirebaseStorageService = FirebaseStorageService()

    // Local Room Reactive Flows (Offline-First Cache)
    val posts: Flow<List<PostEntity>> = database.postDao().getAllPosts()
    val stories: Flow<List<StoryEntity>> = database.postDao().getAllStories()
    val longVideos: Flow<List<VideoEntity>> = database.videoDao().getLongVideos()
    val shortVideos: Flow<List<VideoEntity>> = database.videoDao().getShortVideos()
    val notes: Flow<List<NoteEntity>> = database.noteDao().getAllNotes()
    val projects: Flow<List<ProjectEntity>> = database.projectDao().getAllProjects()
    val studySessions: Flow<List<StudySessionEntity>> = database.studyDao().getAllSessions()
    val achievements: Flow<List<AchievementEntity>> = database.studyDao().getAllAchievements()
    val notifications: Flow<List<NotificationEntity>> = database.notificationDao().getAllNotifications()
    val currentUser: Flow<UserEntity?> = database.userDao().getUser("user_me")

    val authState: Flow<FirebaseUser?> = authManager?.authState ?: flowOf(null)
    val geminiService = com.example.data.api.GeminiService()

    // Global Theme Preference: "system", "dark", "light"
    private val themePrefs = context?.getSharedPreferences("studysphere_theme_prefs", Context.MODE_PRIVATE)
    private val _themeMode = MutableStateFlow(themePrefs?.getString("theme_mode", "system") ?: "system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        themePrefs?.edit()?.putString("theme_mode", mode)?.apply()
    }

    fun getComments(postId: String): Flow<List<CommentEntity>> = database.postDao().getCommentsForPost(postId)
    fun getChatMessages(chatId: String): Flow<List<ChatMessageEntity>> = database.chatDao().getMessages(chatId)

    init {
        // Pre-populate seed data & start Firestore synchronization
        scope.launch {
            seedInitialData()
            observeRemoteFirestore()
        }
    }

    /**
     * Listens to Firestore real-time streams and mirrors remote updates into the local Room cache.
     */
    private fun observeRemoteFirestore() {
        // Observe remote posts
        scope.launch {
            firestoreService.getPostsStream().collectLatest { remotePosts ->
                if (remotePosts.isNotEmpty()) {
                    database.postDao().insertPosts(remotePosts)
                }
            }
        }

        // Observe remote stories
        scope.launch {
            firestoreService.getStoriesStream().collectLatest { remoteStories ->
                if (remoteStories.isNotEmpty()) {
                    database.postDao().insertStories(remoteStories)
                }
            }
        }

        // Observe remote long videos
        scope.launch {
            firestoreService.getVideosStream(isShort = false).collectLatest { remoteVideos ->
                if (remoteVideos.isNotEmpty()) {
                    database.videoDao().insertVideos(remoteVideos)
                }
            }
        }

        // Observe remote short videos
        scope.launch {
            firestoreService.getVideosStream(isShort = true).collectLatest { remoteVideos ->
                if (remoteVideos.isNotEmpty()) {
                    database.videoDao().insertVideos(remoteVideos)
                }
            }
        }

        // Observe remote notifications for user
        scope.launch {
            firestoreService.getNotificationsStream("user_me").collectLatest { remoteNotifications ->
                if (remoteNotifications.isNotEmpty()) {
                    database.notificationDao().insertNotifications(remoteNotifications)
                }
            }
        }
    }

    private suspend fun seedInitialData() {
        val userDao = database.userDao()
        val postDao = database.postDao()
        val videoDao = database.videoDao()
        val noteDao = database.noteDao()
        val projectDao = database.projectDao()
        val studyDao = database.studyDao()
        val notificationDao = database.notificationDao()

        // Seed default user
        userDao.insertOrUpdateUser(
            UserEntity(
                userId = "user_me",
                username = "alex_dev",
                fullName = "Alex Rivera",
                bio = "CS Undergrad @ MIT | Exploring AI/ML, Jetpack Compose & Competitive Math 🚀",
                education = "Computer Science & Engineering (Year 3)",
                interests = "Programming, AI/ML, Mathematics, DSA, Web Development",
                skills = "Kotlin, Python, Java, React, PyTorch, Linear Algebra",
                xp = 1850,
                streakDays = 14,
                level = 6,
                followersCount = 428,
                followingCount = 192
            )
        )

        // Seed Stories
        postDao.insertStories(
            listOf(
                StoryEntity("s1", "Dr. Sarah Chen", "", "Just published our new graph neural network paper! 📄 Check it out!", "AI/ML"),
                StoryEntity("s2", "CodeMaster Ravi", "", "Daily LeetCode #42 Solved in O(1) space! 💡 Two-pointer magic.", "DSA"),
                StoryEntity("s3", "Elena Rostova", "", "Calculus III surfaces visualized in 3D 📐✨", "Math"),
                StoryEntity("s4", "Dev_Priya", "", "Building a full-stack student portal with Compose & Ktor! 📱", "Android"),
                StoryEntity("s5", "Marcus WebLab", "", "Modern CSS Grid tricks you probably didn't know 🚀", "Web Dev")
            )
        )

        // Seed Posts
        postDao.insertPosts(
            listOf(
                PostEntity(
                    id = "p1",
                    authorId = "u_sarah",
                    authorName = "Dr. Sarah Chen",
                    authorHandle = "@sarah_ai",
                    title = "Understanding Transformer Self-Attention in 3 Simple Steps",
                    content = "Self-attention allows tokens in a sequence to dynamically attend to other tokens regardless of distance. Here is the mathematical formulation and an intuitive visualization:",
                    codeSnippet = "def scaled_dot_product_attention(Q, K, V, mask=None):\n    matmul_qk = tf.matmul(Q, K, transpose_b=True)\n    dk = tf.cast(tf.shape(K)[-1], tf.float32)\n    scaled_attention_logits = matmul_qk / tf.math.sqrt(dk)\n    attention_weights = tf.nn.softmax(scaled_attention_logits, axis=-1)\n    return tf.matmul(attention_weights, V)",
                    codeLanguage = "Python",
                    mediaType = "code",
                    subjectTag = "AI & Machine Learning",
                    likesCount = 284,
                    commentsCount = 42,
                    sharesCount = 18,
                    timestamp = System.currentTimeMillis() - 3600000 * 2
                ),
                PostEntity(
                    id = "p2",
                    authorId = "u_ravi",
                    authorName = "Ravi Sharma",
                    authorHandle = "@ravi_algos",
                    title = "Daily DSA Mastery: Inverting a Binary Tree in O(N)",
                    content = "Whether you do it recursively (DFS) or iteratively (BFS with a queue), swapping children at each node is straightforward. Here is the clean Kotlin approach:",
                    codeSnippet = "fun invertTree(root: TreeNode?): TreeNode? {\n    if (root == null) return null\n    val temp = root.left\n    root.left = invertTree(root.right)\n    root.right = invertTree(temp)\n    return root\n}",
                    codeLanguage = "Kotlin",
                    mediaType = "code",
                    subjectTag = "Data Structures & Algos",
                    likesCount = 195,
                    commentsCount = 29,
                    sharesCount = 12,
                    timestamp = System.currentTimeMillis() - 3600000 * 5
                ),
                PostEntity(
                    id = "p3",
                    authorId = "u_math",
                    authorName = "Prof. Euler's Circle",
                    authorHandle = "@math_spheres",
                    title = "Euler's Identity: The Most Beautiful Equation in Math",
                    content = "Why does e^(i*pi) + 1 = 0 connect the five fundamental constants of mathematics (e, i, pi, 1, 0)? It stems directly from Taylor series expansions of exp, sin, and cos.",
                    codeSnippet = "e^(i * theta) = cos(theta) + i * sin(theta)\nWhen theta = pi:\ne^(i * pi) = -1 + 0 = -1\n=> e^(i * pi) + 1 = 0",
                    codeLanguage = "Math",
                    mediaType = "math",
                    subjectTag = "Mathematics",
                    likesCount = 340,
                    commentsCount = 56,
                    sharesCount = 31,
                    timestamp = System.currentTimeMillis() - 3600000 * 12
                )
            )
        )

        // Seed Videos
        videoDao.insertVideos(
            listOf(
                VideoEntity(
                    id = "v1",
                    title = "Full Stack Jetpack Compose & Clean Architecture Masterclass",
                    channelName = "Android Dev Academy",
                    description = "Comprehensive guide into M3, Room, Coroutines, StateFlow and modern navigation in Android.",
                    duration = "45:20",
                    category = "App Development",
                    isShort = false,
                    viewsCount = 14200,
                    likesCount = 1250
                ),
                VideoEntity(
                    id = "v2",
                    title = "Linear Algebra for Deep Learning: Eigenvalues & PCA Demystified",
                    channelName = "Math & AI Hub",
                    description = "Intuitive geometric representations of eigenvectors, matrix transformations, and dimensionality reduction.",
                    duration = "32:15",
                    category = "Mathematics",
                    isShort = false,
                    viewsCount = 9800,
                    likesCount = 890
                ),
                VideoEntity(
                    id = "s_v1",
                    title = "Quick Tip: CSS Flexbox vs Grid in 45 Seconds! ⚡",
                    channelName = "WebDev Quickie",
                    description = "When to use Flexbox (1D) vs Grid (2D) layout.",
                    duration = "0:45",
                    category = "Web Development",
                    isShort = true,
                    viewsCount = 54000,
                    likesCount = 6200
                ),
                VideoEntity(
                    id = "s_v2",
                    title = "Mastering Two Pointers in 60s! 🎯",
                    channelName = "DSA Shorts",
                    description = "Solve sorted array pair sum in O(N) instead of O(N^2).",
                    duration = "0:58",
                    category = "DSA",
                    isShort = true,
                    viewsCount = 82000,
                    likesCount = 9100
                )
            )
        )

        // Seed Projects
        projectDao.insertProjects(
            listOf(
                ProjectEntity(
                    id = "proj_1",
                    title = "AI Code Reviewer & Sandbox",
                    description = "An automated GitHub PR reviewer using Gemini 3.5 Flash that evaluates security, performance, and code smell.",
                    category = "AI/ML",
                    technologies = "Kotlin, Gemini API, GitHub REST API, Room",
                    gitUrl = "https://github.com/alex_dev/ai-code-reviewer",
                    progressPercent = 65,
                    deadline = "2026-11-20",
                    collaboratorsCount = 3,
                    tasksCount = 8,
                    completedTasksCount = 5
                ),
                ProjectEntity(
                    id = "proj_2",
                    title = "Interactive Calculus 3D Visualizer",
                    description = "Real-time parametric surface renderer with vector field animations for STEM learners.",
                    category = "Mathematics",
                    technologies = "Compose Canvas, WebGL, Kotlin Multiplatform",
                    gitUrl = "https://github.com/alex_dev/calc-3d-visualizer",
                    progressPercent = 40,
                    deadline = "2026-12-01",
                    collaboratorsCount = 2,
                    tasksCount = 6,
                    completedTasksCount = 2
                )
            )
        )

        // Seed Notes
        noteDao.insertNote(
            NoteEntity(
                title = "Dynamic Programming Patterns Cheat Sheet",
                content = "1. 0/1 Knapsack: Choices with weights and values.\n2. Unbounded Knapsack: Coin Change.\n3. Longest Common Subsequence: Strings grid matching.\n4. Interval DP: Matrix chain multiplication.\n5. State Machine DP: Stock Buy/Sell with cooldown.",
                subject = "DSA",
                tags = "DP, Algorithms, Interview Prep",
                isPinned = true,
                isFavorite = true
            )
        )
        noteDao.insertNote(
            NoteEntity(
                title = "Gradient Descent & Optimizers Overview",
                content = "SGD with Momentum accelerates in vectors of constant gradient. RMSprop divides learning rate by exponential average of squared gradients. Adam combines Momentum and RMSprop.",
                subject = "AI/ML",
                tags = "Deep Learning, Optimization, Backprop",
                isPinned = false,
                isFavorite = true
            )
        )

        // Seed Achievements
        studyDao.insertAchievements(
            listOf(
                AchievementEntity("ach_1", "7-Day Study Streak", "Completed focus sessions for 7 consecutive days", "ic_streak", 100, true, System.currentTimeMillis() - 86400000 * 3),
                AchievementEntity("ach_2", "Code Craftsman", "Ran and verified 10 algorithms in the Coding Lab", "ic_code", 150, true, System.currentTimeMillis() - 86400000 * 5),
                AchievementEntity("ach_3", "Mathematics Master", "Solved 25 step-by-step equations in Math Lab", "ic_math", 200, true, System.currentTimeMillis() - 86400000 * 1),
                AchievementEntity("ach_4", "Community Pillar", "Shared 5 educational posts and received 50+ likes", "ic_community", 250, false, null),
                AchievementEntity("ach_5", "Polyglot Coder", "Ran projects across Java, Python, C++, and Web", "ic_polyglot", 300, false, null)
            )
        )

        // Seed Notifications
        notificationDao.insertNotifications(
            listOf(
                NotificationEntity(
                    id = "notif_1",
                    title = "🔥 Streak Maintained!",
                    body = "You completed your daily study goal. 14 days and counting!",
                    type = "study",
                    timestamp = System.currentTimeMillis() - 3600000 * 2,
                    isRead = false,
                    targetScreen = "STUDY_DASHBOARD"
                ),
                NotificationEntity(
                    id = "notif_2",
                    title = "💬 New Comment on your Post",
                    body = "Dr. Sarah Chen replied to your Transformer question: 'Great breakdown!'",
                    type = "social",
                    timestamp = System.currentTimeMillis() - 3600000 * 6,
                    isRead = false,
                    targetScreen = "HOME"
                ),
                NotificationEntity(
                    id = "notif_3",
                    title = "🏆 Achievement Unlocked: Code Craftsman",
                    body = "You earned +150 XP for running verified algorithms in Coding Lab.",
                    type = "achievement",
                    timestamp = System.currentTimeMillis() - 86400000,
                    isRead = true,
                    targetScreen = "PROFILE"
                )
            )
        )
    }

    // ==========================================
    // AUTHENTICATION OPERATIONS
    // ==========================================
    suspend fun signInWithGoogle(): Result<UserEntity> {
        val manager = authManager ?: return Result.failure(Exception("Auth Manager not configured"))
        val result = manager.signInWithGoogle()
        return if (result.isSuccess) {
            val firebaseUser = result.getOrThrow()
            val userEntity = manager.toUserEntity(firebaseUser)
            database.userDao().insertOrUpdateUser(userEntity)
            firestoreService.saveUserProfile(userEntity)
            Result.success(userEntity)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Sign-in failed"))
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<UserEntity> {
        val manager = authManager ?: return Result.failure(Exception("Auth Manager not configured"))
        val result = manager.signInWithEmail(email, pass)
        return if (result.isSuccess) {
            val firebaseUser = result.getOrThrow()
            val userEntity = manager.toUserEntity(firebaseUser)
            database.userDao().insertOrUpdateUser(userEntity)
            Result.success(userEntity)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Sign-in failed"))
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String): Result<UserEntity> {
        val manager = authManager ?: return Result.failure(Exception("Auth Manager not configured"))
        val result = manager.signUpWithEmail(email, pass)
        return if (result.isSuccess) {
            val firebaseUser = result.getOrThrow()
            val userEntity = manager.toUserEntity(firebaseUser)
            database.userDao().insertOrUpdateUser(userEntity)
            firestoreService.saveUserProfile(userEntity)
            Result.success(userEntity)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Sign-up failed"))
        }
    }

    fun signOut() {
        authManager?.signOut()
    }

    // ==========================================
    // INTERACTIVE ACTIONS & SYNC
    // ==========================================
    suspend fun toggleLikePost(post: PostEntity) {
        val isLikedNew = !post.isLiked
        val updated = post.copy(
            isLiked = isLikedNew,
            likesCount = if (isLikedNew) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(0)
        )
        database.postDao().updatePost(updated)
        // Sync to Firestore
        firestoreService.updatePostLikeCount(post.id, if (isLikedNew) 1L else -1L)
    }

    suspend fun toggleBookmarkPost(post: PostEntity) {
        val updated = post.copy(isBookmarked = !post.isBookmarked)
        database.postDao().updatePost(updated)
    }

    suspend fun addComment(postId: String, text: String, authorName: String = "Alex Rivera") {
        val sanitizedText = SecurityGuard.sanitizeInput(text, 2000)
        val comment = CommentEntity(
            id = UUID.randomUUID().toString(),
            postId = postId,
            authorName = authorName,
            text = sanitizedText,
            timestamp = System.currentTimeMillis()
        )
        database.postDao().insertComment(comment)
        firestoreService.saveComment(comment)
    }

    suspend fun createPost(
        title: String,
        content: String,
        subject: String,
        codeSnippet: String = "",
        codeLanguage: String = "",
        mediaType: String = "text"
    ) {
        val cleanTitle = SecurityGuard.sanitizeInput(title, 200)
        val cleanContent = SecurityGuard.sanitizeInput(content, 10000)
        val newPost = PostEntity(
            id = UUID.randomUUID().toString(),
            authorId = authManager?.currentFirebaseUser?.uid ?: "user_me",
            authorName = authManager?.currentFirebaseUser?.displayName ?: "Alex Rivera",
            authorHandle = "@" + (authManager?.currentFirebaseUser?.email?.substringBefore("@") ?: "alex_dev"),
            title = cleanTitle,
            content = cleanContent,
            subjectTag = subject,
            codeSnippet = codeSnippet,
            codeLanguage = codeLanguage,
            mediaType = mediaType,
            likesCount = 1,
            isLiked = true,
            timestamp = System.currentTimeMillis()
        )
        database.postDao().insertPost(newPost)
        firestoreService.savePost(newPost)
    }

    suspend fun createStory(caption: String, subject: String) {
        val cleanCaption = SecurityGuard.sanitizeInput(caption, 500)
        val story = StoryEntity(
            id = UUID.randomUUID().toString(),
            authorName = authManager?.currentFirebaseUser?.displayName ?: "Alex Rivera",
            caption = cleanCaption,
            subject = subject,
            timestamp = System.currentTimeMillis()
        )
        database.postDao().insertStories(listOf(story))
        firestoreService.saveStory(story)
    }

    suspend fun saveNote(title: String, content: String, subject: String, tags: String): Long {
        val cleanTitle = SecurityGuard.sanitizeInput(title, 200)
        val cleanContent = SecurityGuard.sanitizeInput(content, 50000)
        return database.noteDao().insertNote(
            NoteEntity(
                title = cleanTitle,
                content = cleanContent,
                subject = subject,
                tags = tags,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteNote(id: Long) = database.noteDao().deleteNote(id)

    suspend fun recordStudySession(subject: String, durationMinutes: Int, mode: String = "Pomodoro") {
        val session = StudySessionEntity(
            subject = subject,
            durationMinutes = durationMinutes,
            mode = mode,
            timestamp = System.currentTimeMillis()
        )
        database.studyDao().insertSession(session)

        // Award XP and update user streak
        val current = database.userDao().getUser("user_me").firstOrNull()
        if (current != null) {
            val xpGain = durationMinutes * 5
            val updatedUser = current.copy(
                xp = current.xp + xpGain,
                level = ((current.xp + xpGain) / 300) + 1
            )
            database.userDao().insertOrUpdateUser(updatedUser)
            firestoreService.saveUserProfile(updatedUser)
        }
    }

    suspend fun createProject(
        title: String,
        description: String,
        category: String,
        technologies: String,
        gitUrl: String,
        deadline: String
    ) {
        val project = ProjectEntity(
            id = UUID.randomUUID().toString(),
            title = SecurityGuard.sanitizeInput(title, 200),
            description = SecurityGuard.sanitizeInput(description, 5000),
            category = category,
            technologies = technologies,
            gitUrl = gitUrl,
            deadline = deadline,
            progressPercent = 0,
            collaboratorsCount = 1,
            tasksCount = 5,
            completedTasksCount = 0
        )
        database.projectDao().insertProject(project)
    }

    suspend fun updateProjectProgress(project: ProjectEntity, newProgress: Int) {
        database.projectDao().updateProject(project.copy(progressPercent = newProgress))
    }

    suspend fun deleteProject(id: String) {
        database.projectDao().deleteProject(id)
    }

    suspend fun sendChatMessage(chatId: String, text: String, isAi: Boolean = false, language: String = "en") {
        val cleanText = SecurityGuard.sanitizeInput(text, 2000)
        database.chatDao().insertMessage(
            ChatMessageEntity(
                chatId = chatId,
                senderId = if (isAi) "ai_tutor" else "user_me",
                senderName = if (isAi) "StudySphere AI Tutor" else "Alex Rivera",
                text = cleanText,
                isAi = isAi,
                language = language,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun toggleFollowUser(targetUserId: String, isFollowing: Boolean) {
        val current = database.userDao().getUser("user_me").firstOrNull() ?: return
        val updated = current.copy(
            followingCount = if (isFollowing) current.followingCount + 1 else (current.followingCount - 1).coerceAtLeast(0)
        )
        database.userDao().insertOrUpdateUser(updated)
        firestoreService.toggleFollow(current.userId, targetUserId, isFollowing)
    }

    suspend fun markNotificationRead(id: String) {
        database.notificationDao().markAsRead(id)
    }

    // ==========================================
    // SECURE AI INTEGRATIONS (PHASE 3 / GEMINI SERVICE)
    // ==========================================
    private fun getCurrentUserId(): String {
        return authManager?.currentFirebaseUser?.uid ?: "user_me"
    }

    private fun isCurrentAdmin(): Boolean {
        val email = authManager?.currentFirebaseUser?.email
        val uid = getCurrentUserId()
        return SecurityGuard.isAdmin(uid, email)
    }

    suspend fun askAiTutor(
        question: String,
        language: String = "en", // "en", "te", "te_en"
        level: String = "beginner",
        subject: String = "General"
    ): Result<String> {
        return geminiService.aiTutor(
            userId = getCurrentUserId(),
            question = question,
            language = language,
            level = level,
            subject = subject,
            isAdmin = isCurrentAdmin()
        )
    }

    suspend fun askAiCodeAssistant(
        code: String,
        taskType: String, // "generate", "explain", "debug", "optimize", "test_cases", "complexity", "convert"
        language: String,
        targetLanguage: String = ""
    ): Result<String> {
        return geminiService.aiCode(
            userId = getCurrentUserId(),
            language = language,
            task = taskType,
            code = code,
            action = taskType,
            targetLanguage = targetLanguage,
            isAdmin = isCurrentAdmin()
        )
    }

    suspend fun solveMathStepByStep(equation: String, topic: String): Result<String> {
        return geminiService.aiMath(
            userId = getCurrentUserId(),
            problem = equation,
            topic = topic,
            stepByStep = true,
            isAdmin = isCurrentAdmin()
        )
    }

    suspend fun generateAiStudyPlan(
        subject: String,
        examDate: String,
        availableHoursPerDay: Int,
        targetScore: String
    ): Result<String> {
        return geminiService.aiStudyPlan(
            userId = getCurrentUserId(),
            subjects = listOf(subject),
            examDate = examDate,
            hoursPerDay = availableHoursPerDay,
            targetScore = targetScore,
            isAdmin = isCurrentAdmin()
        )
    }

    suspend fun generateAiQuiz(
        topic: String,
        difficulty: String = "Medium",
        count: Int = 5,
        language: String = "en"
    ): Result<String> {
        return geminiService.aiQuiz(
            userId = getCurrentUserId(),
            topic = topic,
            difficulty = difficulty,
            count = count,
            language = language,
            isAdmin = isCurrentAdmin()
        )
    }

    suspend fun generateAiFlashcards(
        topic: String,
        content: String = "",
        count: Int = 5
    ): Result<String> {
        return geminiService.aiFlashcards(
            userId = getCurrentUserId(),
            topic = topic,
            content = content,
            count = count,
            isAdmin = isCurrentAdmin()
        )
    }

    suspend fun generateAiSummary(
        text: String,
        summaryType: String = "detailed"
    ): Result<String> {
        return geminiService.aiSummarize(
            userId = getCurrentUserId(),
            text = text,
            summaryType = summaryType,
            isAdmin = isCurrentAdmin()
        )
    }

    suspend fun askAiDocument(
        documentText: String,
        question: String,
        action: String = "qa"
    ): Result<String> {
        return geminiService.aiDocument(
            userId = getCurrentUserId(),
            documentText = documentText,
            question = question,
            action = action,
            isAdmin = isCurrentAdmin()
        )
    }

    suspend fun debugCodeWithAi(
        language: String,
        code: String,
        errorMessage: String
    ): Result<String> {
        return geminiService.aiDebug(
            userId = getCurrentUserId(),
            language = language,
            code = code,
            errorMessage = errorMessage,
            isAdmin = isCurrentAdmin()
        )
    }

    // ==========================================
    // ADMIN & MODERATION
    // ==========================================
    suspend fun reportContent(targetType: String, targetId: String, reason: String) {
        firestoreService.submitModerationReport("user_me", targetType, targetId, reason)
    }

    suspend fun adminDeletePost(postId: String): Result<Unit> {
        val currentEmail = authManager?.currentFirebaseUser?.email
        val currentUid = authManager?.currentFirebaseUser?.uid ?: "user_me"
        if (!SecurityGuard.isAdmin(currentUid, currentEmail)) {
            return Result.failure(SecurityException("Unauthorized: Admin privileges required."))
        }
        firestoreService.deletePostAsAdmin(postId)
        return Result.success(Unit)
    }
}
