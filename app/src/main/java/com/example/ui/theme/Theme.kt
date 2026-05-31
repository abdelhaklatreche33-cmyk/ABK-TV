package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CosmicDarkColorScheme = darkColorScheme(
    primary = BrightPremiumRed,
    secondary = DeepBurgundy,
    tertiary = AmberGold,
    background = DarkVibeBg,
    surface = CarbonCard,
    onPrimary = SmoothWhite,
    onSecondary = SmoothWhite,
    onBackground = SmoothWhite,
    onSurface = SmoothWhite,
    surfaceVariant = CarbonCardLight
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CosmicDarkColorScheme,
        typography = Typography,
        content = content
    )
}
