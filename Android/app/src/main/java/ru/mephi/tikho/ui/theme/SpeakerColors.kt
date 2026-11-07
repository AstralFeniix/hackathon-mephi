package ru.mephi.tikho.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class SpeakerStyle(
    val accent: Color,
    val soft: Color,
    val onSoft: Color,
)

/**
 * Цвета говорящих — те же четыре дорожки, что и в теме:
 * Bloom берёт умбру, терракоту, охру и розу; Lumen — латунь, розу,
 * живой зелёный и костяной серый.
 */
private val LightSpeakerStyles = listOf(
    SpeakerStyle(
        accent = Color(0xFF765E4E),
        soft = Color(0xFFE9DED6),
        onSoft = Color(0xFF4A392E),
    ),
    SpeakerStyle(
        accent = Color(0xFF8F433B),
        soft = Color(0xFFEBD9D4),
        onSoft = Color(0xFF5C241F),
    ),
    SpeakerStyle(
        accent = Color(0xFFCA9246),
        soft = Color(0xFFF3E5D2),
        onSoft = Color(0xFF6B4616),
    ),
    SpeakerStyle(
        accent = Color(0xFFBD7981),
        soft = Color(0xFFF0DEE0),
        onSoft = Color(0xFF6E3740),
    ),
)

private val DarkSpeakerStyles = listOf(
    SpeakerStyle(
        accent = Color(0xFFFFA54B),
        soft = Color(0xFF2A1D12),
        onSoft = Color(0xFFFFD8AE),
    ),
    SpeakerStyle(
        accent = Color(0xFFF3798B),
        soft = Color(0xFF2E1620),
        onSoft = Color(0xFFFFCBD3),
    ),
    SpeakerStyle(
        accent = Color(0xFF72D699),
        soft = Color(0xFF10251B),
        onSoft = Color(0xFFB7F2CC),
    ),
    SpeakerStyle(
        accent = Color(0xFFCFCCD8),
        soft = Color(0xFF211E30),
        onSoft = Color(0xFFE6E2F0),
    ),
)

@Composable
fun speakerStyle(index: Int): SpeakerStyle {
    val styles = if (LocalTikhoPalette.current.isDark) DarkSpeakerStyles else LightSpeakerStyles
    return styles[index.mod(styles.size)]
}
