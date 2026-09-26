package com.example.data.firebase

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.example.data.local.UserEntity
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager(private val context: Context) {
    private val auth: FirebaseAuth? = FirebaseManager.auth
    private val credentialManager = CredentialManager.create(context)

    val authState: Flow<FirebaseUser?> = callbackFlow {
        val authInstance = auth
        if (authInstance == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = FirebaseAuth.AuthStateListener { currentAuth ->
            trySend(currentAuth.currentUser)
        }
        authInstance.addAuthStateListener(listener)
        awaitClose { authInstance.removeAuthStateListener(listener) }
    }

    val currentFirebaseUser: FirebaseUser?
        get() = auth?.currentUser

    /**
     * Signs in with Google using Jetpack Credential Manager.
     */
    suspend fun signInWithGoogle(webClientId: String = "123456789012-mockclientid.apps.googleusercontent.com"): Result<FirebaseUser> {
        val authInstance = auth ?: return Result.failure(Exception("Firebase Auth not initialized"))
        return try {
            val googleIdOption = GetSignInWithGoogleOption.Builder(webClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result: GetCredentialResponse = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = authInstance.signInWithCredential(authCredential).await()
                val user = authResult.user ?: throw Exception("Google sign in failed: Null user")
                Result.success(user)
            } else {
                Result.failure(Exception("Unexpected credential type returned"))
            }
        } catch (e: Exception) {
            Log.w("FirebaseAuthManager", "Google Sign In note: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Sign in with Email and Password
     */
    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser> {
        val authInstance = auth ?: return Result.failure(Exception("Firebase Auth not initialized"))
        return try {
            val result = authInstance.signInWithEmailAndPassword(email, pass).await()
            val user = result.user ?: throw Exception("Null user after sign in")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign up with Email and Password
     */
    suspend fun signUpWithEmail(email: String, pass: String): Result<FirebaseUser> {
        val authInstance = auth ?: return Result.failure(Exception("Firebase Auth not initialized"))
        return try {
            val result = authInstance.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user ?: throw Exception("Null user after sign up")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign out current user
     */
    fun signOut() {
        auth?.signOut()
    }

    /**
     * Converts FirebaseUser into a local UserEntity
     */
    fun toUserEntity(firebaseUser: FirebaseUser, existingUser: UserEntity? = null): UserEntity {
        val displayName = firebaseUser.displayName?.ifBlank { null } ?: "Student User"
        val emailName = firebaseUser.email?.substringBefore("@") ?: "student"
        return UserEntity(
            userId = firebaseUser.uid,
            username = existingUser?.username ?: emailName,
            fullName = displayName,
            bio = existingUser?.bio ?: "StudySphere Learner | AI, Coding & STEM Enthusiast 🚀",
            avatarUrl = firebaseUser.photoUrl?.toString() ?: existingUser?.avatarUrl ?: "",
            education = existingUser?.education ?: "Computer Science & Engineering",
            interests = existingUser?.interests ?: "Programming, AI/ML, Mathematics, DSA",
            skills = existingUser?.skills ?: "Kotlin, Python, Java, Algorithms",
            xp = existingUser?.xp ?: 100,
            streakDays = existingUser?.streakDays ?: 1,
            level = existingUser?.level ?: 1,
            followersCount = existingUser?.followersCount ?: 0,
            followingCount = existingUser?.followingCount ?: 0
        )
    }
}
