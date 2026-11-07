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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import ru.mephi.tikho.ui.theme.DarkPalette
import ru.mephi.tikho.ui.theme.LightPalette
import ru.mephi.tikho.ui.theme.LocalTikhoPalette
import ru.mephi.tikho.ui.theme.TikhoPalette

/**
 * Живой фон приложения: многоцветный градиент, мягкие световые пятна,
 * звуковые волны, круги и редкие искры. Слой рисуется один раз под всеми
 * экранами и плавно перекрашивается при смене темы.
 */
@Composable
fun AppBackground(modifier: Modifier = Modifier) {
    val isDark = LocalTikhoPalette.current.isDark
    Crossfade(
        targetState = isDark,
        modifier = modifier.fillMaxSize(),
        animationSpec = tween(durationMillis = 520),
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
            brush = Brush.linearGradient(
                colors = palette.backgroundGradient,
                start = Offset(0f, 0f),
                end = Offset(w, h),
            ),
        )

        // Мягкие цветные пятна — именно они убирают «монотонность» фона.
        glow(
            center = Offset(w * 0.08f, h * 0.06f),
            radius = w * 0.95f,
            color = palette.glowA,
            alpha = if (palette.isDark) 0.42f else 0.52f,
        )
        glow(
            center = Offset(w * 0.96f, h * 0.14f),
            radius = w * 0.78f,
            color = palette.glowB,
            alpha = if (palette.isDark) 0.34f else 0.46f,
        )
        glow(
            center = Offset(w * 0.05f, h * 0.92f),
            radius = w * 0.82f,
            color = palette.glowC,
            alpha = if (palette.isDark) 0.28f else 0.38f,
        )
        glow(
            center = Offset(w * 0.92f, h * 0.86f),
            radius = w * 0.58f,
            color = palette.glowA,
            alpha = if (palette.isDark) 0.22f else 0.28f,
        )

        // Концентрические «круги звука» в верхней части.
        val ringCenter = Offset(w * 0.89f, h * 0.10f)
        listOf(0.20f, 0.30f, 0.40f).forEachIndexed { index, factor ->
            drawCircle(
                color = palette.pattern.copy(alpha = 0.13f - index * 0.025f),
                radius = w * factor,
                center = ringCenter,
                style = Stroke(width = 1.dp.toPx()),
            )
        }

        // Две плавные волны в нижней трети — тихий аудио-мотив.
        drawPath(
            path = wavePath(
                width = w,
                height = h,
                baseline = h * 0.76f,
                amplitude = h * 0.035f,
            ),
            color = palette.pattern.copy(alpha = if (palette.isDark) 0.16f else 0.20f),
            style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round),
        )
        drawPath(
            path = wavePath(
                width = w,
                height = h,
                baseline = h * 0.83f,
                amplitude = h * 0.022f,
            ),
            color = palette.pattern.copy(alpha = if (palette.isDark) 0.10f else 0.13f),
            style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round),
        )

        // Редкие точки и искры, чтобы фон не выглядел пустым.
        val dots = listOf(
            0.07f to 0.24f,
            0.14f to 0.31f,
            0.91f to 0.45f,
            0.86f to 0.52f,
            0.09f to 0.65f,
            0.94f to 0.71f,
            0.18f to 0.90f,
            0.81f to 0.88f,
        )
        dots.forEachIndexed { index, (x, y) ->
            drawCircle(
                color = palette.pattern.copy(alpha = if (index % 2 == 0) 0.24f else 0.14f),
                radius = if (index % 3 == 0) 2.2.dp.toPx() else 1.4.dp.toPx(),
                center = Offset(w * x, h * y),
            )
        }

        sparkle(
            center = Offset(w * 0.20f, h * 0.18f),
            radius = 5.dp.toPx(),
            color = palette.pattern.copy(alpha = 0.22f),
        )
        sparkle(
            center = Offset(w * 0.93f, h * 0.63f),
            radius = 4.dp.toPx(),
            color = palette.pattern.copy(alpha = 0.18f),
        )
        sparkle(
            center = Offset(w * 0.12f, h * 0.56f),
            radius = 3.dp.toPx(),
            color = palette.pattern.copy(alpha = 0.15f),
        )

        // Широкая дуга у нижнего края.
        drawCircle(
            color = palette.pattern.copy(alpha = if (palette.isDark) 0.09f else 0.12f),
            radius = w * 0.7f,
            center = Offset(w * 0.5f, h * 1.04f),
            style = Stroke(width = 1.2.dp.toPx()),
        )
    }
}

private fun DrawScope.glow(
    center: Offset,
    radius: Float,
    color: Color,
    alpha: Float,
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = alpha),
                color.copy(alpha = alpha * 0.38f),
                Color.Transparent,
            ),
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}

private fun wavePath(
    width: Float,
    height: Float,
    baseline: Float,
    amplitude: Float,
): Path = Path().apply {
    moveTo(0f, baseline)
    cubicTo(
        width * 0.16f, baseline - amplitude,
        width * 0.28f, baseline + amplitude,
        width * 0.43f, baseline,
    )
    cubicTo(
        width * 0.58f, baseline - amplitude,
        width * 0.70f, baseline + amplitude,
        width * 0.84f, baseline,
    )
    cubicTo(
        width * 0.91f, baseline - amplitude * 0.5f,
        width * 0.96f, baseline - amplitude * 0.2f,
        width, baseline,
    )
}

private fun DrawScope.sparkle(
    center: Offset,
    radius: Float,
    color: Color,
) {
    drawLine(
        color = color,
        start = Offset(center.x - radius, center.y),
        end = Offset(center.x + radius, center.y),
        strokeWidth = 1.1.dp.toPx(),
        cap = StrokeCap.Round,
    )
    drawLine(
        color = color,
        start = Offset(center.x, center.y - radius),
        end = Offset(center.x, center.y + radius),
        strokeWidth = 1.1.dp.toPx(),
        cap = StrokeCap.Round,
    )
}
