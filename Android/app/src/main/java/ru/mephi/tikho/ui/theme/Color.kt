package ru.mephi.tikho.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Две темы приложения, обе взяты из библиотеки Hallmark:
 *
 *  - светлая — **Bloom**: тёплая бумага, терракота, мягкие поднятые поверхности,
 *    скругления 18/10/6 и тихая сетка «пиано-ролла»;
 *  - тёмная — **Lumen**: фиолетово-чёрная ночь, костяные чернила, латунный сигнал,
 *    тонкие волосяные линии и чертёжная сетка.
 *
 * Имена полей оставлены прежними, чтобы экраны и компоненты не пришлось
 * переписывать: меняется только значение токена.
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
    /** Светлый блик на верхней кромке поверхности: бумага в Bloom, 6% белого в Lumen. */
    val highlight: Color,
    /** Второй акцент: тёплая охра в Bloom, роза в Lumen (только для одного глагола). */
    val accentSecondary: Color,
    /** Линия сетки: «пиано-ролл» в Bloom, blueprint в Lumen. */
    val gridLine: Color,
)

val LightPalette = TikhoPalette(
    isDark = false,
    background = Color(0xFFF9F4EE),
    backgroundGradient = listOf(Color(0xFFFBF7F2), Color(0xFFF6EFE7)),
    surface = Color(0xFFFDFAF6),
    surfaceAlt = Color(0xFFF3ECE4),
    surfaceGradient = listOf(Color(0xFFFFFCF8), Color(0xFFFBF4EC)),
    primary = Color(0xFFAF4529),
    primaryPressed = Color(0xFF872E21),
    onPrimary = Color(0xFFFEF9F4),
    primarySoft = Color(0xFFF2E1D8),
    primarySoftAlt = Color(0xFFECD5CB),
    primarySoftGradient = listOf(Color(0xFFF7EAE1), Color(0xFFEFDCD1)),
    primaryMuted = Color(0xFF8E5A43),
    primaryGradient = listOf(Color(0xFFC25A39), Color(0xFFAF4529), Color(0xFF9C3B22)),
    progressGradient = listOf(Color(0xFFAF4529), Color(0xFFC97E5A)),
    textPrimary = Color(0xFF221510),
    textSecondary = Color(0xFF3A2D27),
    textTertiary = Color(0xFF6A5E56),
    iconTile = Color(0xFFEDE5DD),
    iconTileAlt = Color(0xFFEFE8E1),
    iconTileGradient = listOf(Color(0xFFF6F0E9), Color(0xFFEAE0D6)),
    divider = Color(0xFFDED6CE),
    outline = Color(0xFFD3C8BE),
    progressTrack = Color(0xFFEBE1D8),
    waveform = Color(0xFFD9C9BC),
    waveformPlayed = Color(0xFFAF4529),
    switchGradient = listOf(Color(0xFFD17652), Color(0xFFAF4529)),
    shadow = Color(0x2E221510),
    glowA = Color(0xFFAF4529),
    glowB = Color(0xFFC18154),
    glowC = Color(0xFFC18154),
    pattern = Color(0xFF221510),
    highlight = Color(0xE6FFFFFF),
    accentSecondary = Color(0xFFC18154),
    gridLine = Color(0x12221510),
)

val DarkPalette = TikhoPalette(
    isDark = true,
    background = Color(0xFF060510),
    backgroundGradient = listOf(Color(0xFF080613), Color(0xFF05040E)),
    surface = Color(0xFF0D0B19),
    surfaceAlt = Color(0xFF161324),
    surfaceGradient = listOf(Color(0xFF0F0D1C), Color(0xFF0A0816)),
    primary = Color(0xFFFFA54B),
    primaryPressed = Color(0xFFE08D35),
    onPrimary = Color(0xFF090714),
    primarySoft = Color(0xFF2A1D12),
    primarySoftAlt = Color(0xFF352314),
    primarySoftGradient = listOf(Color(0xFF2A1E14), Color(0xFF1B1510)),
    primaryMuted = Color(0xFFC9A275),
    primaryGradient = listOf(Color(0xFFFFB25E), Color(0xFFF09A3E)),
    progressGradient = listOf(Color(0xFFFFA54B), Color(0xFFE58C34)),
    textPrimary = Color(0xFFF3F0F8),
    textSecondary = Color(0xFFCFCCD8),
    textTertiary = Color(0xFF9C98AA),
    iconTile = Color(0xFF161324),
    iconTileAlt = Color(0xFF1D192C),
    iconTileGradient = listOf(Color(0xFF1A1628), Color(0xFF100D1D)),
    divider = Color(0xFF262436),
    outline = Color(0xFF3D3951),
    progressTrack = Color(0xFF1D1A2B),
    waveform = Color(0xFF34304A),
    waveformPlayed = Color(0xFFFFA54B),
    switchGradient = listOf(Color(0xFF2A2740), Color(0xFF3D3951)),
    shadow = Color(0x99000000),
    glowA = Color(0xFF0D0B19),
    glowB = Color(0xFF0D0B19),
    glowC = Color(0xFF0D0B19),
    pattern = Color(0xFFF3F0F8),
    highlight = Color(0x14FFFFFF),
    accentSecondary = Color(0xFFF3798B),
    gridLine = Color(0x14F3F0F8),
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

    val Highlight: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalTikhoPalette.current.highlight

    val AccentSecondary: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalTikhoPalette.current.accentSecondary

    val GridLine: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalTikhoPalette.current.gridLine

    val IsDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalTikhoPalette.current.isDark
}
