package ru.mephi.tikho.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.mephi.tikho.ui.theme.TikhoColors
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * Небольшой набор линейных иконок, нарисованных вручную.
 *
 * Макет использует очень тонкие (1.4–1.6 dp) контурные иконки, поэтому
 * стандартный material-icons выглядел бы тяжелее оригинала. Все иконки
 * рисуются в Canvas и масштабируются под переданный размер.
 */

@Composable
fun LogoWaveform(
    modifier: Modifier = Modifier,
    tint: Color = TikhoColors.Primary,
    size: Dp = 22.dp,
) {
    Canvas(modifier.then(Modifier.size(size))) {
        val bars = listOf(0.34f, 0.62f, 1f, 0.72f, 0.38f)
        val stroke = size.toPx() * 0.085f
        val gap = this.size.width / (bars.size * 2.5f)
        val totalWidth = this.size.width - gap * (bars.size - 1)
        val barWidth = totalWidth / bars.size
        val centerY = this.size.height / 2f
        bars.forEachIndexed { index, factor ->
            val x = barWidth / 2f + index * (barWidth + gap)
            val half = this.size.height * factor / 2f
            drawLine(
                color = tint,
                start = Offset(x, centerY - half),
                end = Offset(x, centerY + half),
                strokeWidth = stroke.coerceAtMost(barWidth),
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
fun AudioWaveform(
    modifier: Modifier = Modifier,
    tint: Color = TikhoColors.Waveform,
    playedTint: Color = TikhoColors.WaveformPlayed,
    playedFraction: Float = 0f,
    barCount: Int = 46,
) {
    Canvas(modifier) {
        val barWidth = 2.1.dp.toPx()
        val gap = ((size.width - barCount * barWidth) / (barCount - 1f)).coerceAtLeast(1.5.dp.toPx())
        val centerY = size.height / 2f
        for (i in 0 until barCount) {
            val t = i / (barCount - 1f)
            val envelope = 0.22f + 0.78f * sin((t * Math.PI).toFloat()).coerceIn(0f, 1f).pow(0.7f)
            val detail = 0.32f + 0.68f * abs(
                sin(i * 1.71f) * cos(i * 0.53f + 0.8f) + 0.28f * sin(i * 0.31f),
            )
            val amplitude = (0.16f + envelope * detail * 0.9f).coerceIn(0.12f, 1f)
            val half = size.height * amplitude / 2f
            val x = barWidth / 2f + i * (barWidth + gap)
            drawLine(
                color = if (t <= playedFraction) playedTint else tint,
                start = Offset(x, centerY - half),
                end = Offset(x, centerY + half),
                strokeWidth = barWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
fun PlusIcon(
    modifier: Modifier = Modifier,
    tint: Color = TikhoColors.OnPrimary,
    size: Dp = 20.dp,
) {
    Canvas(modifier.then(Modifier.size(size))) {
        val stroke = 1.8.dp.toPx()
        val c = this.size.width / 2f
        val inset = this.size.width * 0.17f
        drawLine(tint, Offset(inset, c), Offset(this.size.width - inset, c), stroke, StrokeCap.Round)
        drawLine(tint, Offset(c, inset), Offset(c, this.size.height - inset), stroke, StrokeCap.Round)
    }
}

@Composable
fun BackArrowIcon(
    modifier: Modifier = Modifier,
    tint: Color = TikhoColors.TextPrimary,
    size: Dp = 22.dp,
) {
    Canvas(modifier.then(Modifier.size(size))) {
        val stroke = 1.7.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        drawLine(tint, Offset(w * 0.18f, h * 0.5f), Offset(w * 0.84f, h * 0.5f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(w * 0.18f, h * 0.5f), Offset(w * 0.42f, h * 0.26f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(w * 0.18f, h * 0.5f), Offset(w * 0.42f, h * 0.74f), stroke, StrokeCap.Round)
    }
}

@Composable
fun ChevronRightIcon(
    modifier: Modifier = Modifier,
    tint: Color = TikhoColors.TextTertiary,
    size: Dp = 16.dp,
) {
    Canvas(modifier.then(Modifier.size(size))) {
        val stroke = 1.5.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        drawLine(tint, Offset(w * 0.36f, h * 0.22f), Offset(w * 0.64f, h * 0.5f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(w * 0.64f, h * 0.5f), Offset(w * 0.36f, h * 0.78f), stroke, StrokeCap.Round)
    }
}

@Composable
fun CheckIcon(
    modifier: Modifier = Modifier,
    tint: Color = TikhoColors.Primary,
    size: Dp = 14.dp,
) {
    Canvas(modifier.then(Modifier.size(size))) {
        val stroke = 1.8.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.18f, h * 0.52f)
            lineTo(w * 0.42f, h * 0.75f)
            lineTo(w * 0.84f, h * 0.25f)
        }
        drawPath(path, tint, style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun CheckCircleIcon(
    modifier: Modifier = Modifier,
    tint: Color = TikhoColors.Primary,
    size: Dp = 22.dp,
) {
    Canvas(modifier.then(Modifier.size(size))) {
        val stroke = 1.4.dp.toPx()
        drawCircle(tint, radius = this.size.minDimension / 2f - stroke / 2f, style = Stroke(stroke))
        val path = Path().apply {
            moveTo(this@Canvas.size.width * 0.30f, this@Canvas.size.height * 0.52f)
            lineTo(this@Canvas.size.width * 0.45f, this@Canvas.size.height * 0.67f)
            lineTo(this@Canvas.size.width * 0.72f, this@Canvas.size.height * 0.36f)
        }
        drawPath(path, tint, style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun SmartphoneIcon(
    modifier: Modifier = Modifier,
    tint: Color = TikhoColors.TextTertiary,
    size: Dp = 17.dp,
) {
    Canvas(modifier.then(Modifier.size(size))) {
        val stroke = 1.25.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.18f, h * 0.06f),
            size = Size(w * 0.64f, h * 0.88f),
            cornerRadius = CornerRadius(w * 0.12f),
            style = Stroke(stroke),
        )
        drawLine(
            tint,
            Offset(w * 0.42f, h * 0.84f),
            Offset(w * 0.58f, h * 0.84f),
            stroke,
            StrokeCap.Round,
        )
    }
}

@Composable
fun DocumentAudioIcon(
    modifier: Modifier = Modifier,
    tint: Color = TikhoColors.Primary,
    size: Dp = 20.dp,
) {
    Canvas(modifier.then(Modifier.size(size))) {
        val stroke = 1.35.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.22f, h * 0.10f)
            lineTo(w * 0.58f, h * 0.10f)
            lineTo(w * 0.79f, h * 0.32f)
            lineTo(w * 0.79f, h * 0.90f)
            lineTo(w * 0.22f, h * 0.90f)
            close()
        }
        drawPath(path, tint, style = Stroke(stroke, join = StrokeJoin.Round))
        val fold = Path().apply {
            moveTo(w * 0.58f, h * 0.10f)
            lineTo(w * 0.58f, h * 0.32f)
            lineTo(w * 0.79f, h * 0.32f)
        }
        drawPath(fold, tint, style = Stroke(stroke, join = StrokeJoin.Round))
        val bars = listOf(0.14f, 0.26f, 0.18f)
        bars.forEachIndexed { index, value ->
            val x = w * (0.36f + index * 0.13f)
            drawLine(
                tint,
                Offset(x, h * (0.62f - value)),
                Offset(x, h * (0.62f + value)),
                stroke,
                StrokeCap.Round,
            )
        }
    }
}

@Composable
fun ModelBadgeIcon(
    modifier: Modifier = Modifier,
    tint: Color = TikhoColors.Primary,
    size: Dp = 24.dp,
) {
    Canvas(modifier.then(Modifier.size(size))) {
        val stroke = 1.25.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        // Пунктирный «значок модели»: контур + чип внутри.
        val steps = 12
        for (i in 0 until steps) {
            val start = (i / steps.toFloat()) * 360f
            drawArc(
                color = tint,
                startAngle = start,
                sweepAngle = 360f / steps * 0.58f,
                useCenter = false,
                topLeft = Offset(w * 0.10f, h * 0.10f),
                size = Size(w * 0.80f, h * 0.80f),
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.34f, h * 0.34f),
            size = Size(w * 0.32f, h * 0.32f),
            cornerRadius = CornerRadius(w * 0.05f),
            style = Stroke(stroke),
        )
        val tick = w * 0.05f
        listOf(0.5f to 0.10f, 0.5f to 0.90f, 0.10f to 0.5f, 0.90f to 0.5f).forEach { (fx, fy) ->
            val cx = w * fx
            val cy = h * fy
            drawLine(
                tint,
                Offset(cx - tick, cy),
                Offset(cx + tick, cy),
                stroke,
                StrokeCap.Round,
            )
            drawLine(
                tint,
                Offset(cx, cy - tick),
                Offset(cx, cy + tick),
                stroke,
                StrokeCap.Round,
            )
        }
    }
}

@Composable
fun CopyIcon(
    modifier: Modifier = Modifier,
    tint: Color = TikhoColors.Primary,
    size: Dp = 16.dp,
) {
    Canvas(modifier.then(Modifier.size(size))) {
        val stroke = 1.3.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.10f, h * 0.22f),
            size = Size(w * 0.56f, h * 0.62f),
            cornerRadius = CornerRadius(w * 0.08f),
            style = Stroke(stroke),
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.34f, h * 0.10f),
            size = Size(w * 0.56f, h * 0.62f),
            cornerRadius = CornerRadius(w * 0.08f),
            style = Stroke(stroke),
        )
    }
}

@Composable
fun ExportIcon(
    modifier: Modifier = Modifier,
    tint: Color = TikhoColors.OnPrimary,
    size: Dp = 20.dp,
) {
    Canvas(modifier.then(Modifier.size(size))) {
        val stroke = 1.6.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        drawLine(tint, Offset(w * 0.5f, h * 0.12f), Offset(w * 0.5f, h * 0.62f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(w * 0.5f, h * 0.12f), Offset(w * 0.31f, h * 0.31f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(w * 0.5f, h * 0.12f), Offset(w * 0.69f, h * 0.31f), stroke, StrokeCap.Round)
        val tray = Path().apply {
            moveTo(w * 0.20f, h * 0.55f)
            lineTo(w * 0.20f, h * 0.86f)
            lineTo(w * 0.80f, h * 0.86f)
            lineTo(w * 0.80f, h * 0.55f)
        }
        drawPath(tray, tint, style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun ShieldCheckIcon(
    modifier: Modifier = Modifier,
    tint: Color = TikhoColors.TextTertiary,
    size: Dp = 16.dp,
) {
    Canvas(modifier.then(Modifier.size(size))) {
        val stroke = 1.25.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        val shield = Path().apply {
            moveTo(w * 0.5f, h * 0.08f)
            lineTo(w * 0.86f, h * 0.24f)
            lineTo(w * 0.86f, h * 0.50f)
            cubicTo(w * 0.86f, h * 0.74f, w * 0.70f, h * 0.88f, w * 0.5f, h * 0.94f)
            cubicTo(w * 0.30f, h * 0.88f, w * 0.14f, h * 0.74f, w * 0.14f, h * 0.50f)
            lineTo(w * 0.14f, h * 0.24f)
            close()
        }
        drawPath(shield, tint, style = Stroke(stroke, join = StrokeJoin.Round))
        val check = Path().apply {
            moveTo(w * 0.34f, h * 0.50f)
            lineTo(w * 0.46f, h * 0.62f)
            lineTo(w * 0.67f, h * 0.38f)
        }
        drawPath(check, tint, style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun PlayIcon(
    modifier: Modifier = Modifier,
    tint: Color = TikhoColors.Primary,
    size: Dp = 20.dp,
) {
    Canvas(modifier.then(Modifier.size(size))) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.34f, h * 0.20f)
            lineTo(w * 0.78f, h * 0.50f)
            lineTo(w * 0.34f, h * 0.80f)
            close()
        }
        drawPath(path, tint)
    }
}

@Composable
fun PauseIcon(
    modifier: Modifier = Modifier,
    tint: Color = TikhoColors.Primary,
    size: Dp = 20.dp,
) {
    Canvas(modifier.then(Modifier.size(size))) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.12f
        drawLine(tint, Offset(w * 0.38f, h * 0.24f), Offset(w * 0.38f, h * 0.76f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(w * 0.62f, h * 0.24f), Offset(w * 0.62f, h * 0.76f), stroke, StrokeCap.Round)
    }
}

@Composable
fun MoreIcon(
    modifier: Modifier = Modifier,
    tint: Color = TikhoColors.TextPrimary,
    size: Dp = 22.dp,
) {
    Canvas(modifier.then(Modifier.size(size))) {
        val radius = this.size.minDimension * 0.055f
        val cy = this.size.height / 2f
        listOf(0.22f, 0.5f, 0.78f).forEach { fx ->
            drawCircle(tint, radius, Offset(this.size.width * fx, cy))
        }
    }
}

@Composable
fun SunIcon(
    modifier: Modifier = Modifier,
    tint: Color = TikhoColors.Primary,
    size: Dp = 14.dp,
) {
    Canvas(modifier.then(Modifier.size(size))) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val stroke = 1.2.dp.toPx()
        drawCircle(tint, radius = w * 0.17f, center = Offset(cx, cy))
        for (i in 0 until 8) {
            val angle = Math.toRadians(i * 45.0)
            val start = Offset(
                x = cx + (w * 0.27f * kotlin.math.cos(angle)).toFloat(),
                y = cy + (h * 0.27f * kotlin.math.sin(angle)).toFloat(),
            )
            val end = Offset(
                x = cx + (w * 0.42f * kotlin.math.cos(angle)).toFloat(),
                y = cy + (h * 0.42f * kotlin.math.sin(angle)).toFloat(),
            )
            drawLine(tint, start, end, stroke, StrokeCap.Round)
        }
    }
}

@Composable
fun MoonIcon(
    modifier: Modifier = Modifier,
    tint: Color = TikhoColors.Primary,
    size: Dp = 14.dp,
) {
    Canvas(modifier.then(Modifier.size(size))) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.62f, h * 0.10f)
            cubicTo(w * 0.18f, h * 0.16f, w * 0.18f, h * 0.84f, w * 0.62f, h * 0.90f)
            cubicTo(w * 0.36f, h * 0.70f, w * 0.36f, h * 0.30f, w * 0.62f, h * 0.10f)
            close()
        }
        drawPath(path, tint)
    }
}

@Composable
fun SpeakersIcon(
    modifier: Modifier = Modifier,
    tint: Color = TikhoColors.Primary,
    size: Dp = 24.dp,
) {
    Canvas(modifier.then(Modifier.size(size))) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 1.25.dp.toPx()
        drawCircle(
            color = tint,
            radius = w * 0.100f,
            center = Offset(w * 0.31f, h * 0.33f),
            style = Stroke(stroke),
        )
        drawArc(
            color = tint,
            startAngle = 205f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = Offset(w * 0.10f, h * 0.42f),
            size = Size(w * 0.42f, h * 0.34f),
            style = Stroke(stroke, cap = StrokeCap.Round),
        )
        drawCircle(
            color = tint,
            radius = w * 0.086f,
            center = Offset(w * 0.67f, h * 0.29f),
            style = Stroke(stroke),
        )
        drawArc(
            color = tint,
            startAngle = 205f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = Offset(w * 0.48f, h * 0.38f),
            size = Size(w * 0.40f, h * 0.32f),
            style = Stroke(stroke, cap = StrokeCap.Round),
        )
    }
}

@Composable
fun MicIcon(
    modifier: Modifier = Modifier,
    tint: Color = TikhoColors.Primary,
    size: Dp = 22.dp,
) {
    Canvas(modifier.then(Modifier.size(size))) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 1.4.dp.toPx()
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.36f, h * 0.08f),
            size = Size(w * 0.28f, h * 0.46f),
            cornerRadius = CornerRadius(w * 0.14f),
            style = Stroke(stroke),
        )
        val cradle = Path().apply {
            moveTo(w * 0.24f, h * 0.46f)
            cubicTo(w * 0.24f, h * 0.72f, w * 0.76f, h * 0.72f, w * 0.76f, h * 0.46f)
        }
        drawPath(cradle, tint, style = Stroke(stroke, cap = StrokeCap.Round))
        drawLine(
            tint,
            Offset(w * 0.50f, h * 0.72f),
            Offset(w * 0.50f, h * 0.88f),
            stroke,
            StrokeCap.Round,
        )
        drawLine(
            tint,
            Offset(w * 0.34f, h * 0.90f),
            Offset(w * 0.66f, h * 0.90f),
            stroke,
            StrokeCap.Round,
        )
    }
}

@Composable
fun ImportIcon(
    modifier: Modifier = Modifier,
    tint: Color = TikhoColors.Primary,
    size: Dp = 20.dp,
) {
    Canvas(modifier.then(Modifier.size(size))) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 1.4.dp.toPx()
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.14f, h * 0.10f),
            size = Size(w * 0.48f, h * 0.68f),
            cornerRadius = CornerRadius(w * 0.07f),
            style = Stroke(stroke),
        )
        val fold = Path().apply {
            moveTo(w * 0.62f, h * 0.10f)
            lineTo(w * 0.62f, h * 0.30f)
            lineTo(w * 0.82f, h * 0.30f)
        }
        drawPath(fold, tint, style = Stroke(stroke, join = StrokeJoin.Round))
        drawLine(
            tint,
            Offset(w * 0.78f, h * 0.44f),
            Offset(w * 0.78f, h * 0.78f),
            stroke,
            StrokeCap.Round,
        )
        drawLine(
            tint,
            Offset(w * 0.66f, h * 0.66f),
            Offset(w * 0.78f, h * 0.78f),
            stroke,
            StrokeCap.Round,
        )
        drawLine(
            tint,
            Offset(w * 0.90f, h * 0.66f),
            Offset(w * 0.78f, h * 0.78f),
            stroke,
            StrokeCap.Round,
        )
    }
}

@Composable
fun StopIcon(
    modifier: Modifier = Modifier,
    tint: Color = TikhoColors.OnPrimary,
    size: Dp = 18.dp,
) {
    Canvas(modifier.then(Modifier.size(size))) {
        val side = this.size.minDimension * 0.56f
        drawRoundRect(
            color = tint,
            topLeft = Offset(
                (this.size.width - side) / 2f,
                (this.size.height - side) / 2f,
            ),
            size = Size(side, side),
            cornerRadius = CornerRadius(side * 0.18f),
        )
    }
}

private fun Float.pow(exp: Float): Float = this.toDouble().pow(exp.toDouble()).toFloat()
