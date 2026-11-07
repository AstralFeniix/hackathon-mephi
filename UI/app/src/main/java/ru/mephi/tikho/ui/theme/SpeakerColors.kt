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

private val LightSpeakerStyles = listOf(
    SpeakerStyle(
        accent = Color(0xFF6C5CE7),
        soft = Color(0xFFEEE8FF),
        onSoft = Color(0xFF3A2B78),
    ),
    SpeakerStyle(
        accent = Color(0xFFC56A45),
        soft = Color(0xFFFFE8DE),
        onSoft = Color(0xFF6E2F1B),
    ),
    SpeakerStyle(
        accent = Color(0xFF2D8F7C),
        soft = Color(0xFFDFF5ED),
        onSoft = Color(0xFF1B4C42),
    ),
    SpeakerStyle(
        accent = Color(0xFF3F72C8),
        soft = Color(0xFFE3EDFF),
        onSoft = Color(0xFF1E3B6B),
    ),
)

private val DarkSpeakerStyles = listOf(
    SpeakerStyle(
        accent = Color(0xFFA896FF),
        soft = Color(0xFF2A2350),
        onSoft = Color(0xFFE7E1FF),
    ),
    SpeakerStyle(
        accent = Color(0xFFF2A36F),
        soft = Color(0xFF3D2A1E),
        onSoft = Color(0xFFFFE7D6),
    ),
    SpeakerStyle(
        accent = Color(0xFF5BD8C4),
        soft = Color(0xFF123B35),
        onSoft = Color(0xFFD6FFF8),
    ),
    SpeakerStyle(
        accent = Color(0xFF87A9FF),
        soft = Color(0xFF1B2C49),
        onSoft = Color(0xFFDEE9FF),
    ),
)

@Composable
fun speakerStyle(index: Int): SpeakerStyle {
    val styles = if (LocalTikhoPalette.current.isDark) DarkSpeakerStyles else LightSpeakerStyles
    return styles[index.mod(styles.size)]
}
