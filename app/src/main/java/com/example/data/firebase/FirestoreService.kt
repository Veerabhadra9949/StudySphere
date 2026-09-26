package com.example.data.firebase

import android.util.Log
import com.example.data.local.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreService {
    private val firestore: FirebaseFirestore? = FirebaseManager.firestore
    private val TAG = "FirestoreService"

    // ==========================================
    // POSTS
    // ==========================================
    fun getPostsStream(): Flow<List<PostEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Posts listen error: ${e.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            PostEntity(
                                id = doc.id,
                                authorId = doc.getString("authorId") ?: "",
                                authorName = doc.getString("authorName") ?: "Anonymous",
                                authorHandle = doc.getString("authorHandle") ?: "@student",
                                authorAvatar = doc.getString("authorAvatar") ?: "",
                                title = doc.getString("title") ?: "",
                                content = doc.getString("content") ?: "",
                                codeSnippet = doc.getString("codeSnippet") ?: "",
                                codeLanguage = doc.getString("codeLanguage") ?: "",
                                mediaType = doc.getString("mediaType") ?: "text",
                                subjectTag = doc.getString("subjectTag") ?: "General",
                                likesCount = (doc.getLong("likesCount") ?: 0L).toInt(),
                                commentsCount = (doc.getLong("commentsCount") ?: 0L).toInt(),
                                sharesCount = (doc.getLong("sharesCount") ?: 0L).toInt(),
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                isLiked = false,
                                isBookmarked = false
                            )
                        } catch (ex: Exception) {
                            null
                        }
                    }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun savePost(post: PostEntity) {
        val db = firestore ?: return
        val map = hashMapOf(
            "authorId" to post.authorId,
            "authorName" to post.authorName,
            "authorHandle" to post.authorHandle,
            "authorAvatar" to post.authorAvatar,
            "title" to post.title,
            "content" to post.content,
            "codeSnippet" to post.codeSnippet,
            "codeLanguage" to post.codeLanguage,
            "mediaType" to post.mediaType,
            "subjectTag" to post.subjectTag,
            "likesCount" to post.likesCount,
            "commentsCount" to post.commentsCount,
            "sharesCount" to post.sharesCount,
            "timestamp" to post.timestamp
        )
        try {
            db.collection("posts").document(post.id).set(map).await()
        } catch (e: Exception) {
            Log.w(TAG, "Error saving post: ${e.message}")
        }
    }

    suspend fun updatePostLikeCount(postId: String, increment: Long) {
        val db = firestore ?: return
        try {
            db.collection("posts").document(postId)
                .update("likesCount", FieldValue.increment(increment)).await()
        } catch (e: Exception) {
            Log.w(TAG, "Error updating post likes: ${e.message}")
        }
    }

    // ==========================================
    // STORIES
    // ==========================================
    fun getStoriesStream(): Flow<List<StoryEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("stories")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Stories listen error: ${e.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            StoryEntity(
                                id = doc.id,
                                authorName = doc.getString("authorName") ?: "Student",
                                authorAvatar = doc.getString("authorAvatar") ?: "",
                                caption = doc.getString("caption") ?: "",
                                subject = doc.getString("subject") ?: "General",
                                isWatched = false,
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            )
                        } catch (ex: Exception) {
                            null
                        }
                    }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveStory(story: StoryEntity) {
        val db = firestore ?: return
        val map = hashMapOf(
            "authorName" to story.authorName,
            "authorAvatar" to story.authorAvatar,
            "caption" to story.caption,
            "subject" to story.subject,
            "timestamp" to story.timestamp
        )
        try {
            db.collection("stories").document(story.id).set(map).await()
        } catch (e: Exception) {
            Log.w(TAG, "Error saving story: ${e.message}")
        }
    }

    // ==========================================
    // COMMENTS
    // ==========================================
    fun getCommentsStream(postId: String): Flow<List<CommentEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("posts").document(postId).collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Comments listen error: ${e.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            CommentEntity(
                                id = doc.id,
                                postId = postId,
                                authorName = doc.getString("authorName") ?: "Student",
                                authorAvatar = doc.getString("authorAvatar") ?: "",
                                text = doc.getString("text") ?: "",
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            )
                        } catch (ex: Exception) {
                            null
                        }
                    }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveComment(comment: CommentEntity) {
        val db = firestore ?: return
        val map = hashMapOf(
            "authorName" to comment.authorName,
            "authorAvatar" to comment.authorAvatar,
            "text" to comment.text,
            "timestamp" to comment.timestamp
        )
        try {
            db.collection("posts").document(comment.postId).collection("comments")
                .document(comment.id).set(map).await()
            db.collection("posts").document(comment.postId)
                .update("commentsCount", FieldValue.increment(1)).await()
        } catch (e: Exception) {
            Log.w(TAG, "Error saving comment: ${e.message}")
        }
    }

    // ==========================================
    // VIDEOS
    // ==========================================
    fun getVideosStream(isShort: Boolean): Flow<List<VideoEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("videos")
            .whereEqualTo("isShort", isShort)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Videos listen error: ${e.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            VideoEntity(
                                id = doc.id,
                                title = doc.getString("title") ?: "",
                                channelName = doc.getString("channelName") ?: "Study Channel",
                                channelAvatar = doc.getString("channelAvatar") ?: "",
                                description = doc.getString("description") ?: "",
                                duration = doc.getString("duration") ?: "10:00",
                                category = doc.getString("category") ?: "General",
                                isShort = doc.getBoolean("isShort") ?: isShort,
                                viewsCount = (doc.getLong("viewsCount") ?: 0L).toInt(),
                                likesCount = (doc.getLong("likesCount") ?: 0L).toInt(),
                                isSaved = false,
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            )
                        } catch (ex: Exception) {
                            null
                        }
                    }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    // ==========================================
    // USER PROFILES & FOLLOWERS
    // ==========================================
    fun getUserProfileStream(userId: String): Flow<UserEntity?> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }
                try {
                    val user = UserEntity(
                        userId = snapshot.id,
                        username = snapshot.getString("username") ?: "student",
                        fullName = snapshot.getString("fullName") ?: "Student Learner",
                        bio = snapshot.getString("bio") ?: "",
                        avatarUrl = snapshot.getString("avatarUrl") ?: "",
                        education = snapshot.getString("education") ?: "",
                        interests = snapshot.getString("interests") ?: "",
                        skills = snapshot.getString("skills") ?: "",
                        xp = (snapshot.getLong("xp") ?: 100L).toInt(),
                        streakDays = (snapshot.getLong("streakDays") ?: 1L).toInt(),
                        level = (snapshot.getLong("level") ?: 1L).toInt(),
                        followersCount = (snapshot.getLong("followersCount") ?: 0L).toInt(),
                        followingCount = (snapshot.getLong("followingCount") ?: 0L).toInt()
                    )
                    trySend(user)
                } catch (ex: Exception) {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveUserProfile(user: UserEntity) {
        val db = firestore ?: return
        val map = hashMapOf(
            "username" to user.username,
            "fullName" to user.fullName,
            "bio" to user.bio,
            "avatarUrl" to user.avatarUrl,
            "education" to user.education,
            "interests" to user.interests,
            "skills" to user.skills,
            "xp" to user.xp,
            "streakDays" to user.streakDays,
            "level" to user.level,
            "followersCount" to user.followersCount,
            "followingCount" to user.followingCount
        )
        try {
            db.collection("users").document(user.userId).set(map).await()
        } catch (e: Exception) {
            Log.w(TAG, "Error saving user profile: ${e.message}")
        }
    }

    suspend fun toggleFollow(currentUserId: String, targetUserId: String, isFollowing: Boolean) {
        val db = firestore ?: return
        val inc = if (isFollowing) 1L else -1L
        try {
            db.collection("users").document(currentUserId)
                .update("followingCount", FieldValue.increment(inc)).await()
            db.collection("users").document(targetUserId)
                .update("followersCount", FieldValue.increment(inc)).await()
        } catch (e: Exception) {
            Log.w(TAG, "Error toggling follow: ${e.message}")
        }
    }

    // ==========================================
    // NOTIFICATIONS
    // ==========================================
    fun getNotificationsStream(userId: String): Flow<List<NotificationEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("users").document(userId).collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Notifications listen error: ${e.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            NotificationEntity(
                                id = doc.id,
                                title = doc.getString("title") ?: "StudySphere Alert",
                                body = doc.getString("body") ?: "",
                                type = doc.getString("type") ?: "general",
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                isRead = doc.getBoolean("isRead") ?: false,
                                targetScreen = doc.getString("targetScreen")
                            )
                        } catch (ex: Exception) {
                            null
                        }
                    }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveNotification(userId: String, notification: NotificationEntity) {
        val db = firestore ?: return
        val map = hashMapOf(
            "title" to notification.title,
            "body" to notification.body,
            "type" to notification.type,
            "timestamp" to notification.timestamp,
            "isRead" to notification.isRead,
            "targetScreen" to notification.targetScreen
        )
        try {
            db.collection("users").document(userId).collection("notifications")
                .document(notification.id).set(map).await()
        } catch (e: Exception) {
            Log.w(TAG, "Error saving notification: ${e.message}")
        }
    }

    // ==========================================
    // MODERATION REPORTS (Admin)
    // ==========================================
    suspend fun submitModerationReport(
        reporterId: String,
        targetType: String, // "post", "comment", "user"
        targetId: String,
        reason: String
    ) {
        val db = firestore ?: return
        val map = hashMapOf(
            "reporterId" to reporterId,
            "targetType" to targetType,
            "targetId" to targetId,
            "reason" to reason,
            "status" to "pending", // "pending", "reviewed", "dismissed", "removed"
            "timestamp" to System.currentTimeMillis()
        )
        try {
            db.collection("moderation_reports").add(map).await()
        } catch (e: Exception) {
            Log.w(TAG, "Error submitting moderation report: ${e.message}")
        }
    }

    suspend fun deletePostAsAdmin(postId: String) {
        val db = firestore ?: return
        try {
            db.collection("posts").document(postId).delete().await()
        } catch (e: Exception) {
            Log.w(TAG, "Error deleting post as admin: ${e.message}")
        }
    }
}
