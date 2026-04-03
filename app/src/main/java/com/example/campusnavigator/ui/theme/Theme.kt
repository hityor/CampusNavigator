package com.example.campusnavigator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = NavyLight,
    onPrimaryContainer = Color.White,

    secondary = GreenAccent,
    onSecondary = Color.White,
    secondaryContainer = GreenLight,
    onSecondaryContainer = GreenDark,

    background = BackgroundGray,
    onBackground = NavyPrimary,

    surface = SurfaceWhite,
    onSurface = NavyPrimary,
    surfaceVariant = DividerGray,
    onSurfaceVariant = TextSecondary,

    outline = DividerGray
)

@Composable
fun CampusNavigatorTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}