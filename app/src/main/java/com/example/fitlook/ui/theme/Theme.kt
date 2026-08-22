package com.example.fitlook.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FitLookColorScheme = darkColorScheme(
    primary = RoseGold,
    secondary = CoralAccent,
    tertiary = SoftPink,
    background = DeepBlack,
    surface = DarkSurface,
    surfaceVariant = DarkCard,
    onPrimary = DeepBlack,
    onSecondary = White,
    onTertiary = DeepBlack,
    onBackground = White,
    onSurface = OffWhite,
    onSurfaceVariant = LightGray
)

@Composable
fun FitLookTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = FitLookColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DeepBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}