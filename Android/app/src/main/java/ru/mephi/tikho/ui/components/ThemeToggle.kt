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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import ru.mephi.tikho.R
import ru.mephi.tikho.ui.theme.TikhoColors

/**
 * Компактный переключатель темы: бумажная дорожка с терракотовым бегунком
 * днём и латунный бегунок на ночной дорожке.
 */
@Composable
fun ThemeToggle(
    isDarkTheme: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (isDarkTheme) 23.dp else 3.dp,
        animationSpec = tween(durationMillis = 260),
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
            .size(width = 50.dp, height = 44.dp)
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
                .size(width = 46.dp, height = 26.dp)
                .clip(trackShape)
                .background(TikhoColors.ProgressTrack, trackShape)
                .border(1.dp, TikhoColors.Outline, trackShape),
        ) {
            SunIcon(
                tint = TikhoColors.TextTertiary.copy(alpha = if (isDarkTheme) 0.45f else 0.9f),
                size = 11.dp,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 6.dp),
            )
            MoonIcon(
                tint = TikhoColors.TextTertiary.copy(alpha = if (isDarkTheme) 0.9f else 0.45f),
                size = 11.dp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = thumbOffset)
                    .size(20.dp)
                    .then(
                        if (isDarkTheme) Modifier else Modifier.shadow(2.dp, CircleShape),
                    )
                    .clip(CircleShape)
                    .background(
                        if (isDarkTheme) TikhoColors.Primary else TikhoColors.Surface,
                        CircleShape,
                    )
                    .border(
                        1.dp,
                        if (isDarkTheme) TikhoColors.PrimaryPressed else TikhoColors.Outline,
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isDarkTheme) {
                    MoonIcon(tint = TikhoColors.OnPrimary, size = 11.dp)
                } else {
                    SunIcon(tint = TikhoColors.Primary, size = 11.dp)
                }
            }
        }
    }
}
