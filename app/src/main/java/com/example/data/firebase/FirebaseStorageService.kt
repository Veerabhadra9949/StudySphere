package com.example.data.firebase

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseStorageService {
    private val storage: FirebaseStorage? = FirebaseManager.storage
    private val TAG = "FirebaseStorageService"

    /**
     * Uploads media bytes (e.g. image, diagram, code export) to Firebase Storage
     * and returns the download URL.
     */
    suspend fun uploadFile(
        folder: String,
        userId: String,
        fileName: String = "${UUID.randomUUID()}.jpg",
        data: ByteArray,
        contentType: String = "image/jpeg"
    ): Result<String> {
        val storageInstance = storage ?: return Result.failure(Exception("Firebase Storage not initialized"))
        return try {
            val ref = storageInstance.reference.child("$folder/$userId/$fileName")
            val metadata = com.google.firebase.storage.StorageMetadata.Builder()
                .setContentType(contentType)
                .build()

            ref.putBytes(data, metadata).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.w(TAG, "Storage upload error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Uploads a file Uri to Firebase Storage.
     */
    suspend fun uploadUri(
        folder: String,
        userId: String,
        fileUri: Uri,
        fileName: String = "${UUID.randomUUID()}"
    ): Result<String> {
        val storageInstance = storage ?: return Result.failure(Exception("Firebase Storage not initialized"))
        return try {
            val ref = storageInstance.reference.child("$folder/$userId/$fileName")
            ref.putFile(fileUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.w(TAG, "Storage URI upload error: ${e.message}")
            Result.failure(e)
        }
    }
}
