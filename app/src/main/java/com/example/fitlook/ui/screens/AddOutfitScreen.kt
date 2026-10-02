package com.example.fitlook.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.fitlook.data.model.ProductLink
import com.example.fitlook.data.repository.GeminiService
import com.example.fitlook.data.repository.OutfitRepository
import com.example.fitlook.data.repository.StorageRepository
import com.example.fitlook.data.repository.UploadState
import com.example.fitlook.ui.theme.CoralAccent
import com.example.fitlook.ui.theme.DarkCard
import com.example.fitlook.ui.theme.LightGray
import com.example.fitlook.ui.theme.RoseGold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ProductLinkInput(
    var itemName: String = "",
    var productUrl: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOutfitScreen(
    onBackClick: () -> Unit,
    onOutfitAdded: () -> Unit
) {
    // ─── State ───
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var compressedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageUrl by remember { mutableStateOf("") }
    var showUrlField by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Casual") }
    val productLinks = remember { mutableStateListOf(ProductLinkInput()) }
    var isGenerating by remember { mutableStateOf(false) }
    var isPublishing by remember { mutableStateOf(false) }
    var publishStatus by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repository = remember { OutfitRepository() }
    val storageRepository = remember { StorageRepository() }
    val uploadState by storageRepository.uploadState.collectAsState()

    // ─── Photo Picker ───
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            // Compress in background
            scope.launch {
                val bitmap = withContext(Dispatchers.IO) {
                    storageRepository.loadAndCompressBitmap(context, uri)
                }
                compressedBitmap = bitmap
                if (bitmap == null) {
                    Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val hasImage = selectedImageUri != null || imageUrl.startsWith("http")

    val fieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = DarkCard,
        focusedContainerColor = DarkCard,
        unfocusedBorderColor = Color.Transparent,
        focusedBorderColor = RoseGold,
        cursorColor = RoseGold,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedLabelColor = RoseGold,
        unfocusedLabelColor = LightGray
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Outfit",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ─── Outfit Image Section ───
            Text(
                text = "Outfit Image",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Photo Picker Dropzone
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (selectedImageUri != null) 260.dp else 160.dp)
                    .animateContentSize()
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                if (selectedImageUri != null) {
                    // Show local preview
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected outfit",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Change / remove badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .clickable {
                                    selectedImageUri = null
                                    compressedBitmap = null
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "✕ Remove",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Empty dropzone
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 2.dp,
                                color = LightGray.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = LightGray,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap to select outfit photo\nfrom gallery",
                                style = MaterialTheme.typography.bodyMedium,
                                color = LightGray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Upload progress indicator
            if (uploadState is UploadState.Uploading) {
                val progress = (uploadState as UploadState.Uploading).progress
                Column {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = RoseGold,
                        trackColor = DarkCard
                    )
                    Text(
                        text = "Uploading: ${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = LightGray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Expandable URL fallback
            Text(
                text = if (showUrlField) "▾ Or paste image URL" else "▸ Or paste image URL",
                style = MaterialTheme.typography.labelLarge,
                color = LightGray,
                modifier = Modifier.clickable { showUrlField = !showUrlField }
            )
            AnimatedVisibility(visible = showUrlField) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Paste image URL") },
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors,
                        singleLine = true
                    )
                    // URL preview
                    AnimatedVisibility(visible = imageUrl.startsWith("http")) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCard)
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "URL preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            // ─── AI Generate Button ───
            ElevatedButton(
                onClick = {
                    if (compressedBitmap == null && imageUrl.isBlank()) {
                        Toast.makeText(context, "Select a photo or paste a URL first!", Toast.LENGTH_SHORT).show()
                        return@ElevatedButton
                    }
                    isGenerating = true
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            if (compressedBitmap != null) {
                                // Fast path: local bitmap → instant AI
                                GeminiService.generateOutfitInfo(compressedBitmap!!)
                            } else {
                                // Legacy path: download from URL
                                GeminiService.generateOutfitInfo(imageUrl)
                            }
                        }
                        isGenerating = false
                        result.onSuccess { info ->
                            title = info.title
                            description = info.description
                            category = info.category
                            Toast.makeText(context, "✨ AI generated info!", Toast.LENGTH_SHORT).show()
                        }.onFailure { error ->
                            Toast.makeText(context, error.message ?: "AI generation failed", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = RoseGold,
                    contentColor = Color.Black
                ),
                enabled = !isGenerating && hasImage,
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Analyzing outfit with AI...",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                } else {
                    Text(
                        "✨ Generate with AI",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // ─── Title ───
            Text(
                text = "Title",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Outfit title") },
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors,
                singleLine = true
            )

            // ─── Description ───
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Outfit description") },
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors,
                minLines = 3,
                maxLines = 5
            )

            // ─── Category ───
            Text(
                text = "Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GeminiService.CATEGORIES.forEach { cat ->
                    val isSelected = cat == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) Brush.horizontalGradient(listOf(RoseGold, CoralAccent))
                                else Brush.horizontalGradient(listOf(DarkCard, DarkCard))
                            )
                            .clickable { category = cat }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) Color.Black else LightGray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // ─── Product Links ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Product Links",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(RoseGold)
                        .clickable { productLinks.add(ProductLinkInput()) }
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add product",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            productLinks.forEachIndexed { index, link ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Item ${index + 1}",
                                style = MaterialTheme.typography.labelLarge,
                                color = RoseGold,
                                fontWeight = FontWeight.Bold
                            )
                            if (productLinks.size > 1) {
                                IconButton(
                                    onClick = { productLinks.removeAt(index) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = LightGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = link.itemName,
                            onValueChange = { productLinks[index] = link.copy(itemName = it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Product name (e.g., Blue Denim Jacket)") },
                            shape = RoundedCornerShape(10.dp),
                            colors = fieldColors,
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = link.productUrl,
                            onValueChange = { productLinks[index] = link.copy(productUrl = it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Shopping URL") },
                            shape = RoundedCornerShape(10.dp),
                            colors = fieldColors,
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ─── Publish Button ───
            ElevatedButton(
                onClick = {
                    if (!hasImage || title.isBlank()) {
                        Toast.makeText(context, "Image and Title are required!", Toast.LENGTH_SHORT).show()
                        return@ElevatedButton
                    }
                    isPublishing = true
                    scope.launch {
                        val links = productLinks
                            .filter { it.itemName.isNotBlank() }
                            .map { ProductLink(it.itemName, it.productUrl) }

                        var finalImageUrl = imageUrl

                        // If user picked a photo, upload it to Cloud Storage
                        if (compressedBitmap != null) {
                            publishStatus = "Uploading photo..."
                            val uploadedUrl = withContext(Dispatchers.IO) {
                                storageRepository.uploadImage(compressedBitmap!!)
                            }
                            if (uploadedUrl == null) {
                                isPublishing = false
                                publishStatus = ""
                                Toast.makeText(context, "Upload failed. Try again.", Toast.LENGTH_LONG).show()
                                return@launch
                            }
                            finalImageUrl = uploadedUrl
                        }

                        // Save to Firestore
                        publishStatus = "Publishing look..."
                        val success = withContext(Dispatchers.IO) {
                            repository.addOutfit(
                                imageUrl = finalImageUrl,
                                title = title,
                                description = description,
                                category = category,
                                productLinks = links
                            )
                        }
                        isPublishing = false
                        publishStatus = ""
                        storageRepository.resetState()

                        if (success) {
                            Toast.makeText(context, "🎉 Outfit published!", Toast.LENGTH_SHORT).show()
                            onOutfitAdded()
                        } else {
                            Toast.makeText(context, "Failed to publish. Try again.", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                enabled = !isPublishing
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(listOf(RoseGold, CoralAccent)),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPublishing) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = publishStatus.ifBlank { "Publishing..." },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    } else {
                        Text(
                            text = "Publish Outfit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
