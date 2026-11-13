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

private val BazarLightColorScheme = lightColorScheme(
    primary = ZaatarEmerald,
    onPrimary = Color.White,
    primaryContainer = ChampagneGoldSoft,
    onPrimaryContainer = ZaatarEmerald,
    secondary = ChampagneGold,
    onSecondary = EspressoCharcoal,
    secondaryContainer = SoftTravertine,
    onSecondaryContainer = EspressoCharcoal,
    tertiary = TerracottaBronze,
    onTertiary = Color.White,
    background = WarmIvory,
    onBackground = EspressoCharcoal,
    surface = WarmIvory,
    onSurface = EspressoCharcoal,
    surfaceVariant = SoftTravertine,
    onSurfaceVariant = MutedOliveGrey,
    outline = WarmSandBorder,
    outlineVariant = WarmSandBorder,
    error = CrimsonSale,
    onError = Color.White,
    errorContainer = CrimsonSaleContainer,
    onErrorContainer = CrimsonSale
)

private val BazarDarkColorScheme = darkColorScheme(
    primary = DarkSagePrimary,
    onPrimary = DarkObsidianOlive,
    primaryContainer = DarkSurfaceElevated,
    onPrimaryContainer = DarkSagePrimary,
    secondary = DarkGoldAccent,
    onSecondary = DarkObsidianOlive,
    secondaryContainer = DarkSurfaceCard,
    onSecondaryContainer = DarkIvoryText,
    tertiary = TerracottaBronze,
    onTertiary = Color.White,
    background = DarkObsidianOlive,
    onBackground = DarkIvoryText,
    surface = DarkSurfaceCard,
    onSurface = DarkIvoryText,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = Color(0xFFB7C0B9),
    outline = Color(0xFF334239),
    outlineVariant = Color(0xFF28352D),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410)
)

val BazarShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun BazarZaatarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) BazarDarkColorScheme else BazarLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = BazarZaatarTypography,
        shapes = BazarShapes,
        content = content
    )
}
