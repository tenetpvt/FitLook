package com.example.fitlook.data.model

data class Outfit(
    val outfitId: String = "",
    val imageUrl: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "Casual",
    val productLinks: List<ProductLink> = emptyList(),
    val timestamp: Long = 0L
)

data class ProductLink(
    val itemName: String = "",
    val productUrl: String = ""
)
