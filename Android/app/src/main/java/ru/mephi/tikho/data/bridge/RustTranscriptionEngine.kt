package ru.mephi.tikho.data.bridge

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import ru.mephi.tikho.data.TranscriptionEngine
import ru.mephi.tikho.data.TranscriptionProgress
import ru.mephi.tikho.model.AudioItem
import ru.mephi.tikho.model.Utterance
import uniffi.bridge_core.Segment
import uniffi.bridge_core.Session
import uniffi.bridge_core.labelSpeakers
import kotlin.math.min

/**
 * Реальный локальный движок: Rust-ядро (whisper.cpp через whisper-rs) +
 * sherpa-onnx (pyannote-сегментация и speaker embeddings).
 *
 * Файл декодируется в PCM 16 кГц, нарезается по 5 секунд и отдаётся в [Session].
 * Это даёт честный прогресс по обработанному аудио и промежуточные реплики.
 * Финальная диаризация выполняется по всему файлу и переносит speaker_id на реплики.
 */
class RustTranscriptionEngine(context: Context) : TranscriptionEngine {

    private val bridge = BridgeRuntime(context.applicationContext)

    override suspend fun transcribe(
        audio: AudioItem,
        onProgress: suspend (TranscriptionProgress) -> Unit,
    ): List<Utterance> = withContext(Dispatchers.Default) {
        val input = bridge.resolveAudioFile(audio)
        val pcm = bridge.decodeToPcm(input)

        try {
            onProgress(TranscriptionProgress(0.10f, emptyList()))
            val engine = bridge.ensureEngine { fraction ->
                onProgress(TranscriptionProgress(fraction, emptyList()))
            }
            val samples = bridge.readPcm(pcm)
            if (samples.isEmpty()) {
                throw IllegalStateException("В аудиофайле не найдено звука")
            }

            val session = Session(engine)
            session.use {
                val finalized = ArrayList<Utterance>()
                var offset = 0
                val chunkSamples = SAMPLE_RATE * 5
                val total = samples.size

                while (offset < total) {
                    currentCoroutineContext().ensureActive()
                    val end = min(offset + chunkSamples, total)
                    val chunk = samples.copyOfRange(offset, end)
                    val update = session.pushAudio(chunk.asList(), SAMPLE_RATE.toUInt())
                    finalized += update.finalized.map { it.toUtterance() }
                    val fraction = 0.12f + 0.60f * (end.toFloat() / total.toFloat())
                    onProgress(
                        TranscriptionProgress(
                            fraction = fraction.coerceIn(0f, 0.72f),
                            visibleUtterances = finalized.toList(),
                        ),
                    )
                    offset = end
                }

                val transcript = session.finish()
                val rawUtterances = transcript.segments.toUtterances()
                onProgress(TranscriptionProgress(0.78f, rawUtterances))

                val labeler = bridge.ensureLabeler { fraction ->
                    onProgress(TranscriptionProgress(fraction, rawUtterances))
                }
                onProgress(TranscriptionProgress(0.84f, rawUtterances))
                val turns = labeler.label(samples)
                val labeled = labelSpeakers(transcript, turns)
                val utterances = labeled.segments.toUtterances()

                onProgress(TranscriptionProgress(1f, utterances))
                utterances
            }
        } finally {
            bridge.cleanup(pcm)
        }
    }

    private fun List<Segment>.toUtterances(): List<Utterance> =
        mapNotNull { it.toUtterance().takeIf { utterance -> utterance.text.isNotBlank() } }

    private fun Segment.toUtterance(): Utterance {
        val start = startMs.toLong().coerceAtLeast(0L)
        val end = endMs.toLong().coerceAtLeast(start)
        return Utterance(
            speakerId = "speaker_${speaker.toInt() + 1}",
            startMs = start,
            endMs = end,
            text = text.trim(),
        )
    }

    private companion object {
        const val SAMPLE_RATE = 16_000
    }
}
