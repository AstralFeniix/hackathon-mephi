package ru.mephi.tikho.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mephi.tikho.ui.theme.TikhoColors
import ru.mephi.tikho.ui.theme.TikhoType
import ru.mephi.tikho.ui.theme.displayCase

/** Bloom: мягкая бумажная карта с тенью. Lumen: плоский well с волосяной линией. */
@Composable
fun TikhoCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    grid: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val dark = TikhoColors.IsDark
    val shape = RoundedCornerShape(if (dark) 10.dp else 18.dp)
    val base = if (dark) {
        modifier
    } else {
        modifier.shadow(
            elevation = 18.dp,
            shape = shape,
            clip = false,
            ambientColor = TikhoColors.Shadow,
            spotColor = TikhoColors.Shadow,
        )
    }
    Box(
        modifier = base
            .clip(shape)
            .background(brush = Brush.linearGradient(TikhoColors.SurfaceGradient), shape = shape)
            .border(
                width = 1.dp,
                color = if (dark) TikhoColors.Divider else TikhoColors.Highlight.copy(alpha = 0.7f),
                shape = shape,
            ),
    ) {
        if (grid) {
            GridFloor(Modifier.matchParentSize())
        }
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

/**
 * Сетка внутри карточки: «пиано-ролл» в Bloom, blueprint в Lumen.
 * Всегда под содержимым и только в одном well на экран.
 */
@Composable
private fun GridFloor(modifier: Modifier = Modifier) {
    val dark = TikhoColors.IsDark
    val line = TikhoColors.GridLine
    Canvas(modifier.fillMaxSize()) {
        if (dark) {
            val pitch = 48.dp.toPx()
            val faint = line.copy(alpha = line.alpha * 0.62f)
            var x = 0f
            while (x <= size.width) {
                drawLine(faint, Offset(x, 0f), Offset(x, size.height), 0.7.dp.toPx())
                x += pitch
            }
            var y = 0f
            while (y <= size.height) {
                drawLine(faint, Offset(0f, y), Offset(size.width, y), 0.7.dp.toPx())
                y += pitch
            }
        } else {
            val beat = 24.dp.toPx()
            var x = 0f
            var index = 0
            while (x <= size.width) {
                val strong = index % 4 == 0
                drawLine(
                    color = line.copy(alpha = line.alpha * if (strong) 1f else 0.55f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = if (strong) 0.8.dp.toPx() else 0.5.dp.toPx(),
                )
                x += beat
                index++
            }
            var y = 24.dp.toPx()
            while (y <= size.height) {
                drawLine(
                    color = line.copy(alpha = line.alpha * 0.45f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 0.5.dp.toPx(),
                )
                y += 24.dp.toPx()
            }
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val dark = TikhoColors.IsDark
    val shape = CircleShape
    val gradientColors = if (enabled) {
        TikhoColors.PrimaryGradient
    } else {
        TikhoColors.PrimaryGradient.map { it.copy(alpha = 0.40f) }
    }
    val base = if (dark) {
        modifier
    } else {
        modifier.shadow(
            elevation = if (enabled) 12.dp else 0.dp,
            shape = shape,
            clip = false,
            ambientColor = TikhoColors.Primary.copy(alpha = 0.28f),
            spotColor = TikhoColors.Primary.copy(alpha = 0.28f),
        )
    }
    Button(
        onClick = onClick,
        modifier = base
            .fillMaxWidth()
            .height(54.dp)
            .background(brush = Brush.linearGradient(gradientColors), shape = shape),
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = TikhoColors.OnPrimary,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = TikhoColors.OnPrimary.copy(alpha = 0.65f),
        ),
        contentPadding = PaddingValues(horizontal = 22.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(Modifier.width(10.dp))
        }
        Text(text = displayCase(text), style = TikhoType.button)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val dark = TikhoColors.IsDark
    val shape = CircleShape
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(color = TikhoColors.Surface.copy(alpha = if (dark) 0.4f else 0.72f), shape = shape),
        shape = shape,
        border = BorderStroke(1.dp, TikhoColors.Outline),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = TikhoColors.TextPrimary,
        ),
        contentPadding = PaddingValues(horizontal = 18.dp),
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(Modifier.width(9.dp))
        }
        Text(
            text = displayCase(text),
            style = TikhoType.button.copy(
                color = TikhoColors.TextPrimary,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = TikhoType.caption.copy(
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = TikhoColors.TextSecondary,
            ),
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = TikhoType.timecode.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                ),
            )
        }
    }
}

@Composable
fun PrivacyFooter(
    text: String,
    modifier: Modifier = Modifier,
    showShield: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showShield) {
            ShieldCheckIcon(tint = TikhoColors.TextTertiary, size = 16.dp)
            Spacer(Modifier.width(7.dp))
        }
        Text(
            text = text,
            style = TikhoType.caption.copy(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = TikhoColors.TextTertiary,
            ),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun StatusPill(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(TikhoColors.PrimarySoft, CircleShape)
            .border(1.dp, TikhoColors.Primary.copy(alpha = 0.18f), CircleShape)
            .padding(horizontal = 11.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CheckIcon(tint = TikhoColors.Primary, size = 13.dp)
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = TikhoType.caption.copy(
                fontSize = 12.5.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TikhoColors.Primary,
            ),
        )
    }
}

@Composable
fun CardIconTile(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(if (TikhoColors.IsDark) 8.dp else 12.dp)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(brush = Brush.linearGradient(TikhoColors.IconTileGradient), shape = shape)
            .border(1.dp, TikhoColors.Outline.copy(alpha = 0.55f), shape),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
fun HorizontalDivider(modifier: Modifier = Modifier, color: Color = TikhoColors.Divider) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color),
    )
}
