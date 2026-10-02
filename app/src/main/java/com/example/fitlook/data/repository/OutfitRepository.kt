package com.example.fitlook.data.repository

import com.example.fitlook.data.model.Outfit
import com.example.fitlook.data.model.ProductLink
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class OutfitRepository {

    private val db = FirebaseFirestore.getInstance()
    private val outfitsCollection = db.collection("outfits")

    private fun parseOutfit(doc: com.google.firebase.firestore.DocumentSnapshot): Outfit? {
        if (!doc.exists()) return null
        val productLinksRaw = doc.get("productLinks") as? List<Map<String, String>> ?: emptyList()
        val productLinks = productLinksRaw.map { map ->
            ProductLink(
                itemName = map["itemName"] ?: "",
                productUrl = map["productUrl"] ?: ""
            )
        }
        return Outfit(
            outfitId = doc.id,
            imageUrl = doc.getString("imageUrl") ?: "",
            title = doc.getString("title") ?: "",
            description = doc.getString("description") ?: "",
            category = doc.getString("category") ?: "Casual",
            productLinks = productLinks,
            timestamp = doc.getLong("timestamp") ?: 0L
        )
    }

    suspend fun getOutfits(): List<Outfit> {
        return try {
            val snapshot = outfitsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { parseOutfit(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getOutfitById(outfitId: String): Outfit? {
        return try {
            val doc = outfitsCollection.document(outfitId).get().await()
            parseOutfit(doc)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addOutfit(
        imageUrl: String,
        title: String,
        description: String,
        category: String,
        productLinks: List<ProductLink>
    ): Boolean {
        return try {
            val outfitData = mapOf(
                "imageUrl" to imageUrl,
                "title" to title,
                "description" to description,
                "category" to category,
                "productLinks" to productLinks.map { link ->
                    mapOf(
                        "itemName" to link.itemName,
                        "productUrl" to link.productUrl
                    )
                },
                "timestamp" to System.currentTimeMillis()
            )
            outfitsCollection.add(outfitData).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteOutfit(outfitId: String): Boolean {
        return try {
            // Fetch the outfit first to get its imageUrl for storage cleanup
            val doc = outfitsCollection.document(outfitId).get().await()
            val imageUrl = doc.getString("imageUrl") ?: ""

            // Delete from Firestore
            outfitsCollection.document(outfitId).delete().await()

            // Delete associated image from Cloud Storage (if it's a Firebase URL)
            if (imageUrl.contains("firebasestorage.googleapis.com") ||
                imageUrl.contains("storage.googleapis.com")) {
                try {
                    com.google.firebase.storage.FirebaseStorage.getInstance()
                        .getReferenceFromUrl(imageUrl)
                        .delete()
                        .await()
                } catch (e: Exception) {
                    // Image deletion is best-effort; don't fail the outfit deletion
                    android.util.Log.w("OutfitRepository", "Failed to delete storage image: ${e.message}")
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun seedOutfits() {
        val snapshot = outfitsCollection.limit(1).get().await()
        if (!snapshot.isEmpty) return

        val outfits = listOf(
            mapOf(
                "imageUrl" to "https://i.pinimg.com/1200x/1e/40/52/1e40527dfdff0ae315f471a32e02c5e8.jpg",
                "title" to "Elegant Evening Look",
                "description" to "M L XL Pattern: solid color Collar type: Lapel Clothing placket: single-breasted Color classification: Brown Subdivision style: Japanese-style retro Clothing style",
                "category" to "Vintage",
                "productLinks" to listOf(
                    mapOf("itemName" to "Japanese-style Retro Coat", "productUrl" to "https://trendha.com/products/japanese-style-retro-brown-woolen-coat?variant=41337802424454"),
                    mapOf("itemName" to "Cream Colour Sweater", "productUrl" to "https://www.amazon.in/s?k=cream+colour+sweater"),
                    mapOf("itemName" to "Brown Pants Baggy", "productUrl" to "https://www.amazon.in/s?k=brown+pants+baggy")
                ),
                "timestamp" to System.currentTimeMillis()
            ),
            mapOf(
                "imageUrl" to "https://i.pinimg.com/736x/51/36/0e/51360ed5de8e3b709f41c06ebe41e583.jpg",
                "title" to "Old Money Men's Outfit",
                "description" to "Refined old money men's outfit featuring an olive crew neck sweater paired with high-waisted cocoa brown trousers and classic loafers.",
                "category" to "Formal",
                "productLinks" to listOf(
                    mapOf("itemName" to "Olive Crew Neck Sweater", "productUrl" to "https://www.amazon.in/Van-Heusen-Cotton-Sweater-VSSWURGFK57243_Olive/dp/B0DGPY1BDV/ref=sr_1_6?sr=8-6&psc=1"),
                    mapOf("itemName" to "Brown Mid Rise Jeans", "productUrl" to "https://www.amazon.in/Urbano-Plus-Washed-Non-Stretchable-plusjeanloosep-brown-42/dp/B0CVV2867V/ref=sr_1_10?sr=8-10&psc=1"),
                    mapOf("itemName" to "Brown Formal Shoes", "productUrl" to "https://www.amazon.in/Centrino-Brown-Formal-Shoe-64055-2/dp/B0CFY1N69X/ref=sr_1_7?sr=8-7&psc=1")
                ),
                "timestamp" to System.currentTimeMillis() - 1000
            ),
            mapOf(
                "imageUrl" to "https://i.pinimg.com/736x/32/69/b3/3269b37d0d646223f0e8417aade68190.jpg",
                "title" to "Light Academia Oversized Knit Vest",
                "description" to "The Modern Academic: Pair with a white linen shirt, rolled sleeves, and light-colored chinos (as seen in the image) for a sophisticated, European-inspired look.",
                "category" to "Casual",
                "productLinks" to listOf(
                    mapOf("itemName" to "Cotton Full Sleeve Shirt", "productUrl" to "https://www.amazon.in/Urbano-Fashion-Cotton-Regular-shirtsolreg-01-white-m/dp/B0DHZPFM4K/ref=sr_1_14?sr=8-14&psc=1"),
                    mapOf("itemName" to "V Neck Cotton Half Sweater", "productUrl" to "https://www.amazon.in/GODFREY-Regular-Sleeves-V-Neck-Sweater/dp/B0CJVJZ57F/ref=sr_1_6?sr=8-6&psc=1"),
                    mapOf("itemName" to "Raymond Formal White Trouser", "productUrl" to "https://www.amazon.in/Raymond-Slim-Self-Design-Trousers/dp/B0CSNL9MNN/ref=sr_1_19?sr=8-19&psc=1")
                ),
                "timestamp" to System.currentTimeMillis() - 2000
            ),
            mapOf(
                "imageUrl" to "https://i.pinimg.com/736x/e7/ea/82/e7ea82a18576194c35aec3683a8807b1.jpg",
                "title" to "Summer Outfit",
                "description" to "This look captures a timeless, effortless Parisian aesthetic, blending a relaxed navy button-down with classic sand-colored chinos for a clean, high-contrast silhouette",
                "category" to "Summer",
                "productLinks" to listOf(
                    mapOf("itemName" to "Navy Blue Shirt", "productUrl" to "https://www.amazon.in/MADHAVISTA-Lightweight-Breathable-Stylish-Everyday/dp/B0FMFFT83B/ref=sr_1_6?sr=8-6&psc=1"),
                    mapOf("itemName" to "Cream Baggy Trousers", "productUrl" to "https://www.amazon.in/Urbano-Fashion-Korean-Stretchable-koreantrou-cream-32/dp/B0DSB95W74/ref=sr_1_7?sr=8-7&psc=1"),
                    mapOf("itemName" to "Puma White Sneakers", "productUrl" to "https://www.amazon.in/Puma-Unisex-Adult-Smashic-White-Matte-Sneaker/dp/B0BSLJBJ3N/ref=sr_1_7?sr=8-7&psc=1")
                ),
                "timestamp" to System.currentTimeMillis() - 3000
            )
        )

        outfits.forEach { outfit ->
            outfitsCollection.add(outfit).await()
        }
    }
}
