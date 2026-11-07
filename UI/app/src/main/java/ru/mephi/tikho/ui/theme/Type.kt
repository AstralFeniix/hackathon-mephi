package ru.mephi.tikho.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val sans = FontFamily.SansSerif

/**
 * Текстовые стили макета. Цвета берутся из активной палитры, поэтому
 * типографика автоматически подстраивается под светлую и тёмную тему.
 */
object TikhoType {
    val appTitle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Bold,
            fontSize = 21.sp,
            lineHeight = 26.sp,
            letterSpacing = (-0.3).sp,
            color = TikhoColors.TextPrimary,
        )

    val screenTitle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            lineHeight = 40.sp,
            letterSpacing = (-1.1).sp,
            color = TikhoColors.TextPrimary,
        )

    val screenTitleLarge: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Bold,
            fontSize = 41.sp,
            lineHeight = 47.sp,
            letterSpacing = (-1.6).sp,
            color = TikhoColors.TextPrimary,
        )

    val screenTitleCompact: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            letterSpacing = (-1.0).sp,
            color = TikhoColors.TextPrimary,
        )

    val cardTitle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = (-0.3).sp,
            color = TikhoColors.TextPrimary,
        )

    val rowTitle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 21.sp,
            letterSpacing = (-0.1).sp,
            color = TikhoColors.TextPrimary,
        )

    val body: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 21.sp,
            color = TikhoColors.TextSecondary,
        )

    val transcript: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Normal,
            fontSize = 15.5.sp,
            lineHeight = 23.sp,
            color = TikhoColors.TextPrimary,
        )

    val caption: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = TikhoColors.TextTertiary,
        )

    val label: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            color = TikhoColors.TextPrimary,
        )

    val button: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.sp,
            color = TikhoColors.OnPrimary,
        )

    val navTitle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Medium,
            fontSize = 17.sp,
            lineHeight = 22.sp,
            letterSpacing = (-0.2).sp,
            color = TikhoColors.TextPrimary,
        )

    val bigNumber: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Bold,
            fontSize = 42.sp,
            lineHeight = 46.sp,
            letterSpacing = (-1.5).sp,
            color = TikhoColors.Primary,
        )
}

@Composable
fun tikhoTypography(): Typography = Typography(
    displayLarge = TikhoType.screenTitle,
    headlineMedium = TikhoType.screenTitleCompact,
    titleLarge = TikhoType.cardTitle,
    titleMedium = TikhoType.rowTitle,
    bodyLarge = TikhoType.body,
    bodyMedium = TikhoType.body,
    labelLarge = TikhoType.button,
    labelMedium = TikhoType.caption,
)
