package com.example.fitlook.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import java.net.URL

/**
 * Holds AI-generated outfit info: title, description, and style category.
 */
data class GeneratedOutfitInfo(
    val title: String,
    val description: String,
    val category: String
)

/**
 * Service for generating outfit titles, descriptions, and categories using Gemini AI.
 */
object GeminiService {

    private const val TAG = "GeminiService"
    private val API_KEY = com.example.fitlook.BuildConfig.GEMINI_API_KEY

    val CATEGORIES = listOf("Casual", "Formal", "Street", "Boho", "Athleisure", "Vintage", "Summer", "Winter")

    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash-lite",
        apiKey = API_KEY,
        generationConfig = generationConfig {
            temperature = 0.7f
            maxOutputTokens = 400
        }
    )

    private fun downloadImage(imageUrl: String): Bitmap? {
        return try {
            val connection = URL(imageUrl).openConnection()
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            val inputStream = connection.getInputStream()
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download image: ${e.message}")
            null
        }
    }

    /**
     * Scales a bitmap so its largest dimension is at most [maxDim] pixels.
     * Prevents OOM when sending large camera captures to Gemini.
     */
    private fun ensureMaxDimension(bitmap: Bitmap, maxDim: Int = 1024): Bitmap {
        return if (bitmap.width > maxDim || bitmap.height > maxDim) {
            val scale = maxDim.toFloat() / maxOf(bitmap.width, bitmap.height)
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else bitmap
    }

    /**
     * Core AI analysis: sends a scaled bitmap to Gemini and parses the structured response.
     */
    private suspend fun analyzeImage(bitmap: Bitmap): Result<GeneratedOutfitInfo> {
        return try {
            val scaledBitmap = ensureMaxDimension(bitmap)
            val categoriesStr = CATEGORIES.joinToString(", ")

            val prompt = """
                You are a fashion expert writing for a premium outfit discovery app called FitLook.
                
                Look at this outfit image and generate:
                1. A catchy, short TITLE (max 5 words) for this outfit.
                2. A compelling DESCRIPTION (2-3 sentences) describing the outfit, its vibe, and when to wear it.
                3. A CATEGORY — pick ONE from: ${'$'}categoriesStr
                
                Respond in EXACTLY this format (no extra text, no markdown):
                TITLE: <your title here>
                DESCRIPTION: <your description here>
                CATEGORY: <one category>
            """.trimIndent()

            val response = model.generateContent(
                content {
                    image(scaledBitmap)
                    text(prompt)
                }
            )

            val text = response.text
            Log.d(TAG, "Gemini response: ${'$'}text")

            if (text == null) {
                return Result.failure(Exception("Gemini returned empty response"))
            }

            val titleMatch = Regex("TITLE:\\s*(.+)").find(text)
            val descMatch = Regex("DESCRIPTION:\\s*(.+?)(?=CATEGORY:)", RegexOption.DOT_MATCHES_ALL).find(text)
            val catMatch = Regex("CATEGORY:\\s*(.+)").find(text)

            val title = titleMatch?.groupValues?.get(1)?.trim() ?: text.take(50).trim()
            val description = descMatch?.groupValues?.get(1)?.trim() ?: text.trim()
            val category = catMatch?.groupValues?.get(1)?.trim() ?: "Casual"

            // Validate category against known list
            val validCategory = CATEGORIES.find { it.equals(category, ignoreCase = true) } ?: "Casual"

            Result.success(GeneratedOutfitInfo(title, description, validCategory))
        } catch (e: Exception) {
            Log.e(TAG, "Gemini error", e)
            Result.failure(Exception("AI error: ${'$'}{e.message}"))
        }
    }

    /**
     * Generates outfit info from an image URL (legacy flow — downloads then analyzes).
     */
    suspend fun generateOutfitInfo(imageUrl: String): Result<GeneratedOutfitInfo> {
        val bitmap = downloadImage(imageUrl)
            ?: return Result.failure(Exception("Could not download image. Check the URL."))
        return analyzeImage(bitmap)
    }

    /**
     * Generates outfit info from a local Bitmap (new flow — instant local analysis).
     * Massive UX win: no need to wait for upload before getting AI tags.
     */
    suspend fun generateOutfitInfo(bitmap: Bitmap): Result<GeneratedOutfitInfo> {
        return analyzeImage(bitmap)
    }
}
