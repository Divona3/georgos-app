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
    primary = ForestGreen,
    onPrimary = OffWhite,
    primaryContainer = MintMist,
    onPrimaryContainer = DeepCanopyGreen,
    secondary = EarthBrown,
    onSecondary = OffWhite,
    secondaryContainer = SoftBeige,
    onSecondaryContainer = LoamBrown,
    tertiary = AmberHarvest,
    onTertiary = OffWhite,
    tertiaryContainer = CreamSoil,
    onTertiaryContainer = BarkBrown,
    background = OffWhite,
    onBackground = BarkBrown,
    surface = WarmLinen,
    onSurface = BarkBrown,
    surfaceVariant = SoftBeige,
    onSurfaceVariant = LoamBrown,
    surfaceContainerLowest = OffWhite,
    surfaceContainerLow = WarmLinen,
    surfaceContainer = SoftBeige,
    surfaceContainerHigh = SandstoneBeige,
    outline = SandstoneBeige,
    outlineVariant = DeepCanopyGreen.copy(alpha = 0.14f),
    error = AlertTerracotta,
    onError = OffWhite
)

private val DarkColorScheme = darkColorScheme(
    primary = SageLeafLight,
    onPrimary = DeepCanopyGreen,
    primaryContainer = ForestGreen,
    onPrimaryContainer = MintMist,
    secondary = WarmTanAccent,
    onSecondary = BarkBrown,
    secondaryContainer = EarthBrown,
    onSecondaryContainer = WarmBeigeText,
    tertiary = AmberHarvest,
    onTertiary = BarkBrown,
    background = DarkForestBg,
    onBackground = WarmBeigeText,
    surface = DarkLoamSurface,
    onSurface = WarmBeigeText,
    surfaceVariant = DarkBeigeCard,
    onSurfaceVariant = WarmTanAccent,
    surfaceContainerLowest = DarkForestBg,
    surfaceContainerLow = DarkLoamSurface,
    surfaceContainer = DarkBeigeCard,
    outline = EarthBrown,
    outlineVariant = Color(0xFF3A4435),
    error = Color(0xFFE07A68),
    onError = BarkBrown
)

val GrievousShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun GrievousTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GrievousTypography,
        shapes = GrievousShapes,
        content = content
    )
}
