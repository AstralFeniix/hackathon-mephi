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

private val TikhoShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

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
            onPrimaryContainer = palette.primary,
            secondary = palette.primaryMuted,
            onSecondary = palette.textPrimary,
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
            onPrimaryContainer = palette.primary,
            secondary = palette.primaryMuted,
            onSecondary = palette.onPrimary,
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
            shapes = TikhoShapes,
        ) {
            CompositionLocalProvider(LocalContentColor provides palette.textPrimary) {
                content()
            }
        }
    }
}
