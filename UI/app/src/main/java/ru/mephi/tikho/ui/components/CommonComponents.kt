package ru.mephi.tikho.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mephi.tikho.ui.theme.TikhoColors
import ru.mephi.tikho.ui.theme.TikhoType

@Composable
fun TikhoCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 14.dp,
                shape = shape,
                clip = false,
                ambientColor = TikhoColors.Shadow,
                spotColor = TikhoColors.Shadow,
            )
            .clip(shape)
            .background(
                brush = Brush.linearGradient(TikhoColors.SurfaceGradient),
                shape = shape,
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.55f),
                        TikhoColors.Outline.copy(alpha = 0.75f),
                    ),
                ),
                shape = shape,
            ),
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
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
    val shape = RoundedCornerShape(16.dp)
    val gradientColors = if (enabled) {
        TikhoColors.PrimaryGradient
    } else {
        TikhoColors.PrimaryGradient.map { it.copy(alpha = 0.42f) }
    }
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(
                elevation = 10.dp,
                shape = shape,
                clip = false,
                ambientColor = TikhoColors.Primary.copy(alpha = 0.30f),
                spotColor = TikhoColors.Primary.copy(alpha = 0.30f),
            )
            .background(brush = Brush.linearGradient(gradientColors), shape = shape),
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = TikhoColors.OnPrimary,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = TikhoColors.OnPrimary.copy(alpha = 0.65f),
        ),
        contentPadding = PaddingValues(horizontal = 20.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(Modifier.width(10.dp))
        }
        Text(text = text, style = TikhoType.button)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(16.dp)
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(
                brush = Brush.linearGradient(TikhoColors.SurfaceGradient),
                shape = shape,
            ),
        shape = shape,
        border = BorderStroke(1.dp, TikhoColors.Outline),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = TikhoColors.TextPrimary,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(Modifier.width(9.dp))
        }
        Text(
            text = text,
            style = TikhoType.button.copy(
                color = TikhoColors.TextPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
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
            style = TikhoType.body.copy(
                fontSize = 14.sp,
                lineHeight = 18.sp,
                color = TikhoColors.TextSecondary,
            ),
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = TikhoType.body.copy(
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    color = TikhoColors.TextSecondary,
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
            .clip(RoundedCornerShape(100.dp))
            .background(
                brush = Brush.horizontalGradient(TikhoColors.PrimarySoftGradient),
                shape = RoundedCornerShape(100.dp),
            )
            .border(
                width = 1.dp,
                color = TikhoColors.Outline.copy(alpha = 0.6f),
                shape = RoundedCornerShape(100.dp),
            )
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
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.linearGradient(TikhoColors.IconTileGradient),
                shape = RoundedCornerShape(12.dp),
            )
            .border(
                width = 1.dp,
                color = TikhoColors.Outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
            ),
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
            .background(
                brush = Brush.horizontalGradient(
                    listOf(
                        color.copy(alpha = 0f),
                        color,
                        color.copy(alpha = 0f),
                    ),
                ),
            ),
    )
}
