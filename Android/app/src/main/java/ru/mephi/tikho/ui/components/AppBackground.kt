package ru.mephi.tikho.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import ru.mephi.tikho.ui.theme.DarkPalette
import ru.mephi.tikho.ui.theme.LightPalette
import ru.mephi.tikho.ui.theme.LocalTikhoPalette
import ru.mephi.tikho.ui.theme.TikhoPalette

/**
 * Фон приложения — материал темы, а не декоративная заливка.
 *
 * Bloom: тёплая бумага, две тихие терракотовые «розы» и сетка пиано-ролла,
 * которая проявляется у сцены и растворяется к низу экрана.
 * Lumen: фиолетово-чёрная ночь с одним шагом светлоты; чертёжная сетка живёт
 * внутри карточек, а не под текстом.
 */
@Composable
fun AppBackground(modifier: Modifier = Modifier) {
    val isDark = LocalTikhoPalette.current.isDark
    Crossfade(
        targetState = isDark,
        modifier = modifier.fillMaxSize(),
        animationSpec = tween(durationMillis = 420),
        label = "app-background",
    ) { dark ->
        BackgroundCanvas(palette = if (dark) DarkPalette else LightPalette)
    }
}

@Composable
private fun BackgroundCanvas(palette: TikhoPalette) {
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = palette.backgroundGradient,
                startY = 0f,
                endY = h,
            ),
        )

        if (palette.isDark) {
            // Глубина — только шаг светлоты, без свечения и ореолов.
            drawRect(
                brush = Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.55f to Color.Transparent,
                    1f to palette.surface.copy(alpha = 0.55f),
                ),
            )
            return@Canvas
        }

        // Две тёплые розы: одна у верхнего правого края, вторая — у нижнего левого.
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.glowA.copy(alpha = 0.10f),
                    palette.glowA.copy(alpha = 0.035f),
                    Color.Transparent,
                ),
                center = Offset(w * 0.98f, h * 0.02f),
                radius = w * 0.95f,
            ),
            radius = w * 0.95f,
            center = Offset(w * 0.98f, h * 0.02f),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.glowB.copy(alpha = 0.075f),
                    palette.glowB.copy(alpha = 0.025f),
                    Color.Transparent,
                ),
                center = Offset(w * 0.02f, h * 0.98f),
                radius = w * 0.9f,
            ),
            radius = w * 0.9f,
            center = Offset(w * 0.02f, h * 0.98f),
        )

        // Сетка «пиано-ролла»: такты, доли и строки, растворяющиеся вниз.
        val beat = 24.dp.toPx()
        val bar = beat * 4f
        val fade = h * 0.55f
        val line = palette.gridLine.copy(alpha = palette.gridLine.alpha * 0.45f)
        val verticalFade = Brush.verticalGradient(
            colors = listOf(line, Color.Transparent),
            startY = 0f,
            endY = fade,
        )
        var x = 0f
        var index = 0
        while (x <= w) {
            val strong = index % 4 == 0
            drawLine(
                brush = verticalFade,
                start = Offset(x, 0f),
                end = Offset(x, fade),
                strokeWidth = if (strong) 1.dp.toPx() else 0.6.dp.toPx(),
                cap = StrokeCap.Butt,
            )
            x += beat
            index++
        }

        val row = 24.dp.toPx()
        var y = row
        while (y < fade) {
            drawLine(
                color = line.copy(alpha = line.alpha * 0.55f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 0.6.dp.toPx(),
            )
            y += row
        }

        // Верхняя граница сцены: линия такта, а не рамка.
        drawLine(
            color = line.copy(alpha = line.alpha * 1.6f),
            start = Offset(0f, bar * 0.5f),
            end = Offset(w, bar * 0.5f),
            strokeWidth = 0.8.dp.toPx(),
        )
    }
}
