package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = ForestEmerald,
    onPrimary = Color.White,
    primaryContainer = SoftMintSurface,
    onPrimaryContainer = ForestEmerald,
    secondary = PalayGold,
    onSecondary = Color.White,
    secondaryContainer = SunlitAmber,
    onSecondaryContainer = DeepAmberText,
    tertiary = TerracottaEarth,
    onTertiary = Color.White,
    tertiaryContainer = TerracottaContainer,
    onTertiaryContainer = DeepTerracottaText,
    background = RiceCreamBackground,
    onBackground = TextPrimaryLight,
    surface = Color.White,
    onSurface = TextPrimaryLight,
    surfaceVariant = CardSageContainer,
    onSurfaceVariant = TextSecondaryLight,
    surfaceContainerLow = CardSageContainer,
    surfaceContainer = CardSageContainer,
    surfaceContainerHigh = CardSageHigh,
    outline = OutlineSage,
    error = Color(0xFFC62828),
    onError = Color.White,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFF7F0000)
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkMintAccent,
    onPrimary = Color(0xFF0A2E10),
    primaryContainer = Color(0xFF1F4D26),
    onPrimaryContainer = SoftMintSurface,
    secondary = DarkGoldAccent,
    onSecondary = Color(0xFF3E2700),
    secondaryContainer = Color(0xFF563900),
    onSecondaryContainer = SunlitAmber,
    tertiary = DarkTerracottaAccent,
    onTertiary = Color(0xFF471404),
    tertiaryContainer = Color(0xFF6B240F),
    onTertiaryContainer = TerracottaContainer,
    background = DeepSoilDark,
    onBackground = TextPrimaryDark,
    surface = DarkLeafSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkCardContainer,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainerLow = DarkLeafSurface,
    surfaceContainer = DarkCardContainer,
    surfaceContainerHigh = Color(0xFF28422E),
    outline = OutlineDarkSage,
    error = Color(0xFFEF9A9A),
    onError = Color(0xFF5F0000),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFFDAD6)
)

val AgriShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun AgriQuestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AgriQuestTypography,
        shapes = AgriShapes,
        content = content
    )
}
