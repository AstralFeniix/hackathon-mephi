package ru.mephi.tikho.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mephi.tikho.R
import ru.mephi.tikho.data.AudioImporter
import ru.mephi.tikho.model.AudioItem
import ru.mephi.tikho.model.Recording
import ru.mephi.tikho.ui.components.ChevronRightIcon
import ru.mephi.tikho.ui.components.ImportIcon
import ru.mephi.tikho.ui.components.LibraryTopBar
import ru.mephi.tikho.ui.components.MicIcon
import ru.mephi.tikho.ui.components.PrimaryButton
import ru.mephi.tikho.ui.components.PrivacyFooter
import ru.mephi.tikho.ui.components.SecondaryButton
import ru.mephi.tikho.ui.components.SectionHeader
import ru.mephi.tikho.ui.components.SpeakerStack
import ru.mephi.tikho.ui.components.TikhoCard
import ru.mephi.tikho.ui.theme.TikhoColors
import ru.mephi.tikho.ui.theme.TikhoType

@Composable
fun LibraryScreen(
    recordings: List<Recording>,
    onAudioPicked: (AudioItem) -> Unit,
    onOpenRecording: (Recording) -> Unit,
    onStartRecording: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val pickAudio = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            val audio = runCatching { AudioImporter.read(context, uri) }.getOrNull()
            if (audio != null) onAudioPicked(audio)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        LibraryTopBar(
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(top = 22.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item(key = "hero") {
                Text(text = stringResource(R.string.library_title), style = TikhoType.screenTitle)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.library_subtitle),
                    style = TikhoType.body.copy(
                        fontSize = 14.5.sp,
                        lineHeight = 20.sp,
                        color = TikhoColors.TextSecondary,
                    ),
                )
            }
            item(key = "import") {
                Spacer(Modifier.height(8.dp))
                ImportCard(
                    onImport = { pickAudio.launch(arrayOf("audio/*")) },
                    onRecord = onStartRecording,
                )
            }
            item(key = "section") {
                Spacer(Modifier.height(12.dp))
                SectionHeader(
                    title = stringResource(R.string.recent_transcriptions),
                    trailing = recordings.size.toString(),
                )
            }
            if (recordings.isEmpty()) {
                item(key = "empty") {
                    TikhoCard(contentPadding = PaddingValues(18.dp)) {
                        Text(
                            text = stringResource(R.string.empty_recordings),
                            style = TikhoType.body,
                        )
                    }
                }
            } else {
                items(
                    items = recordings,
                    key = { it.id },
                    contentType = { "recording" },
                ) { recording ->
                    RecordingCard(
                        recording = recording,
                        onClick = { onOpenRecording(recording) },
                    )
                }
            }
        }

        PrivacyFooter(text = stringResource(R.string.privacy_library))
        Spacer(Modifier.height(14.dp))
    }
}

@Composable
private fun ImportCard(
    onImport: () -> Unit,
    onRecord: () -> Unit,
) {
    TikhoCard(contentPadding = PaddingValues(18.dp)) {
        Text(text = stringResource(R.string.new_dialog), style = TikhoType.cardTitle)
        Spacer(Modifier.height(7.dp))
        Text(text = stringResource(R.string.import_card_subtitle), style = TikhoType.body)
        Spacer(Modifier.height(18.dp))
        PrimaryButton(
            text = stringResource(R.string.import_record),
            onClick = onImport,
            leadingIcon = { ImportIcon(tint = TikhoColors.OnPrimary, size = 19.dp) },
        )
        Spacer(Modifier.height(10.dp))
        SecondaryButton(
            text = stringResource(R.string.record_with_mic),
            onClick = onRecord,
            leadingIcon = { MicIcon(tint = TikhoColors.Primary, size = 20.dp) },
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.supported_formats),
            style = TikhoType.caption.copy(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = TikhoColors.TextTertiary,
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

@Composable
private fun RecordingCard(
    recording: Recording,
    onClick: () -> Unit,
) {
    TikhoCard(contentPadding = PaddingValues(0.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SpeakerStack(
                participants = recording.participants,
                size = 34.dp,
                max = 3,
            )
            Spacer(Modifier.width(13.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = recording.title,
                    style = TikhoType.rowTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = recording.listSubtitle,
                    style = TikhoType.caption.copy(
                        fontSize = 12.5.sp,
                        lineHeight = 16.sp,
                        color = TikhoColors.TextTertiary,
                        fontWeight = FontWeight.Normal,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            ChevronRightIcon(size = 16.dp)
        }
    }
}
