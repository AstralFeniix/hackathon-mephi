package ru.mephi.tikho.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mephi.tikho.R
import ru.mephi.tikho.data.formatProcessed
import ru.mephi.tikho.data.remainingLabel
import ru.mephi.tikho.model.AudioItem
import ru.mephi.tikho.model.Speaker
import ru.mephi.tikho.model.Utterance
import ru.mephi.tikho.model.speakersLabel
import ru.mephi.tikho.ui.components.CheckIcon
import ru.mephi.tikho.ui.components.DetailTopBar
import ru.mephi.tikho.ui.components.PrivacyFooter
import ru.mephi.tikho.ui.components.PrimaryButton
import ru.mephi.tikho.ui.components.SecondaryButton
import ru.mephi.tikho.ui.components.SpeakerAvatar
import ru.mephi.tikho.ui.components.SpeakersIcon
import ru.mephi.tikho.ui.components.TikhoCard
import ru.mephi.tikho.ui.components.UtteranceBubble
import ru.mephi.tikho.ui.theme.TikhoColors
import ru.mephi.tikho.ui.theme.TikhoType
import ru.mephi.tikho.ui.theme.displayCase
import kotlin.math.roundToInt

@Composable
fun ProcessingScreen(
    audio: AudioItem,
    progress: Float,
    visibleUtterances: List<Utterance>,
    error: String?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onCancel: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 260),
        label = "transcription-progress",
    )
    val percent = (animatedProgress * 100f).roundToInt().coerceIn(0, 100)
    val processed = formatProcessed(animatedProgress, audio.durationMillis)
    val speakerCount = if (progress >= 0.72f) {
        visibleUtterances.map { it.speakerId }.distinct().size
    } else {
        0
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        DetailTopBar(
            title = stringResource(R.string.processing),
            onBack = onBack,
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(18.dp))
            Text(
                text = displayCase(
                    stringResource(
                        if (error == null) R.string.transcribing else R.string.processing_error_title,
                    ),
                ),
                style = TikhoType.screenTitleCompact,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = "${audio.fileName}, ${audio.durationLabel}",
                style = TikhoType.caption.copy(
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    color = TikhoColors.TextTertiary,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(20.dp))

            if (error == null) {
                ProgressCard(
                    percent = percent,
                    progress = animatedProgress,
                    processed = processed,
                    total = audio.durationLabel,
                    remaining = remainingLabel(animatedProgress, audio.durationMillis),
                    speakerCount = speakerCount,
                )

                Spacer(Modifier.height(14.dp))

                LiveDialogCard(
                    utterances = visibleUtterances,
                    speakerCount = speakerCount,
                )

                Spacer(Modifier.height(22.dp))

                SecondaryButton(
                    text = stringResource(R.string.cancel_processing),
                    onClick = onCancel,
                )
            } else {
                ProcessingErrorCard(
                    message = error,
                    onRetry = onRetry,
                    onBack = onCancel,
                )
            }
            Spacer(Modifier.height(12.dp))
            PrivacyFooter(text = stringResource(R.string.privacy_processing))
            Spacer(Modifier.height(70.dp))
        }
    }
}

@Composable
private fun ProcessingErrorCard(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    TikhoCard(contentPadding = PaddingValues(20.dp)) {
        Text(
            text = stringResource(R.string.processing_error_hint),
            style = TikhoType.body.copy(
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = TikhoColors.TextSecondary,
            ),
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = message,
            style = TikhoType.caption.copy(
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = TikhoColors.TextTertiary,
            ),
        )
        Spacer(Modifier.height(18.dp))
        PrimaryButton(
            text = stringResource(R.string.retry),
            onClick = onRetry,
        )
        Spacer(Modifier.height(10.dp))
        SecondaryButton(
            text = stringResource(R.string.back_to_recordings),
            onClick = onBack,
        )
    }
}

@Composable
private fun ProgressCard(
    percent: Int,
    progress: Float,
    processed: String,
    total: String,
    remaining: String,
    speakerCount: Int,
) {
    TikhoCard(contentPadding = PaddingValues(20.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "$percent%",
                style = TikhoType.bigNumber,
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = stringResource(R.string.processed),
                style = TikhoType.body.copy(
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    color = TikhoColors.TextSecondary,
                ),
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(TikhoColors.ProgressTrack),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(100.dp))
                    .background(
                        brush = Brush.horizontalGradient(TikhoColors.ProgressGradient),
                        shape = RoundedCornerShape(100.dp),
                    ),
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.processed_time, processed),
                style = TikhoType.label.copy(
                    fontSize = 14.5.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Medium,
                ),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.of_time, total),
                style = TikhoType.caption.copy(
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    color = TikhoColors.TextTertiary,
                ),
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = remaining,
                style = TikhoType.caption.copy(
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    color = TikhoColors.TextTertiary,
                ),
                modifier = Modifier.weight(1f),
            )
            if (speakerCount > 0) {
                SpeakersIcon(size = 15.dp)
                Spacer(Modifier.width(5.dp))
                Text(
                    text = speakersLabel(speakerCount),
                    style = TikhoType.caption.copy(
                        fontSize = 12.5.sp,
                        lineHeight = 16.sp,
                        color = TikhoColors.TextSecondary,
                    ),
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        StageStrip(progress = progress)
    }
}

@Composable
private fun StageStrip(progress: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StageChip(
            title = displayCase(stringResource(R.string.stage_speech_short)),
            done = progress >= 0.72f,
            active = progress in 0.10f..0.72f,
            modifier = Modifier.weight(1f),
        )
        StageChip(
            title = displayCase(stringResource(R.string.stage_speakers_short)),
            done = progress >= 0.94f,
            active = progress in 0.72f..0.94f,
            modifier = Modifier.weight(1f),
        )
        StageChip(
            title = displayCase(stringResource(R.string.stage_dialog_short)),
            done = progress >= 0.99f,
            active = progress >= 0.94f,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StageChip(
    title: String,
    done: Boolean,
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(100.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(
                brush = if (active) {
                    Brush.horizontalGradient(TikhoColors.PrimarySoftGradient)
                } else {
                    Brush.horizontalGradient(
                        listOf(TikhoColors.Surface.copy(alpha = 0.55f), TikhoColors.Surface),
                    )
                },
                shape = shape,
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (done) {
            CheckIcon(tint = TikhoColors.Primary, size = 11.dp)
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = title,
            style = TikhoType.caption.copy(
                fontSize = 10.5.sp,
                lineHeight = 14.sp,
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                color = if (active) TikhoColors.Primary else TikhoColors.TextTertiary,
            ),
            maxLines = 1,
        )
    }
}

@Composable
private fun LiveDialogCard(
    utterances: List<Utterance>,
    speakerCount: Int,
) {
    val speakers = remember(utterances) {
        utterances
            .map { it.speakerId }
            .distinct()
            .map { id -> Speaker(id = id, name = genericSpeakerName(id)) }
    }
    // Во время длинной записи держим в live-карточке только последние реплики:
    // так экран не растёт на сотни элементов и не перерисовывает весь диалог.
    val recentUtterances = remember(utterances) { utterances.takeLast(8) }

    TikhoCard(
        modifier = Modifier.animateContentSize(),
        contentPadding = PaddingValues(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = displayCase(stringResource(R.string.text_as_processed)),
                style = TikhoType.cardTitle.copy(
                    fontSize = 15.5.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = displayCase(stringResource(R.string.now)),
                style = TikhoType.caption.copy(
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = TikhoColors.Primary,
                ),
            )
        }
        if (speakers.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    speakers.forEachIndexed { index, speaker ->
                        SpeakerAvatar(
                            name = speaker.name,
                            index = index,
                            size = 24.dp,
                        )
                    }
                }
                if (speakerCount > 0) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = speakersLabel(speakerCount),
                        style = TikhoType.caption.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = TikhoColors.TextTertiary,
                        ),
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        if (utterances.isEmpty()) {
            Text(
                text = stringResource(R.string.listening_placeholder),
                style = TikhoType.body.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = TikhoColors.TextTertiary,
                ),
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recentUtterances.forEachIndexed { index, utterance ->
                    val speakerIndex = speakers.indexOfFirst { it.id == utterance.speakerId }
                        .coerceAtLeast(0)
                    UtteranceBubble(
                        utterance = utterance,
                        speakerName = speakers.getOrNull(speakerIndex)?.name ?: utterance.speakerId,
                        speakerIndex = speakerIndex,
                        compact = true,
                        isSpeaking = index == recentUtterances.lastIndex,
                    )
                }
            }
        }
    }
}

private fun genericSpeakerName(speakerId: String): String {
    val number = speakerId.removePrefix("speaker_").toIntOrNull()
    return if (number != null) "Говорящий $number" else speakerId
}

