package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Always Clean, Bright, Whitish Palette inspired by Pinterest Reference
private val AppLightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = PrimaryBlue,
    secondary = SecondarySlate,
    onSecondary = Color.White,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = SecondarySlate,
    tertiary = AccentGreen,
    onTertiary = Color.White,
    tertiaryContainer = AccentGreenLight,
    onTertiaryContainer = OnAccentGreen,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = AppSurface,
    onSurface = TextPrimary,
    surfaceVariant = AppSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderVerySubtle,
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun TuitionManagerTheme(
    darkTheme: Boolean = false, // Always enforce bright, white/whitish theme as requested
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.White.toArgb()
            window.decorView.setBackgroundColor(android.graphics.Color.WHITE)
            // Ensure dark status bar and nav bar icons on light backgrounds
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = true
            insetsController.isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = AppLightColorScheme,
        typography = Typography,
        content = content
    )
}
