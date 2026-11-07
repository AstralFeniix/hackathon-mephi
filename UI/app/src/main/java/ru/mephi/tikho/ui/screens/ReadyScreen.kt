package ru.mephi.tikho.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mephi.tikho.R
import ru.mephi.tikho.data.formatClock
import ru.mephi.tikho.model.AudioItem
import ru.mephi.tikho.ui.components.AudioPreviewPlayer
import ru.mephi.tikho.ui.components.AudioWaveform
import ru.mephi.tikho.ui.components.CardIconTile
import ru.mephi.tikho.ui.components.CheckCircleIcon
import ru.mephi.tikho.ui.components.DetailTopBar
import ru.mephi.tikho.ui.components.HorizontalDivider
import ru.mephi.tikho.ui.components.LogoWaveform
import ru.mephi.tikho.ui.components.ModelBadgeIcon
import ru.mephi.tikho.ui.components.PauseIcon
import ru.mephi.tikho.ui.components.PlayIcon
import ru.mephi.tikho.ui.components.PrimaryButton
import ru.mephi.tikho.ui.components.PrivacyFooter
import ru.mephi.tikho.ui.components.SpeakersIcon
import ru.mephi.tikho.ui.components.TikhoCard
import ru.mephi.tikho.ui.components.rememberAudioPreviewPlayer
import ru.mephi.tikho.ui.theme.TikhoColors
import ru.mephi.tikho.ui.theme.TikhoType

@Composable
fun ReadyScreen(
    audio: AudioItem,
    onBack: () -> Unit,
    onTranscribe: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val player = rememberAudioPreviewPlayer(
        uriString = audio.uriString,
        durationMillis = audio.durationMillis,
    )

    fun togglePlayback() {
        if (!player.toggle()) {
            Toast.makeText(context, R.string.playback_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        DetailTopBar(
            title = stringResource(R.string.new_transcription),
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
                text = stringResource(R.string.audio_added),
                style = TikhoType.screenTitleCompact,
            )
            Spacer(Modifier.height(7.dp))
            Text(
                text = stringResource(R.string.check_before_transcribing),
                style = TikhoType.body,
            )
            Spacer(Modifier.height(20.dp))

            AudioCard(
                audio = audio,
                player = player,
                onTogglePlayback = { togglePlayback() },
            )

            Spacer(Modifier.height(16.dp))
            SettingsCard(language = audio.language)

            Spacer(Modifier.height(20.dp))
        }

        PrimaryButton(
            text = stringResource(R.string.transcribe_dialog),
            onClick = onTranscribe,
            leadingIcon = { LogoWaveform(tint = TikhoColors.OnPrimary, size = 20.dp) },
        )
        Spacer(Modifier.height(12.dp))
        PrivacyFooter(text = stringResource(R.string.privacy_ready))
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun AudioCard(
    audio: AudioItem,
    player: AudioPreviewPlayer,
    onTogglePlayback: () -> Unit,
) {
    val extension = audio.fileName.substringAfterLast('.', "audio").uppercase()
    val duration = player.durationMs.takeIf { it > 0 } ?: audio.durationMillis
    val playbackLabel = stringResource(
        if (player.isPlaying) R.string.pause_audio else R.string.play_audio,
    )

    TikhoCard(contentPadding = PaddingValues(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CardIconTile(size = 44.dp) {
                LogoWaveform(size = 22.dp)
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = extension,
                style = TikhoType.caption.copy(
                    fontSize = 12.5.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TikhoColors.Primary,
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(
                        brush = Brush.horizontalGradient(TikhoColors.PrimarySoftGradient),
                        shape = RoundedCornerShape(100.dp),
                    )
                    .border(
                        width = 1.dp,
                        color = TikhoColors.Outline.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(100.dp),
                    )
                    .padding(horizontal = 11.dp, vertical = 6.dp),
            )
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text = audio.fileName,
            style = TikhoType.cardTitle.copy(
                fontSize = 20.sp,
                lineHeight = 26.sp,
                letterSpacing = (-0.35).sp,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "${audio.durationLabel} · ${audio.sizeLabel}",
            style = TikhoType.caption.copy(
                fontSize = 14.sp,
                lineHeight = 19.sp,
                color = TikhoColors.TextTertiary,
            ),
        )
        Spacer(Modifier.height(14.dp))

        WaveformScrubber(player = player)

        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            CurrentTime(
                player = player,
                modifier = Modifier.align(Alignment.CenterStart),
            )
            val playButtonShape = CircleShape
            val playButtonColors = if (player.isPlaying) {
                TikhoColors.PrimaryGradient
            } else {
                TikhoColors.PrimarySoftGradient
            }
            val playButtonBrush = remember(playButtonColors) {
                Brush.linearGradient(playButtonColors)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = playButtonShape,
                        clip = false,
                        ambientColor = TikhoColors.Shadow,
                        spotColor = TikhoColors.Shadow,
                    )
                    .clip(playButtonShape)
                    .background(brush = playButtonBrush, shape = playButtonShape)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = if (player.isPlaying) 0.35f else 0.55f),
                        shape = playButtonShape,
                    )
                    .semantics { contentDescription = playbackLabel }
                    .clickable(
                        role = androidx.compose.ui.semantics.Role.Button,
                        onClickLabel = playbackLabel,
                        onClick = onTogglePlayback,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (player.isPlaying) {
                    PauseIcon(tint = TikhoColors.OnPrimary, size = 19.dp)
                } else {
                    PlayIcon(tint = TikhoColors.Primary, size = 19.dp)
                }
            }
            Text(
                text = formatClock(duration),
                style = TikhoType.caption.copy(fontSize = 13.sp, lineHeight = 17.sp),
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }

        if (player.isAvailable) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.seek_hint),
                style = TikhoType.caption.copy(
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp,
                    color = TikhoColors.TextTertiary,
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

@Composable
private fun CurrentTime(
    player: AudioPreviewPlayer,
    modifier: Modifier = Modifier,
) {
    // derivedStateOf обновляет текст только когда меняется отображаемая секунда,
    // а не 12 раз в секунду вместе с позицией плеера.
    val label by remember(player) {
        derivedStateOf { formatClock(player.positionMs) }
    }
    val hasProgress by remember(player) {
        derivedStateOf { player.positionMs > 0 }
    }
    Text(
        text = label,
        style = TikhoType.caption.copy(
            fontSize = 13.sp,
            lineHeight = 17.sp,
            color = if (hasProgress) TikhoColors.Primary else TikhoColors.TextTertiary,
        ),
        modifier = modifier,
    )
}

@Composable
private fun WaveformScrubber(player: AudioPreviewPlayer) {
    val positionDescription = stringResource(R.string.audio_position)
    val duration = player.durationMs.takeIf { it > 0 } ?: 1L
    val fraction = (player.positionMs.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    val lineColor = TikhoColors.Primary.copy(alpha = 0.62f)
    val playheadColors = TikhoColors.PrimaryGradient
    val playheadBrush = remember(playheadColors) {
        Brush.linearGradient(playheadColors)
    }
    val enabled = player.isAvailable

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .then(
                if (enabled) {
                    Modifier
                        .pointerInput(enabled) {
                            detectTapGestures { offset ->
                                player.seekTo((offset.x / size.width.toFloat()).coerceIn(0f, 1f))
                            }
                        }
                        .pointerInput(enabled) {
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    player.seekTo((offset.x / size.width.toFloat()).coerceIn(0f, 1f))
                                },
                                onHorizontalDrag = { change, _ ->
                                    change.consume()
                                    player.seekTo(
                                        (change.position.x / size.width.toFloat()).coerceIn(0f, 1f),
                                    )
                                },
                            )
                        }
                        .semantics {
                            contentDescription = positionDescription
                            progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
                            setProgress { target ->
                                player.seekTo(target.coerceIn(0f, 1f))
                                true
                            }
                        }
                } else {
                    Modifier
                },
            ),
    ) {
        AudioWaveform(
            modifier = Modifier.fillMaxSize(),
            playedFraction = fraction,
            barCount = 52,
        )

        Canvas(Modifier.fillMaxSize()) {
            val x = size.width * fraction
            drawLine(
                color = lineColor,
                start = Offset(x, 4.dp.toPx()),
                end = Offset(x, size.height - 4.dp.toPx()),
                strokeWidth = 1.4.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (maxWidth - 28.dp) * fraction)
                .size(28.dp)
                .shadow(
                    elevation = 7.dp,
                    shape = CircleShape,
                    clip = false,
                    ambientColor = TikhoColors.Shadow,
                    spotColor = TikhoColors.Shadow,
                )
                .clip(CircleShape)
                .background(brush = playheadBrush, shape = CircleShape)
                .border(1.2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            LogoWaveform(tint = Color.White, size = 14.dp)
        }
    }
}

@Composable
private fun SettingsCard(language: String) {
    TikhoCard(contentPadding = PaddingValues(0.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.language_audio),
                style = TikhoType.body.copy(fontSize = 15.sp, lineHeight = 20.sp),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = language,
                style = TikhoType.label.copy(fontWeight = FontWeight.SemiBold),
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        SettingRow(
            icon = { ModelBadgeIcon(size = 23.dp) },
            title = stringResource(R.string.model_on_device),
            subtitle = stringResource(R.string.model_ready),
            trailing = { CheckCircleIcon(size = 22.dp) },
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        SettingRow(
            icon = { SpeakersIcon(size = 23.dp) },
            title = stringResource(R.string.speakers),
            subtitle = stringResource(R.string.speakers_auto),
            trailing = {
                Text(
                    text = stringResource(R.string.speakers_auto_chip),
                    style = TikhoType.caption.copy(
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = TikhoColors.Primary,
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(
                            brush = Brush.horizontalGradient(TikhoColors.PrimarySoftGradient),
                            shape = RoundedCornerShape(100.dp),
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                )
            },
        )
    }
}

@Composable
private fun SettingRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = TikhoType.label.copy(
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = subtitle,
                style = TikhoType.caption.copy(
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    color = TikhoColors.TextTertiary,
                ),
            )
        }
        Spacer(Modifier.width(10.dp))
        trailing()
    }
}
