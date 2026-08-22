package com.example.fitlook.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages liked outfits using SharedPreferences for local persistence.
 * Uses a StateFlow so Compose screens automatically recompose on changes.
 */
object LikedOutfitsManager {

    private const val PREFS_NAME = "fitlook_likes"
    private const val KEY_LIKED_IDS = "liked_outfit_ids"

    private lateinit var prefs: SharedPreferences
    private val _likedIds = MutableStateFlow<Set<String>>(emptySet())
    val likedIds: StateFlow<Set<String>> = _likedIds.asStateFlow()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _likedIds.value = prefs.getStringSet(KEY_LIKED_IDS, emptySet()) ?: emptySet()
    }

    fun toggleLike(outfitId: String) {
        val current = _likedIds.value.toMutableSet()
        if (current.contains(outfitId)) {
            current.remove(outfitId)
        } else {
            current.add(outfitId)
        }
        _likedIds.value = current
        prefs.edit().putStringSet(KEY_LIKED_IDS, current).apply()
    }

    fun isLiked(outfitId: String): Boolean = _likedIds.value.contains(outfitId)
}
