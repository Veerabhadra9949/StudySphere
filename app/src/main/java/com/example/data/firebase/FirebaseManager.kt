package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    private var isInitialized = false

    fun init(context: Context) {
        if (!isInitialized) {
            try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    FirebaseApp.initializeApp(context)
                }
                // Configure Firestore offline cache and persistent settings
                try {
                    val firestore = FirebaseFirestore.getInstance()
                    val settings = FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(true)
                        .build()
                    firestore.firestoreSettings = settings
                } catch (e: Exception) {
                    Log.w(TAG, "Firestore settings initialization note: ${e.message}")
                }
                isInitialized = true
                Log.d(TAG, "Firebase initialized successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "Firebase initialization error: ${e.message}", e)
            }
        }
    }

    val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth not available: ${e.message}")
            null
        }

    val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseFirestore not available: ${e.message}")
            null
        }

    val storage: FirebaseStorage?
        get() = try {
            FirebaseStorage.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseStorage not available: ${e.message}")
            null
        }

    val messaging: FirebaseMessaging?
        get() = try {
            FirebaseMessaging.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseMessaging not available: ${e.message}")
            null
        }
}
