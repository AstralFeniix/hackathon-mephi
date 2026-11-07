@file:OptIn(ExperimentalTextApi::class)

package ru.mephi.tikho.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ru.mephi.tikho.R

/**
 * Bloom: Onest вместо Ranade и Lora вместо Gambarino — обе гарнитуры
 * сохраняют тёплый гротеск и один римский serif-момент, но имеют кириллицу.
 * Lumen: Playfair Display вместо Instrument Serif, Geist и Geist Mono — как в теме.
 */
private val Onest = FontFamily(
    Font(
        R.font.onest,
        FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
    Font(
        R.font.onest,
        FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
    Font(
        R.font.onest,
        FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    ),
)

private val Lora = FontFamily(
    Font(
        R.font.lora,
        FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
    Font(
        R.font.lora,
        FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
)

private val Playfair = FontFamily(
    Font(
        R.font.playfair,
        FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
    Font(
        R.font.playfair,
        FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
)

private val Geist = FontFamily(
    Font(
        R.font.geist,
        FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
    Font(
        R.font.geist,
        FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
    Font(
        R.font.geist,
        FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    ),
)

private val GeistMono = FontFamily(
    Font(
        R.font.geist_mono,
        FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
    Font(
        R.font.geist_mono,
        FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
)

/**
 * Текстовые стили. Цвета и гарнитуры берутся из активной темы, поэтому
 * переключение света и ночи меняет не только палитру, но и голос интерфейса.
 */
object TikhoType {
    val appTitle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = if (LocalTikhoPalette.current.isDark) {
            TextStyle(
                fontFamily = Playfair,
                fontWeight = FontWeight.Normal,
                fontSize = 31.sp,
                lineHeight = 34.sp,
                letterSpacing = (-0.3).sp,
                color = TikhoColors.TextPrimary,
            )
        } else {
            TextStyle(
                fontFamily = Lora,
                fontWeight = FontWeight.Normal,
                fontSize = 30.sp,
                lineHeight = 34.sp,
                letterSpacing = 0.sp,
                color = TikhoColors.TextPrimary,
            )
        }

    val screenTitle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = if (LocalTikhoPalette.current.isDark) {
            TextStyle(
                fontFamily = Playfair,
                fontWeight = FontWeight.Normal,
                fontSize = 41.sp,
                lineHeight = 39.sp,
                letterSpacing = (-0.6).sp,
                color = TikhoColors.TextPrimary,
            )
        } else {
            TextStyle(
                fontFamily = Onest,
                fontWeight = FontWeight.Medium,
                fontSize = 35.sp,
                lineHeight = 37.sp,
                letterSpacing = (-0.9).sp,
                color = TikhoColors.TextPrimary,
            )
        }

    val screenTitleLarge: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = if (LocalTikhoPalette.current.isDark) {
            TextStyle(
                fontFamily = Playfair,
                fontWeight = FontWeight.Normal,
                fontSize = 47.sp,
                lineHeight = 45.sp,
                letterSpacing = (-0.8).sp,
                color = TikhoColors.TextPrimary,
            )
        } else {
            TextStyle(
                fontFamily = Onest,
                fontWeight = FontWeight.Medium,
                fontSize = 42.sp,
                lineHeight = 44.sp,
                letterSpacing = (-1.4).sp,
                color = TikhoColors.TextPrimary,
            )
        }

    val screenTitleCompact: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = if (LocalTikhoPalette.current.isDark) {
            TextStyle(
                fontFamily = Playfair,
                fontWeight = FontWeight.Normal,
                fontSize = 35.sp,
                lineHeight = 33.sp,
                letterSpacing = (-0.5).sp,
                color = TikhoColors.TextPrimary,
            )
        } else {
            TextStyle(
                fontFamily = Onest,
                fontWeight = FontWeight.Medium,
                fontSize = 32.sp,
                lineHeight = 34.sp,
                letterSpacing = (-0.9).sp,
                color = TikhoColors.TextPrimary,
            )
        }

    val cardTitle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = if (LocalTikhoPalette.current.isDark) {
            TextStyle(
                fontFamily = Geist,
                fontWeight = FontWeight.Medium,
                fontSize = 17.sp,
                lineHeight = 23.sp,
                letterSpacing = (-0.1).sp,
                color = TikhoColors.TextPrimary,
            )
        } else {
            TextStyle(
                fontFamily = Onest,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                lineHeight = 23.sp,
                letterSpacing = (-0.3).sp,
                color = TikhoColors.TextPrimary,
            )
        }

    val rowTitle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = TextStyle(
            fontFamily = bodyFamily(),
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
            fontFamily = bodyFamily(),
            fontWeight = FontWeight.Normal,
            fontSize = 15.5.sp,
            lineHeight = 22.sp,
            color = TikhoColors.TextSecondary,
        )

    val transcript: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = TextStyle(
            fontFamily = bodyFamily(),
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = TikhoColors.TextPrimary,
        )

    val caption: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = TextStyle(
            fontFamily = bodyFamily(),
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = TikhoColors.TextTertiary,
        )

    val label: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = TextStyle(
            fontFamily = bodyFamily(),
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            color = TikhoColors.TextPrimary,
        )

    val button: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = TextStyle(
            fontFamily = bodyFamily(),
            fontWeight = FontWeight.Medium,
            fontSize = 16.5.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.sp,
            color = TikhoColors.OnPrimary,
        )

    val navTitle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = if (LocalTikhoPalette.current.isDark) {
            TextStyle(
                fontFamily = Playfair,
                fontWeight = FontWeight.Normal,
                fontSize = 21.sp,
                lineHeight = 24.sp,
                letterSpacing = (-0.2).sp,
                color = TikhoColors.TextPrimary,
            )
        } else {
            TextStyle(
                fontFamily = Onest,
                fontWeight = FontWeight.Medium,
                fontSize = 17.sp,
                lineHeight = 22.sp,
                letterSpacing = (-0.2).sp,
                color = TikhoColors.TextPrimary,
            )
        }

    val bigNumber: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = if (LocalTikhoPalette.current.isDark) {
            TextStyle(
                fontFamily = Playfair,
                fontWeight = FontWeight.Normal,
                fontSize = 52.sp,
                lineHeight = 50.sp,
                letterSpacing = (-0.5).sp,
                color = TikhoColors.Primary,
            )
        } else {
            TextStyle(
                fontFamily = Onest,
                fontWeight = FontWeight.Medium,
                fontSize = 44.sp,
                lineHeight = 46.sp,
                letterSpacing = (-1.5).sp,
                color = TikhoColors.Primary,
            )
        }

    /** Таймкоды и счётчики: в Lumen — машинный Geist Mono. */
    val timecode: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = TextStyle(
            fontFamily = if (LocalTikhoPalette.current.isDark) GeistMono else Onest,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = if (LocalTikhoPalette.current.isDark) 0.sp else 0.2.sp,
            fontFeatureSettings = "tnum",
            color = TikhoColors.TextTertiary,
        )

    /** Крупный отсчёт записи. */
    val timer: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = if (LocalTikhoPalette.current.isDark) {
            TextStyle(
                fontFamily = GeistMono,
                fontWeight = FontWeight.Normal,
                fontSize = 40.sp,
                lineHeight = 44.sp,
                letterSpacing = (-1.2).sp,
                fontFeatureSettings = "tnum",
                color = TikhoColors.TextPrimary,
            )
        } else {
            TextStyle(
                fontFamily = Onest,
                fontWeight = FontWeight.Medium,
                fontSize = 42.sp,
                lineHeight = 46.sp,
                letterSpacing = (-0.6).sp,
                fontFeatureSettings = "tnum",
                color = TikhoColors.TextPrimary,
            )
        }
}

@Composable
@ReadOnlyComposable
private fun bodyFamily(): FontFamily =
    if (LocalTikhoPalette.current.isDark) Geist else Onest

/**
 * Lumen набирает головы, навигацию и контролы строчными; Bloom — обычным
 * регистром. Аббревиатуры (JSON, MP3, WAV, UTF) сохраняются как есть, иначе
 * «Экспортировать JSON» превращалось бы в «экспортировать json».
 * Пользовательский текст (названия записей) эта функция не трогает.
 */
@Composable
@ReadOnlyComposable
fun displayCase(text: String): String =
    if (LocalTikhoPalette.current.isDark) lowerCasePreservingAcronyms(text) else text

private val Acronym = Regex("\\b[A-ZА-ЯЁ0-9]{2,}\\b")

/**
 * Чистая функция вне композиции: Compose-компилятор не любит лямбды внутри
 * @ReadOnlyComposable-функций, возвращающих значение.
 */
private fun lowerCasePreservingAcronyms(text: String): String {
    val acronyms = Acronym.findAll(text).map { it.value }.toList()
    var result = text.lowercase()
    acronyms.forEach { acronym ->
        result = result.replace(acronym.lowercase(), acronym)
    }
    return result
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
