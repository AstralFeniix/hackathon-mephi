package ru.mephi.tikho.data

import ru.mephi.tikho.model.AudioItem
import ru.mephi.tikho.model.Utterance

/**
 * Точка подключения локального движка распознавания.
 *
 * В приложении используется [ru.mephi.tikho.data.bridge.RustTranscriptionEngine]:
 * Whisper (Rust/whisper.cpp) + диаризация sherpa-onnx.
 */
interface TranscriptionEngine {
    suspend fun transcribe(
        audio: AudioItem,
        onProgress: suspend (TranscriptionProgress) -> Unit,
    ): List<Utterance>
}

data class TranscriptionProgress(
    val fraction: Float,
    val visibleUtterances: List<Utterance>,
)
