package ru.mephi.tikho.data

import kotlinx.coroutines.delay
import ru.mephi.tikho.model.AudioItem
import ru.mephi.tikho.model.Utterance

/**
 * Точка подключения локального движка распознавания.
 *
 * В приложении используется [ru.mephi.tikho.data.bridge.RustTranscriptionEngine]:
 * Whisper (Rust/whisper.cpp) + диаризация sherpa-onnx. [DemoTranscriptionEngine]
 * оставлен только для UI-тестов и превью, где не нужен прогон модели.
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

/**
 * Демонстрационный движок: 22 секунды «обработки», реплики появляются
 * по мере приближения прогресса к их таймкодам. Не используется в проде.
 */
class DemoTranscriptionEngine : TranscriptionEngine {

    override suspend fun transcribe(
        audio: AudioItem,
        onProgress: suspend (TranscriptionProgress) -> Unit,
    ): List<Utterance> {
        val scale = if (audio.durationMillis > 0L) {
            (audio.durationMillis.toDouble() / DemoData.demoAudio.durationMillis.toDouble()).toFloat()
        } else {
            1f
        }
        val utterances = DemoData.demoUtterances.map { utterance ->
            utterance.copy(
                startMs = (utterance.startMs * scale).toLong(),
                endMs = (utterance.endMs * scale).toLong(),
            )
        }
        val revealAt = listOf(0f, 0.06f, 0.14f, 0.28f, 0.58f, 0.72f)
        val totalSteps = 180
        for (step in 0..totalSteps) {
            val fraction = step / totalSteps.toFloat()
            val visible = utterances.filterIndexed { index, _ ->
                fraction >= (revealAt.getOrNull(index) ?: 1f)
            }
            onProgress(
                TranscriptionProgress(
                    fraction = fraction,
                    visibleUtterances = visible,
                ),
            )
            delay(120)
        }
        onProgress(TranscriptionProgress(1f, utterances))
        return utterances
    }
}
