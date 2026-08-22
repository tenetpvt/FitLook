package com.example.fitlook.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.fitlook.data.model.ProductLink
import com.example.fitlook.data.repository.GeminiService
import com.example.fitlook.data.repository.OutfitRepository
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
    var imageUrl by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Casual") }
    val productLinks = remember { mutableStateListOf(ProductLinkInput()) }
    var isGenerating by remember { mutableStateOf(false) }
    var isPublishing by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repository = remember { OutfitRepository() }

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
            // ─── Image URL ───
            Text(
                text = "Outfit Image",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            OutlinedTextField(
                value = imageUrl,
                onValueChange = {
                    imageUrl = it
                    showPreview = it.startsWith("http")
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Paste image URL") },
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors,
                singleLine = true
            )

            // Image Preview
            AnimatedVisibility(visible = showPreview && imageUrl.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Outfit preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // ─── AI Generate Button ───
            ElevatedButton(
                onClick = {
                    if (imageUrl.isBlank()) {
                        Toast.makeText(context, "Paste an image URL first!", Toast.LENGTH_SHORT).show()
                        return@ElevatedButton
                    }
                    isGenerating = true
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            GeminiService.generateOutfitInfo(imageUrl)
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
                enabled = !isGenerating,
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
                        "Generating with Gemini AI...",
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
                    if (imageUrl.isBlank() || title.isBlank()) {
                        Toast.makeText(context, "Image URL and Title are required!", Toast.LENGTH_SHORT).show()
                        return@ElevatedButton
                    }
                    isPublishing = true
                    scope.launch {
                        val links = productLinks
                            .filter { it.itemName.isNotBlank() }
                            .map { ProductLink(it.itemName, it.productUrl) }

                        val success = withContext(Dispatchers.IO) {
                            repository.addOutfit(
                                imageUrl = imageUrl,
                                title = title,
                                description = description,
                                category = category,
                                productLinks = links
                            )
                        }
                        isPublishing = false
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
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
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
