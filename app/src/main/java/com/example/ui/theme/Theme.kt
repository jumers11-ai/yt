package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PremiumCrimson,
    onPrimary = Color.White,
    secondary = CharcoalGray,
    onSecondary = TextLight,
    background = ObsidianBlack,
    onBackground = TextLight,
    surface = DeepCharcoal,
    onSurface = TextLight,
    surfaceVariant = CharcoalGray,
    onSurfaceVariant = TextGray,
    outline = TextGray
)

private val LightColorScheme = lightColorScheme(
    primary = PremiumCrimson,
    onPrimary = Color.White,
    secondary = CleanGray,
    onSecondary = TextDark,
    background = SnowyWhite,
    onBackground = TextDark,
    surface = PureWhite,
    onSurface = TextDark,
    surfaceVariant = CleanGray,
    onSurfaceVariant = SlateGray,
    outline = SlateGray
)

@Composable
fun KazikPlayerTheme(
    darkTheme: Boolean = true, // Default to dark theme!
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                
                val windowInsetsController = WindowCompat.getInsetsController(window, view)
                windowInsetsController.isAppearanceLightStatusBars = !darkTheme
                windowInsetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Keep original name as wrapper to ensure perfect compile compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    KazikPlayerTheme(darkTheme = darkTheme, content = content)
}
