package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Sculpted3DLightColorScheme = lightColorScheme(
    primary = AccentGold,
    onPrimary = Color.White,
    primaryContainer = Surface2,
    onPrimaryContainer = AccentGold,
    secondary = TextSecondary,
    onSecondary = SoftLightBackground,
    secondaryContainer = Surface1,
    onSecondaryContainer = TextPrimary,
    tertiary = TextTertiary,
    onTertiary = TextPrimary,
    background = SoftLightBackground,
    onBackground = TextPrimary,
    surface = Surface1,
    onSurface = TextPrimary,
    surfaceVariant = Surface2,
    onSurfaceVariant = TextSecondary,
    outline = HairlineColor,
    outlineVariant = SurfaceHighlight,
    error = UrgencyCoral,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = Sculpted3DLightColorScheme,
        typography = Typography,
        content = content
    )
}
