package ru.mephi.tikho.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mephi.tikho.R
import ru.mephi.tikho.model.Recording
import ru.mephi.tikho.model.utterancesLabel
import ru.mephi.tikho.ui.components.CopyIcon
import ru.mephi.tikho.ui.components.DetailTopBar
import ru.mephi.tikho.ui.components.ExportIcon
import ru.mephi.tikho.ui.components.MoreActionButton
import ru.mephi.tikho.ui.components.ParticipantStrip
import ru.mephi.tikho.ui.components.PauseIcon
import ru.mephi.tikho.ui.components.PlayIcon
import ru.mephi.tikho.ui.components.PrimaryButton
import ru.mephi.tikho.ui.components.PrivacyFooter
import ru.mephi.tikho.ui.components.StatusPill
import ru.mephi.tikho.ui.components.TikhoCard
import ru.mephi.tikho.ui.components.UtteranceBubble
import ru.mephi.tikho.ui.components.rememberAudioPreviewPlayer
import ru.mephi.tikho.ui.theme.TikhoColors
import ru.mephi.tikho.ui.theme.TikhoType

private const val JSON_MIME = "application/json"

@Composable
fun ResultScreen(
    recording: Recording,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val player = rememberAudioPreviewPlayer(
        uriString = recording.uriString,
        durationMillis = recording.durationMillis,
    )
    val canPlay = recording.uriString != null
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    fun copyText(text: String, toastOnOldAndroid: Boolean = true) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(recording.title, text))
        if (toastOnOldAndroid && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
        }
    }

    fun shareDialog() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, recording.title)
            putExtra(Intent.EXTRA_TEXT, recording.plainText)
        }
        context.startActivity(Intent.createChooser(intent, null))
    }

    val exportJson = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(JSON_MIME),
    ) { uri ->
        if (uri != null) {
            val json = runCatching { recording.toJson() }.getOrNull()
            val written = if (json != null) {
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(json.toByteArray(Charsets.UTF_8))
                    } != null
                }.getOrDefault(false)
            } else {
                false
            }
            Toast.makeText(
                context,
                if (written) R.string.export_done else R.string.export_error,
                Toast.LENGTH_SHORT,
            ).show()
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
            title = stringResource(R.string.transcription),
            onBack = onBack,
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,
            action = {
                Box {
                    MoreActionButton(onClick = { menuExpanded = true })
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.share), style = TikhoType.body) },
                            onClick = {
                                menuExpanded = false
                                shareDialog()
                            },
                        )
                        DropdownMenuItem(
                            text = {
                                Text(stringResource(R.string.copy_json), style = TikhoType.body)
                            },
                            onClick = {
                                menuExpanded = false
                                copyText(recording.toJson(), toastOnOldAndroid = false)
                                Toast.makeText(context, R.string.json_copied, Toast.LENGTH_SHORT).show()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete), style = TikhoType.body) },
                            onClick = {
                                menuExpanded = false
                                showDeleteDialog = true
                            },
                        )
                    }
                }
            },
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(top = 18.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "header") {
                StatusPill(text = stringResource(R.string.saved_on_device))
                Spacer(Modifier.height(14.dp))
                val titleStyle = when {
                    recording.title.length <= 16 -> TikhoType.screenTitleLarge
                    recording.title.length <= 26 -> TikhoType.screenTitleCompact
                    else -> TikhoType.screenTitleCompact.copy(
                        fontSize = 27.sp,
                        lineHeight = 33.sp,
                        letterSpacing = (-0.8).sp,
                    )
                }
                Text(
                    text = recording.title,
                    style = titleStyle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = recording.metaLine,
                    style = TikhoType.caption.copy(
                        fontSize = 14.sp,
                        lineHeight = 19.sp,
                        color = TikhoColors.TextTertiary,
                    ),
                )
                Spacer(Modifier.height(16.dp))
                ParticipantsCard(recording = recording)
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.dialog_title),
                        style = TikhoType.cardTitle.copy(
                            fontSize = 17.sp,
                            lineHeight = 22.sp,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                copyText(recording.plainText)
                            }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CopyIcon(size = 15.dp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.copy),
                            style = TikhoType.caption.copy(
                                fontSize = 13.sp,
                                lineHeight = 17.sp,
                                fontWeight = FontWeight.Medium,
                                color = TikhoColors.Primary,
                            ),
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            items(
                items = recording.utterances,
                key = { "${it.speakerId}-${it.startMs}" },
                contentType = { "utterance" },
            ) { utterance ->
                UtteranceBubble(
                    utterance = utterance,
                    speakerName = recording.speakerName(utterance.speakerId),
                    speakerIndex = recording.speakerIndex(utterance.speakerId),
                    canPlay = canPlay,
                    onPlay = if (canPlay) {
                        {
                            if (!player.playFrom(utterance.startMs)) {
                                Toast.makeText(context, R.string.playback_unavailable, Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        null
                    },
                )
            }

            item(key = "tail") { Spacer(Modifier.height(6.dp)) }
        }

        if (canPlay) {
            PlayerBar(player = player)
            Spacer(Modifier.height(10.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.json_file),
                style = TikhoType.caption.copy(
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    color = TikhoColors.TextSecondary,
                ),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.json_utf8),
                style = TikhoType.caption.copy(
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TikhoColors.TextPrimary,
                ),
            )
        }
        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            text = stringResource(R.string.export_json),
            onClick = {
                exportJson.launch(safeExportName(recording.title, "json"))
            },
            leadingIcon = { ExportIcon(size = 20.dp) },
        )
        Spacer(Modifier.height(8.dp))
        PrivacyFooter(
            text = stringResource(R.string.save_to_folder),
            showShield = false,
        )
        Spacer(Modifier.height(10.dp))
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.delete_title),
                    style = TikhoType.cardTitle,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.delete_message),
                    style = TikhoType.body,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                ) {
                    Text(
                        text = stringResource(R.string.delete),
                        style = TikhoType.label.copy(color = TikhoColors.Primary),
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = TikhoType.label.copy(color = TikhoColors.TextSecondary),
                    )
                }
            },
            containerColor = TikhoColors.Surface,
            shape = RoundedCornerShape(24.dp),
        )
    }
}

@Composable
private fun ParticipantsCard(recording: Recording) {
    TikhoCard(contentPadding = PaddingValues(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.in_dialog),
                style = TikhoType.caption.copy(
                    fontSize = 12.5.sp,
                    lineHeight = 16.sp,
                    color = TikhoColors.TextSecondary,
                ),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = utterancesLabel(recording.utteranceCount),
                style = TikhoType.caption.copy(
                    fontSize = 12.5.sp,
                    lineHeight = 16.sp,
                    color = TikhoColors.TextTertiary,
                ),
            )
        }
        Spacer(Modifier.height(9.dp))
        ParticipantStrip(participants = recording.participants)
    }
}

@Composable
private fun PlayerBar(player: ru.mephi.tikho.ui.components.AudioPreviewPlayer) {
    val context = LocalContext.current
    val playing = player.isPlaying
    val shape = RoundedCornerShape(16.dp)
    val description = stringResource(
        if (playing) R.string.pause_audio else R.string.listen_dialog,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                brush = Brush.linearGradient(TikhoColors.SurfaceGradient),
                shape = shape,
            )
            .border(1.dp, TikhoColors.Outline, shape)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        if (playing) TikhoColors.PrimaryGradient else TikhoColors.PrimarySoftGradient,
                    ),
                    shape = CircleShape,
                )
                .semantics { contentDescription = description }
                .clickable(
                    onClickLabel = description,
                    onClick = {
                        if (!player.toggle()) {
                            Toast.makeText(context, R.string.playback_unavailable, Toast.LENGTH_SHORT).show()
                        }
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (playing) {
                PauseIcon(tint = TikhoColors.OnPrimary, size = 17.dp)
            } else {
                PlayIcon(tint = TikhoColors.Primary, size = 17.dp)
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(
                    if (playing) R.string.listening_dialog else R.string.listen_dialog,
                ),
                style = TikhoType.label.copy(
                    fontSize = 13.5.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.tap_utterance_hint),
                style = TikhoType.caption.copy(
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp,
                    color = TikhoColors.TextTertiary,
                ),
            )
        }
    }
}

private fun safeExportName(title: String, extension: String): String {
    val cleaned = title
        .replace(Regex("[\\\\/:*?\"<>|]"), " ")
        .trim()
        .ifBlank { "Диалог" }
    return "$cleaned.$extension"
}
