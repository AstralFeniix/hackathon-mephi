package ru.mephi.tikho.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp

/** Bloom: 18 / 10 / 6, мягкая бумага. Lumen: 10 / 6, инженерная линейка. */
private fun tikhoShapes(dark: Boolean): Shapes = if (dark) {
    Shapes(
        extraSmall = RoundedCornerShape(6.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(10.dp),
        large = RoundedCornerShape(12.dp),
        extraLarge = RoundedCornerShape(20.dp),
    )
} else {
    Shapes(
        extraSmall = RoundedCornerShape(6.dp),
        small = RoundedCornerShape(10.dp),
        medium = RoundedCornerShape(14.dp),
        large = RoundedCornerShape(18.dp),
        extraLarge = RoundedCornerShape(24.dp),
    )
}

@Composable
fun TikhoTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val palette = if (darkTheme) DarkPalette else LightPalette
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.primary,
            onPrimary = palette.onPrimary,
            primaryContainer = palette.primarySoft,
            onPrimaryContainer = palette.textPrimary,
            secondary = palette.primaryMuted,
            onSecondary = palette.textPrimary,
            secondaryContainer = palette.surfaceAlt,
            onSecondaryContainer = palette.textPrimary,
            background = palette.background,
            onBackground = palette.textPrimary,
            surface = palette.surface,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.iconTile,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.outline,
            outlineVariant = palette.divider,
        )
    } else {
        lightColorScheme(
            primary = palette.primary,
            onPrimary = palette.onPrimary,
            primaryContainer = palette.primarySoft,
            onPrimaryContainer = palette.textPrimary,
            secondary = palette.primaryMuted,
            onSecondary = palette.onPrimary,
            secondaryContainer = palette.surfaceAlt,
            onSecondaryContainer = palette.textPrimary,
            background = palette.background,
            onBackground = palette.textPrimary,
            surface = palette.surface,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.iconTile,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.outline,
            outlineVariant = palette.divider,
        )
    }

    CompositionLocalProvider(LocalTikhoPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = tikhoTypography(),
            shapes = tikhoShapes(darkTheme),
        ) {
            CompositionLocalProvider(LocalContentColor provides palette.textPrimary) {
                content()
            }
        }
    }
}
