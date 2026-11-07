package com.example.bridge

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uniffi.bridge_core.Engine
import uniffi.bridge_core.Segment
import uniffi.bridge_core.Session
import uniffi.bridge_core.decodeToPcm
import uniffi.bridge_core.labelSpeakers
import uniffi.bridge_core.transcriptToJson
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt

/** Intent contract for other Android apps. See README for an example. */
object BridgeIntents {
    /** Start with startActivityForResult(); audio is passed as the intent data URI. */
    const val ACTION_TRANSCRIBE = "com.example.bridge.action.TRANSCRIBE"

    /** String extra in the result: the transcript JSON. */
    const val EXTRA_TRANSCRIPT_JSON = "com.example.bridge.extra.TRANSCRIPT_JSON"
}

private const val TAG = "Bridge"
private const val MODEL_NAME = "ggml-base.bin"
private const val SAMPLE_RATE = 16_000
private val AUDIO_MIME_TYPES = arrayOf("audio/mpeg", "audio/wav", "audio/x-wav")

data class TranscribeRequest(val uri: Uri, val returnToCaller: Boolean)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val request = parseRequest(intent)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BridgeScreen(request)
                }
            }
        }
    }

    private fun parseRequest(intent: Intent): TranscribeRequest? = when (intent.action) {
        BridgeIntents.ACTION_TRANSCRIBE -> intent.data?.let { TranscribeRequest(it, returnToCaller = true) }
        Intent.ACTION_SEND -> IntentCompat
            .getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
            ?.let { TranscribeRequest(it, returnToCaller = false) }
        else -> null
    }
}

/** Copies the bundled model out of the APK once, so whisper.cpp can open it by path. */
private fun copyModelFromAssets(context: Context, name: String): String {
    val target = File(context.filesDir, name)
    val expectedSize = context.assets.openFd(name).use { it.length }
    if (target.length() != expectedSize) {
        val tmp = File(context.filesDir, "$name.tmp")
        context.assets.open(name).use { input -> tmp.outputStream().use { input.copyTo(it) } }
        tmp.renameTo(target)
    }
    return target.absolutePath
}

/** Copies a content:// URI into the app cache, because the decoder needs a file path. */
private fun copyToCache(context: Context, uri: Uri): File {
    val name = context.contentResolver
        .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
        ?: "audio"
    val dir = File(context.cacheDir, "imports").apply { mkdirs() }
    val target = File(dir, name.replace(Regex("[^A-Za-z0-9._-]"), "_"))
    val input = context.contentResolver.openInputStream(uri) ?: error("cannot open $uri")
    input.use { source -> target.outputStream().use { source.copyTo(it) } }
    return target
}

/** Reads a raw file of little-endian f32 samples written by decodeToPcm. */
private fun readPcm(file: File): FloatArray {
    val buffer = ByteBuffer.wrap(file.readBytes()).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer()
    return FloatArray(buffer.remaining()).also { buffer.get(it) }
}

private fun finishWithResult(context: Context, json: String?) {
    val activity = context as? Activity ?: return
    if (json == null) {
        activity.setResult(Activity.RESULT_CANCELED)
    } else {
        activity.setResult(
            Activity.RESULT_OK,
            Intent().putExtra(BridgeIntents.EXTRA_TRANSCRIPT_JSON, json),
        )
    }
    activity.finish()
}

private fun formatSeconds(ms: ULong): String = "%.2fs".format(ms.toDouble() / 1000.0)

private fun rmsLevel(samples: FloatArray): Double =
    sqrt(samples.sumOf { (it * it).toDouble() } / samples.size)

@Composable
fun BridgeScreen(request: TranscribeRequest?) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val capture = remember { AudioCapture() }
    val labeler = remember { SpeakerLabeler(context) }

    val engineResult by produceState<Result<Engine>?>(initialValue = null) {
        value = runCatching {
            val modelPath = withContext(Dispatchers.IO) { copyModelFromAssets(context, MODEL_NAME) }
            withContext(Dispatchers.Default) { Engine(modelPath) }
        }
    }

    var recording by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var handledRequest by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Idle") }
    var segments by remember { mutableStateOf<List<Segment>>(emptyList()) }
    var partial by remember { mutableStateOf<Segment?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var json by remember { mutableStateOf<String?>(null) }
    var session by remember { mutableStateOf<Session?>(null) }
    var consumer by remember { mutableStateOf<Job?>(null) }

    fun engineOrReport(): Engine? {
        engineResult?.getOrNull()?.let { return it }
        status = engineResult?.exceptionOrNull()?.let { "Model load failed: ${it.javaClass.simpleName}: ${it.message}" }
            ?: "Model still loading, try again in a moment"
        return null
    }

    fun showResult(segs: List<Segment>, text: String) {
        segments = segs
        json = text
        status = "Done: ${segs.size} segments"
    }

    fun startLive(engine: Engine) {
        val current = Session(engine)
        session = current
        segments = emptyList()
        partial = null
        json = null
        error = null
        val chunks = capture.start(scope)
        recording = true
        status = "Recording..."
        consumer = scope.launch {
            var received = 0
            var processed = 0
            for (chunk in chunks) {
                received += chunk.size
                val started = System.nanoTime()
                val result = runCatching {
                    withContext(Dispatchers.Default) { current.pushAudio(chunk.asList(), SAMPLE_RATE.toUInt()) }
                }
                result
                    .onSuccess { update ->
                        processed += chunk.size
                        segments = segments + update.finalized
                        partial = update.partial
                        Log.d(TAG, "push took ${(System.nanoTime() - started) / 1_000_000} ms, final=${update.finalized.size}, partial=${update.partial?.text}")
                    }
                    .onFailure { e ->
                        Log.e(TAG, "push failed", e)
                        error = e.message ?: e.javaClass.simpleName
                    }
                status = "Recording... %.1f s captured, %.1f s processed, level %.3f".format(
                    received / SAMPLE_RATE.toDouble(),
                    processed / SAMPLE_RATE.toDouble(),
                    rmsLevel(chunk),
                )
            }
        }
    }

    fun stopLive() {
        val current = session ?: return
        recording = false
        busy = true
        partial = null
        status = "Finishing..."
        scope.launch {
            val recorded = capture.stop()
            consumer?.join()
            status = "Finding speakers..."
            val result = runCatching {
                withContext(Dispatchers.Default) {
                    val transcript = current.finish()
                    val turns = if (recorded.isEmpty()) emptyList() else labeler.label(recorded)
                    val labeled = labelSpeakers(transcript, turns)
                    labeled.segments to transcriptToJson(labeled)
                }
            }
            busy = false
            result
                .onSuccess { (segs, text) -> showResult(segs, text) }
                .onFailure { status = "Error: ${it.message}" }
        }
    }

    fun transcribeUri(engine: Engine, uri: Uri, returnToCaller: Boolean) {
        busy = true
        status = "Decoding audio..."
        scope.launch {
            val result = runCatching {
                val input = withContext(Dispatchers.IO) { copyToCache(context, uri) }
                val pcm = File(input.parentFile, "${input.name}.pcm")
                withContext(Dispatchers.Default) { decodeToPcm(input.absolutePath, pcm.absolutePath) }
                val samples = withContext(Dispatchers.IO) { readPcm(pcm) }

                status = "Transcribing and finding speakers..."
                withContext(Dispatchers.Default) {
                    val current = Session(engine)
                    current.pushPcmFile(pcm.absolutePath)
                    val labeled = labelSpeakers(current.finish(), labeler.label(samples))
                    labeled.segments to transcriptToJson(labeled)
                }
            }
            busy = false
            result
                .onSuccess { (segs, text) ->
                    showResult(segs, text)
                    if (returnToCaller) finishWithResult(context, text)
                }
                .onFailure {
                    status = "Error: ${it.message}"
                    if (returnToCaller) finishWithResult(context, null)
                }
        }
    }

    fun beginLive() {
        engineOrReport()?.let { startLive(it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) beginLive() else status = "Microphone permission denied"
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) engineOrReport()?.let { transcribeUri(it, uri, returnToCaller = false) }
    }

    LaunchedEffect(request, engineResult) {
        val engine = engineResult?.getOrNull()
        if (request != null && !handledRequest) {
            if (engine != null) {
                handledRequest = true
                transcribeUri(engine, request.uri, request.returnToCaller)
            } else if (engineResult != null && request.returnToCaller) {
                handledRequest = true
                finishWithResult(context, null)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                enabled = !busy,
                onClick = {
                    when {
                        recording -> stopLive()
                        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                            PackageManager.PERMISSION_GRANTED -> beginLive()
                        else -> permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
            ) {
                Text(if (recording) "Stop" else "Record")
            }
            Button(
                enabled = !recording && !busy,
                onClick = { filePicker.launch(AUDIO_MIME_TYPES) },
            ) {
                Text("Import file")
            }
        }

        Text(status)
        error?.let { Text("Error: $it", color = Color.Red) }

        val listState = rememberLazyListState()
        LaunchedEffect(segments.size, partial) {
            val count = segments.size + (if (partial != null) 1 else 0)
            if (count > 0) listState.animateScrollToItem(count - 1)
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(segments) { seg ->
                Text("[${formatSeconds(seg.startMs)} - ${formatSeconds(seg.endMs)}] speaker_${seg.speaker + 1u}: ${seg.text}")
            }
            partial?.let { p ->
                item {
                    Text("speaker_${p.speaker + 1u}: ${p.text}", color = Color.Gray)
                }
            }
            json?.let { text ->
                item {
                    SelectionContainer {
                        Text(text, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
