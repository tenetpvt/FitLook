package com.example.fitlook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.fitlook.data.repository.LikedOutfitsManager
import com.example.fitlook.navigation.FitLookNavGraph
import com.example.fitlook.ui.theme.FitLookTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LikedOutfitsManager.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            FitLookTheme {
                FitLookNavGraph()
            }
        }
    }
}