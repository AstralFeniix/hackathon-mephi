package ru.mephi.tikho.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import ru.mephi.tikho.R
import ru.mephi.tikho.data.formatClock
import ru.mephi.tikho.model.AudioItem
import ru.mephi.tikho.ui.components.AudioRecordingController
import ru.mephi.tikho.ui.components.DetailTopBar
import ru.mephi.tikho.ui.components.MicIcon
import ru.mephi.tikho.ui.components.PrimaryButton
import ru.mephi.tikho.ui.components.PrivacyFooter
import ru.mephi.tikho.ui.components.SecondaryButton
import ru.mephi.tikho.ui.components.StopIcon
import ru.mephi.tikho.ui.components.TikhoCard
import ru.mephi.tikho.ui.theme.TikhoColors
import ru.mephi.tikho.ui.theme.TikhoType
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun RecordingScreen(
    onBack: () -> Unit,
    onRecorded: (AudioItem) -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val controller = remember { AudioRecordingController(context.applicationContext) }
    var permissionDenied by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    DisposableEffect(controller) {
        onDispose { controller.dispose() }
    }

    fun startRecording() {
        if (controller.start()) {
            permissionDenied = false
        } else {
            Toast.makeText(context, R.string.recording_error, Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            startRecording()
        } else {
            permissionDenied = true
        }
    }

    fun requestOrStart() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            startRecording()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun stopAndContinue() {
        val audio = controller.stop()
        if (audio != null) {
            onRecorded(audio)
        } else {
            Toast.makeText(context, R.string.recording_error, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(controller.isRecording) {
        while (controller.isRecording) {
            controller.tick()
            delay(100)
        }
    }

    BackHandler(enabled = controller.isRecording) {
        showDiscardDialog = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        DetailTopBar(
            title = stringResource(R.string.recording_title),
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
                text = stringResource(R.string.recording_heading),
                style = TikhoType.screenTitleCompact,
            )
            Spacer(Modifier.height(7.dp))
            Text(
                text = stringResource(R.string.recording_subtitle),
                style = TikhoType.body,
            )
            Spacer(Modifier.height(20.dp))

            TikhoCard(contentPadding = PaddingValues(vertical = 24.dp, horizontal = 20.dp)) {
                RecordVisual(controller = controller)
                Spacer(Modifier.height(14.dp))
                RecordingTimer(controller = controller)
                Spacer(Modifier.height(5.dp))
                Text(
                    text = if (controller.isRecording) {
                        stringResource(R.string.recording_in_progress)
                    } else {
                        stringResource(R.string.recording_ready)
                    },
                    style = TikhoType.label.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = TikhoColors.TextSecondary,
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.recording_hint),
                    style = TikhoType.caption.copy(
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp,
                        color = TikhoColors.TextTertiary,
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                )
            }

            if (permissionDenied) {
                Spacer(Modifier.height(14.dp))
                PermissionCard(onRetry = { requestOrStart() })
            }

            Spacer(Modifier.height(20.dp))
        }

        if (controller.isRecording) {
            PrimaryButton(
                text = stringResource(R.string.stop_recording),
                onClick = { stopAndContinue() },
                leadingIcon = { StopIcon(tint = TikhoColors.OnPrimary, size = 18.dp) },
            )
            Spacer(Modifier.height(6.dp))
            TextButton(
                onClick = { showDiscardDialog = true },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = TikhoType.label.copy(color = TikhoColors.TextSecondary),
                )
            }
        } else {
            PrimaryButton(
                text = stringResource(R.string.start_recording),
                onClick = { requestOrStart() },
                leadingIcon = { MicIcon(tint = TikhoColors.OnPrimary, size = 20.dp) },
            )
        }
        Spacer(Modifier.height(10.dp))
        PrivacyFooter(text = stringResource(R.string.privacy_recording))
        Spacer(Modifier.height(14.dp))
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.discard_recording_title),
                    style = TikhoType.cardTitle,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.discard_recording_message),
                    style = TikhoType.body,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        controller.cancel()
                        onBack()
                    },
                ) {
                    Text(
                        text = stringResource(R.string.discard),
                        style = TikhoType.label.copy(color = TikhoColors.Primary),
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(
                        text = stringResource(R.string.keep_recording),
                        style = TikhoType.label.copy(color = TikhoColors.TextSecondary),
                    )
                }
            },
            containerColor = TikhoColors.Surface,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        )
    }
}

@Composable
private fun RecordVisual(controller: AudioRecordingController) {
    if (controller.isRecording) {
        RecordingPulse()
    } else {
        IdleMic()
    }
}

@Composable
private fun IdleMic() {
    val primaryColor = TikhoColors.Primary
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(158.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(150.dp)) {
            drawCircle(
                color = primaryColor.copy(alpha = 0.10f),
                radius = size.minDimension * 0.47f,
                style = Stroke(width = 1.dp.toPx()),
            )
            drawCircle(
                color = primaryColor.copy(alpha = 0.06f),
                radius = size.minDimension * 0.37f,
                style = Stroke(width = 1.dp.toPx()),
            )
        }
        MicCircle(size = 92.dp, iconSize = 36.dp)
    }
}

@Composable
private fun RecordingPulse() {
    val primaryColor = TikhoColors.Primary
    val transition = rememberInfiniteTransition(label = "recording-pulse")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulse",
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(158.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(150.dp)) {
            for (ring in 0..2) {
                val progress = (pulse + ring / 3f) % 1f
                drawCircle(
                    color = primaryColor.copy(alpha = 0.20f * (1f - progress)),
                    radius = size.minDimension * (0.32f + 0.18f * progress),
                    style = Stroke(width = 1.4.dp.toPx()),
                )
            }
        }
        MicCircle(size = 92.dp, iconSize = 36.dp)
    }
}

@Composable
private fun MicCircle(size: androidx.compose.ui.unit.Dp, iconSize: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(TikhoColors.PrimaryGradient),
                shape = CircleShape,
            )
            .border(1.4.dp, Color.White.copy(alpha = 0.55f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        MicIcon(tint = TikhoColors.OnPrimary, size = iconSize)
    }
}

@Composable
private fun RecordingTimer(controller: AudioRecordingController) {
    val label by remember(controller) {
        androidx.compose.runtime.derivedStateOf { formatClock(controller.elapsedMs) }
    }
    Text(
        text = label,
        style = TikhoType.screenTitleLarge.copy(
            fontSize = 40.sp,
            lineHeight = 46.sp,
            letterSpacing = 0.sp,
            fontFeatureSettings = "tnum",
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    if (controller.isRecording) {
        Spacer(Modifier.height(8.dp))
        RecordingBars()
    }
}

@Composable
private fun RecordingBars() {
    val barColor = TikhoColors.Primary
    val transition = rememberInfiniteTransition(label = "recording-bars")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "bars",
    )
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp),
    ) {
        val bars = 28
        val gap = 3.dp.toPx()
        val width = (size.width - gap * (bars - 1)) / bars
        val centerY = size.height / 2f
        for (i in 0 until bars) {
            val wave = abs(sin(phase + i * 0.55f))
            val height = size.height * (0.18f + wave * 0.78f)
            val x = i * (width + gap) + width / 2f
            drawLine(
                color = barColor.copy(alpha = 0.38f + wave * 0.45f),
                start = Offset(x, centerY - height / 2f),
                end = Offset(x, centerY + height / 2f),
                strokeWidth = width.coerceAtMost(2.2.dp.toPx()),
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun PermissionCard(onRetry: () -> Unit) {
    TikhoCard(contentPadding = PaddingValues(18.dp)) {
        Text(
            text = stringResource(R.string.recording_permission_title),
            style = TikhoType.cardTitle,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.recording_permission_message),
            style = TikhoType.body,
        )
        Spacer(Modifier.height(14.dp))
        SecondaryButton(
            text = stringResource(R.string.recording_permission_action),
            onClick = onRetry,
            leadingIcon = { MicIcon(tint = TikhoColors.Primary, size = 19.dp) },
        )
    }
}
