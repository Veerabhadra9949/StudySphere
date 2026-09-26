package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String = "user_me",
    val username: String = "alex_dev",
    val fullName: String = "Alex Rivera",
    val bio: String = "CS Undergrad @ MIT | Exploring AI/ML, Flutter & Competitive Math 🚀",
    val avatarUrl: String = "",
    val education: String = "Computer Science & Engineering",
    val interests: String = "Programming, AI/ML, Mathematics, DSA, Web Development",
    val skills: String = "Kotlin, Python, Java, React, Algorithms",
    val xp: Int = 1250,
    val streakDays: Int = 12,
    val level: Int = 5,
    val followersCount: Int = 342,
    val followingCount: Int = 189
)

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val authorName: String,
    val authorHandle: String,
    val authorAvatar: String = "",
    val title: String,
    val content: String,
    val codeSnippet: String = "",
    val codeLanguage: String = "",
    val mediaType: String = "text", // "text", "image", "code", "math"
    val subjectTag: String = "General",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val postId: String,
    val authorName: String,
    val authorAvatar: String = "",
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: String,
    val authorName: String,
    val authorAvatar: String = "",
    val caption: String,
    val subject: String,
    val isWatched: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val channelName: String,
    val channelAvatar: String = "",
    val description: String,
    val duration: String,
    val category: String,
    val isShort: Boolean = false,
    val viewsCount: Int = 1200,
    val likesCount: Int = 145,
    val isSaved: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val subject: String,
    val tags: String = "",
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val technologies: String,
    val gitUrl: String = "",
    val progressPercent: Int = 0,
    val deadline: String = "2026-10-15",
    val collaboratorsCount: Int = 1,
    val tasksCount: Int = 4,
    val completedTasksCount: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val durationMinutes: Int,
    val mode: String = "Pomodoro",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: String = "global",
    val senderId: String,
    val senderName: String,
    val text: String,
    val isAi: Boolean = false,
    val language: String = "en",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val xpReward: Int,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val type: String = "general", // "study", "social", "achievement", "security"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val targetScreen: String? = null
)
