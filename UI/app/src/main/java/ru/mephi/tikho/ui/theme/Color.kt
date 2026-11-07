package ru.mephi.tikho.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Палитра приложения. Светлая тема построена вокруг лаванды (#E6E6FA),
 * тёмная — вокруг глубокого бирюзово-зелёного (#1B796E).
 *
 * В обеих темах используются многоцветные градиенты, поэтому фон и карточки
 * не выглядят монотонными.
 */
@Immutable
data class TikhoPalette(
    val isDark: Boolean,
    val background: Color,
    val backgroundGradient: List<Color>,
    val surface: Color,
    val surfaceAlt: Color,
    val surfaceGradient: List<Color>,
    val primary: Color,
    val primaryPressed: Color,
    val onPrimary: Color,
    val primarySoft: Color,
    val primarySoftAlt: Color,
    val primarySoftGradient: List<Color>,
    val primaryMuted: Color,
    val primaryGradient: List<Color>,
    val progressGradient: List<Color>,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val iconTile: Color,
    val iconTileAlt: Color,
    val iconTileGradient: List<Color>,
    val divider: Color,
    val outline: Color,
    val progressTrack: Color,
    val waveform: Color,
    val waveformPlayed: Color,
    val switchGradient: List<Color>,
    val shadow: Color,
    val glowA: Color,
    val glowB: Color,
    val glowC: Color,
    val pattern: Color,
)

val LightPalette = TikhoPalette(
    isDark = false,
    background = Color(0xFFF2EDFF),
    backgroundGradient = listOf(
        Color(0xFFF8F4FF),
        Color(0xFFE9E4FA),
        Color(0xFFFDF8FF),
        Color(0xFFEDF3FF),
    ),
    surface = Color(0xFAFFFFFF),
    surfaceAlt = Color(0xFFF7F1FF),
    surfaceGradient = listOf(Color(0xFFFFFFFF), Color(0xFFF8F2FF)),
    primary = Color(0xFF6C5CE7),
    primaryPressed = Color(0xFF5847D3),
    onPrimary = Color(0xFFFFFFFF),
    primarySoft = Color(0xFFEEE8FF),
    primarySoftAlt = Color(0xFFE1D6FF),
    primarySoftGradient = listOf(Color(0xFFF2ECFF), Color(0xFFE2D7FF)),
    primaryMuted = Color(0xFF8C7BD9),
    primaryGradient = listOf(Color(0xFF5B49D6), Color(0xFF6C5CE7), Color(0xFF7D62DF)),
    progressGradient = listOf(Color(0xFF6C5CE7), Color(0xFF9D7CF1)),
    textPrimary = Color(0xFF241E38),
    textSecondary = Color(0xFF655E7A),
    textTertiary = Color(0xFF8D86A5),
    iconTile = Color(0xFFF2ECFF),
    iconTileAlt = Color(0xFFE7DDFF),
    iconTileGradient = listOf(Color(0xFFF6F1FF), Color(0xFFE8DEFF)),
    divider = Color(0xFFECE5F9),
    outline = Color(0xFFE2D8F4),
    progressTrack = Color(0xFFE7E0F6),
    waveform = Color(0xFFD7CFF0),
    waveformPlayed = Color(0xFF8A75E8),
    switchGradient = listOf(Color(0xFFD4C8FF), Color(0xFF8066E9)),
    shadow = Color(0x336C5CE7),
    glowA = Color(0xFFCDB8FF),
    glowB = Color(0xFFFFD8EC),
    glowC = Color(0xFFC6E6FF),
    pattern = Color(0xFF7A68C9),
)

val DarkPalette = TikhoPalette(
    isDark = true,
    background = Color(0xFF071F1B),
    backgroundGradient = listOf(
        Color(0xFF061A17),
        Color(0xFF0A2B25),
        Color(0xFF08231F),
        Color(0xFF0C332D),
    ),
    surface = Color(0xF20D2F2A),
    surfaceAlt = Color(0xFF123B35),
    surfaceGradient = listOf(Color(0xFF103630), Color(0xFF0A2823)),
    primary = Color(0xFF5BD8C4),
    primaryPressed = Color(0xFF47C3B0),
    onPrimary = Color(0xFFFFFFFF),
    primarySoft = Color(0xFF123F38),
    primarySoftAlt = Color(0xFF185148),
    primarySoftGradient = listOf(Color(0xFF174A42), Color(0xFF0F3832)),
    primaryMuted = Color(0xFF8FCEC3),
    primaryGradient = listOf(Color(0xFF0E574E), Color(0xFF1B796E), Color(0xFF20897B)),
    progressGradient = listOf(Color(0xFF1B796E), Color(0xFF4FDCC4)),
    textPrimary = Color(0xFFECFAF5),
    textSecondary = Color(0xFFA7CBC3),
    textTertiary = Color(0xFF7DA8A0),
    iconTile = Color(0xFF123B35),
    iconTileAlt = Color(0xFF184A42),
    iconTileGradient = listOf(Color(0xFF17473F), Color(0xFF0F322C)),
    divider = Color(0xFF1C4A43),
    outline = Color(0xFF275B53),
    progressTrack = Color(0xFF17453E),
    waveform = Color(0xFF2C5A53),
    waveformPlayed = Color(0xFF4DC7B4),
    switchGradient = listOf(Color(0xFF1B796E), Color(0xFF35B7A5)),
    shadow = Color(0x66000000),
    glowA = Color(0xFF1B796E),
    glowB = Color(0xFF0D5F6B),
    glowC = Color(0xFF2A8C7B),
    pattern = Color(0xFF9CE8DB),
)

val LocalTikhoPalette = staticCompositionLocalOf { LightPalette }

/**
 * Удобные имена цветов для UI. Значения берутся из CompositionLocal,
 * поэтому одни и те же компоненты автоматически перекрашиваются при смене темы.
 */
object TikhoColors {
    val Background: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalTikhoPalette.current.background

    val Surface: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalTikhoPalette.current.surface

    val SurfaceGradient: List<Color>
        @Composable
        @ReadOnlyComposable
        get() = LocalTikhoPalette.current.surfaceGradient

    val Primary: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalTikhoPalette.current.primary

    val PrimaryPressed: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalTikhoPalette.current.primaryPressed

    val OnPrimary: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalTikhoPalette.current.onPrimary

    val PrimarySoft: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalTikhoPalette.current.primarySoft

    val PrimarySoftGradient: List<Color>
        @Composable
        @ReadOnlyComposable
        get() = LocalTikhoPalette.current.primarySoftGradient

    val PrimaryMuted: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalTikhoPalette.current.primaryMuted

    val PrimaryGradient: List<Color>
        @Composable
        @ReadOnlyComposable
        get() = LocalTikhoPalette.current.primaryGradient

    val ProgressGradient: List<Color>
        @Composable
        @ReadOnlyComposable
        get() = LocalTikhoPalette.current.progressGradient

    val TextPrimary: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalTikhoPalette.current.textPrimary

    val TextSecondary: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalTikhoPalette.current.textSecondary

    val TextTertiary: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalTikhoPalette.current.textTertiary

    val IconTile: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalTikhoPalette.current.iconTile

    val IconTileGradient: List<Color>
        @Composable
        @ReadOnlyComposable
        get() = LocalTikhoPalette.current.iconTileGradient

    val Divider: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalTikhoPalette.current.divider

    val Outline: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalTikhoPalette.current.outline

    val ProgressTrack: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalTikhoPalette.current.progressTrack

    val Waveform: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalTikhoPalette.current.waveform

    val WaveformPlayed: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalTikhoPalette.current.waveformPlayed

    val SwitchGradient: List<Color>
        @Composable
        @ReadOnlyComposable
        get() = LocalTikhoPalette.current.switchGradient

    val Shadow: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalTikhoPalette.current.shadow
}
