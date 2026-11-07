package ru.mephi.tikho.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import ru.mephi.tikho.R
import ru.mephi.tikho.model.Speaker
import ru.mephi.tikho.model.Utterance
import ru.mephi.tikho.model.timeRange
import ru.mephi.tikho.ui.theme.TikhoColors
import ru.mephi.tikho.ui.theme.TikhoType
import ru.mephi.tikho.ui.theme.speakerStyle

@Composable
fun SpeakerAvatar(
    name: String,
    index: Int,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    isSpeaking: Boolean = false,
) {
    val style = speakerStyle(index)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(style.soft)
            .border(
                width = if (isSpeaking) 1.6.dp else 1.dp,
                color = style.accent.copy(alpha = if (isSpeaking) 0.95f else 0.35f),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = speakerInitials(name),
            style = TikhoType.label.copy(
                fontSize = (size.value * 0.36f).sp,
                lineHeight = (size.value * 0.44f).sp,
                fontWeight = FontWeight.SemiBold,
                color = style.onSoft,
            ),
        )
    }
}

@Composable
fun SpeakerChip(
    name: String,
    index: Int,
    modifier: Modifier = Modifier,
) {
    val style = speakerStyle(index)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(style.soft)
            .border(1.dp, style.accent.copy(alpha = 0.25f), RoundedCornerShape(100.dp))
            .padding(start = 4.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpeakerAvatar(name = name, index = index, size = 22.dp)
        Spacer(Modifier.width(6.dp))
        Text(
            text = name,
            style = TikhoType.caption.copy(
                fontSize = 12.5.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
                color = style.onSoft,
            ),
        )
    }
}

@Composable
fun ParticipantStrip(
    participants: List<Speaker>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        participants.forEachIndexed { index, speaker ->
            SpeakerChip(name = speaker.name, index = index)
        }
    }
}

@Composable
fun SpeakerStack(
    participants: List<Speaker>,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    max: Int = 3,
) {
    val shown = participants.take(max)
    val overlap = size * 0.34f
    Box(
        modifier = modifier
            .width(size + overlap * (shown.size - 1).coerceAtLeast(0).toFloat())
            .height(size),
    ) {
        shown.forEachIndexed { index, speaker ->
            SpeakerAvatar(
                name = speaker.name,
                index = index,
                size = size,
                modifier = Modifier
                    .offset(x = overlap * index)
                    .zIndex((shown.size - index).toFloat()),
            )
        }
    }
}

/**
 * Реплика диалога: аватар и цвет говорящего, диапазон времени, текст.
 * Если у записи есть аудио, по нажатию можно перейти к этому моменту.
 */
@Composable
fun UtteranceBubble(
    utterance: Utterance,
    speakerName: String,
    speakerIndex: Int,
    modifier: Modifier = Modifier,
    canPlay: Boolean = false,
    isSpeaking: Boolean = false,
    compact: Boolean = false,
    onPlay: (() -> Unit)? = null,
) {
    val style = speakerStyle(speakerIndex)
    val shape = RoundedCornerShape(if (compact) 14.dp else 18.dp)
    val background = remember(style, isSpeaking) {
        Brush.linearGradient(
            listOf(
                style.soft.copy(alpha = if (isSpeaking) 1f else 0.92f),
                style.soft.copy(alpha = if (isSpeaking) 0.82f else 0.62f),
            ),
        )
    }
    val clickableModifier = if (canPlay && onPlay != null) {
        Modifier.clickable(
            role = Role.Button,
            onClickLabel = stringResource(R.string.play_utterance),
            onClick = onPlay,
        )
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(brush = background, shape = shape)
            .border(
                width = if (isSpeaking) 1.5.dp else 1.dp,
                color = style.accent.copy(alpha = if (isSpeaking) 0.8f else 0.20f),
                shape = shape,
            )
            .then(clickableModifier)
            .padding(if (compact) 10.dp else 12.dp),
    ) {
        SpeakerAvatar(
            name = speakerName,
            index = speakerIndex,
            size = if (compact) 28.dp else 34.dp,
            isSpeaking = isSpeaking,
        )
        Spacer(Modifier.width(if (compact) 8.dp else 10.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = speakerName,
                    style = TikhoType.caption.copy(
                        fontSize = if (compact) 12.5.sp else 13.5.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = style.onSoft,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    text = utterance.timeRange(),
                    style = TikhoType.caption.copy(
                        fontSize = if (compact) 10.5.sp else 11.5.sp,
                        lineHeight = 15.sp,
                        color = TikhoColors.TextTertiary,
                    ),
                )
                if (canPlay && onPlay != null) {
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(if (compact) 22.dp else 25.dp)
                            .clip(CircleShape)
                            .background(style.accent.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        PlayIcon(tint = style.accent, size = if (compact) 11.dp else 12.dp)
                    }
                }
            }
            Spacer(Modifier.height(if (compact) 3.dp else 4.dp))
            Text(
                text = utterance.text,
                style = TikhoType.transcript.copy(
                    fontSize = if (compact) 14.sp else 15.sp,
                    lineHeight = if (compact) 20.sp else 21.5.sp,
                ),
            )
        }
    }
}

private fun speakerInitials(name: String): String {
    val words = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    if (words.isEmpty()) return "?"
    if (words.size >= 2 && words[1].first().isDigit()) {
        return (words[0].take(1) + words[1].take(1)).uppercase()
    }
    if (words.size >= 2) {
        return words.take(2).joinToString(separator = "") { it.take(1) }.uppercase()
    }
    return words[0].take(1).uppercase()
}
