package ru.mephi.tikho.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import ru.mephi.tikho.R
import ru.mephi.tikho.ui.theme.TikhoColors

/**
 * Компактный свитч темы для верхней панели. Лавандово-фиолетовый в светлой
 * теме и бирюзовый в тёмной; бегунок плавно переезжает и показывает
 * солнце или луну.
 */
@Composable
fun ThemeToggle(
    isDarkTheme: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (isDarkTheme) 25.dp else 3.dp,
        animationSpec = tween(durationMillis = 320),
        label = "theme-thumb",
    )
    val trackShape = RoundedCornerShape(100.dp)
    val stateLabel = stringResource(
        if (isDarkTheme) R.string.theme_dark else R.string.theme_light,
    )
    val actionLabel = stringResource(
        if (isDarkTheme) R.string.switch_to_light else R.string.switch_to_dark,
    )

    Box(
        modifier = modifier
            .size(width = 54.dp, height = 44.dp)
            .clip(RoundedCornerShape(22.dp))
            .toggleable(
                value = isDarkTheme,
                role = Role.Switch,
                onValueChange = { onToggle() },
            )
            .semantics {
                contentDescription = actionLabel
                stateDescription = stateLabel
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 50.dp, height = 28.dp)
                .clip(trackShape)
                .background(
                    brush = Brush.horizontalGradient(TikhoColors.SwitchGradient),
                    shape = trackShape,
                )
                .border(1.dp, Color.White.copy(alpha = 0.35f), trackShape),
        ) {
            SunIcon(
                tint = Color.White.copy(alpha = if (isDarkTheme) 0.55f else 0.95f),
                size = 12.dp,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 7.dp),
            )
            MoonIcon(
                tint = Color.White.copy(alpha = if (isDarkTheme) 0.95f else 0.55f),
                size = 12.dp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 7.dp),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = thumbOffset)
                    .size(22.dp)
                    .shadow(elevation = 3.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(TikhoColors.SurfaceGradient),
                        shape = CircleShape,
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (isDarkTheme) {
                    MoonIcon(tint = TikhoColors.Primary, size = 12.dp)
                } else {
                    SunIcon(tint = TikhoColors.Primary, size = 12.dp)
                }
            }
        }
    }
}
