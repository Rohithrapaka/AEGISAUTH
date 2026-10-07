package com.aegisauth.core.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AegisCyan,
    onPrimary = AegisDarkBackground,
    primaryContainer = AegisDarkSurfaceElevated,
    onPrimaryContainer = AegisCyanLight,
    secondary = AegisCyanLight,
    onSecondary = AegisDarkBackground,
    secondaryContainer = AegisDarkSurfaceVariant,
    onSecondaryContainer = AegisTextPrimary,
    tertiary = AegisNothingDot,
    onTertiary = AegisDarkBackground,
    background = AegisDarkBackground,
    onBackground = AegisTextPrimary,
    surface = AegisDarkSurface,
    onSurface = AegisTextPrimary,
    surfaceVariant = AegisDarkSurfaceVariant,
    onSurfaceVariant = AegisTextSecondary,
    outline = AegisBorder,
    outlineVariant = AegisBorderActive,
    error = AegisCritical,
    onError = AegisDarkBackground
)

@Composable
fun AegisTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AegisTypography,
        shapes = AegisShapes,
        content = content
    )
}
