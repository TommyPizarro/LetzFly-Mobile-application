package com.gamecodeschool.letzfly.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Default Light Scheme: Sky Blue + White + Soft Mint Tints
private val LightColorScheme = lightColorScheme(
    primary = SkyBluePrimary,
    onPrimary = SkyLightSurface,
    secondary = SoftSkySecondary,
    onSecondary = SkyLightOnSurface,
    tertiary = MintMossAccent,
    background = SkyLightBackground,
    onBackground = SkyLightOnSurface,
    surface = SkyLightSurface,
    onSurface = SkyLightOnSurface,
    surfaceVariant = MintMossAccent.copy(alpha = 0.25f),
    onSurfaceVariant = SkyLightOnSurface
)

// Dark Scheme: Deep Forest Dark Green + Bright Mint
private val DarkColorScheme = darkColorScheme(
    primary = MintMossPrimary,
    onPrimary = DeepForestDark,
    secondary = MintSecondary,
    onSecondary = DeepForestDark,
    tertiary = VibrantMintAccent,
    background = MintDarkBackground,
    onBackground = MintDarkOnSurface,
    surface = MintDarkSurface,
    onSurface = MintDarkOnSurface,
    surfaceVariant = MintDarkSurface,
    onSurfaceVariant = MintDarkOnSurface
)

@Composable
fun LetzFlyTheme(
    darkTheme: Boolean = false, // Defaults strictly to Light Theme regardless of system settings
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}