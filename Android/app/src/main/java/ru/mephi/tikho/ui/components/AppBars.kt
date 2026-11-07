package ru.mephi.tikho.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mephi.tikho.R
import ru.mephi.tikho.ui.theme.TikhoColors
import ru.mephi.tikho.ui.theme.TikhoType
import ru.mephi.tikho.ui.theme.displayCase

/** Верхняя строка главного экрана: логотип, название, локальный режим и свитч темы. */
@Composable
fun LibraryTopBar(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LogoWaveform(size = 22.dp)
        Spacer(Modifier.width(10.dp))
        Text(text = displayCase(stringResource(R.string.app_name)), style = TikhoType.appTitle)
        Spacer(Modifier.weight(1f))
        SmartphoneIcon(size = 17.dp, tint = TikhoColors.TextTertiary)
        Spacer(Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.all_local),
            style = TikhoType.caption.copy(
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = TikhoColors.TextSecondary,
            ),
        )
        Spacer(Modifier.width(10.dp))
        ThemeToggle(isDarkTheme = isDarkTheme, onToggle = onToggleTheme)
    }
}

/** Верхняя строка внутренних экранов: стрелка назад, название, тема и действие. */
@Composable
fun DetailTopBar(
    title: String,
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClickLabel = stringResource(R.string.back), onClick = onBack),
            contentAlignment = Alignment.CenterStart,
        ) {
            BackArrowIcon(size = 22.dp)
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = displayCase(title),
            style = TikhoType.navTitle,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(8.dp))
        ThemeToggle(isDarkTheme = isDarkTheme, onToggle = onToggleTheme)
        if (action != null) {
            Spacer(Modifier.width(6.dp))
            action()
        }
    }
}

/** Маленькая иконка «ещё» для карточки результата. */
@Composable
fun MoreActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClickLabel = stringResource(R.string.more), onClick = onClick),
        contentAlignment = Alignment.CenterEnd,
    ) {
        MoreIcon(size = 22.dp)
    }
}

@Composable
fun ScreenHeadline(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = TikhoType.screenTitle,
        modifier = modifier.padding(0.dp),
    )
}

@Composable
fun MetaText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = TikhoType.body.copy(
            fontSize = 14.sp,
            lineHeight = 19.sp,
            color = TikhoColors.TextTertiary,
            fontWeight = FontWeight.Normal,
        ),
        modifier = modifier,
    )
}
