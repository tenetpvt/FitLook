package com.example.fitlook.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

/**
 * Represents the current state of an image upload operation.
 */
sealed class UploadState {
    data object Idle : UploadState()
    data class Uploading(val progress: Float) : UploadState()
    data class Success(val downloadUrl: String) : UploadState()
    data class Error(val message: String) : UploadState()
}

/**
 * Handles image compression and upload to Firebase Cloud Storage.
 */
class StorageRepository {

    private val storage = FirebaseStorage.getInstance()
    private val outfitsRef = storage.reference.child("outfits")

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState

    companion object {
        private const val TAG = "StorageRepository"
        private const val MAX_DIMENSION = 1440
        private const val JPEG_QUALITY = 82
        private const val MAX_FILE_SIZE = 10 * 1024 * 1024 // 10MB
    }

    /**
     * Reads a URI from the device gallery, compresses it, and returns the optimized Bitmap.
     * Uses inSampleSize for memory-safe decoding of large camera photos.
     */
    fun loadAndCompressBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            // First pass: decode bounds only to calculate inSampleSize
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            // Calculate inSampleSize to avoid OOM on large camera images
            val inSampleSize = calculateInSampleSize(options, MAX_DIMENSION, MAX_DIMENSION)

            // Second pass: decode with inSampleSize
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            val rawBitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOptions)
            } ?: return null

            // Fine-scale to exactly MAX_DIMENSION
            scaleBitmap(rawBitmap, MAX_DIMENSION)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load/compress bitmap: ${e.message}")
            null
        }
    }

    /**
     * Uploads a compressed bitmap to Firebase Cloud Storage.
     * Emits progress updates via [uploadState] flow.
     */
    suspend fun uploadImage(bitmap: Bitmap): String? {
        return try {
            _uploadState.value = UploadState.Uploading(0f)

            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos)
            val imageBytes = baos.toByteArray()

            if (imageBytes.size > MAX_FILE_SIZE) {
                _uploadState.value = UploadState.Error("Image too large (max 10MB)")
                return null
            }

            val fileName = "${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
            val imageRef = outfitsRef.child(fileName)

            val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build()

            val uploadTask = imageRef.putBytes(imageBytes, metadata)

            // Track progress
            uploadTask.addOnProgressListener { snapshot ->
                val progress = snapshot.bytesTransferred.toFloat() / snapshot.totalByteCount.toFloat()
                _uploadState.value = UploadState.Uploading(progress)
            }

            uploadTask.await()

            val downloadUrl = imageRef.downloadUrl.await().toString()
            _uploadState.value = UploadState.Success(downloadUrl)

            Log.d(TAG, "Upload success: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed: ${e.message}")
            _uploadState.value = UploadState.Error(e.message ?: "Upload failed")
            null
        }
    }

    /**
     * Deletes an image from Firebase Storage by its download URL.
     */
    suspend fun deleteImage(imageUrl: String): Boolean {
        return try {
            if (imageUrl.contains("firebasestorage.googleapis.com") ||
                imageUrl.contains("storage.googleapis.com")) {
                val ref = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                ref.delete().await()
                Log.d(TAG, "Deleted image: $imageUrl")
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete image: ${e.message}")
            false
        }
    }

    /**
     * Resets the upload state back to Idle.
     */
    fun resetState() {
        _uploadState.value = UploadState.Idle
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun scaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap

        val scale = maxDimension.toFloat() / maxOf(width, height)
        return Bitmap.createScaledBitmap(
            bitmap,
            (width * scale).toInt(),
            (height * scale).toInt(),
            true
        )
    }
}
