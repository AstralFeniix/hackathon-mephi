package ru.mephi.tikho.data.bridge

import android.content.Context
import com.k2fsa.sherpa.onnx.FastClusteringConfig
import com.k2fsa.sherpa.onnx.OfflineSpeakerDiarization
import com.k2fsa.sherpa.onnx.OfflineSpeakerDiarizationConfig
import com.k2fsa.sherpa.onnx.OfflineSpeakerSegmentationModelConfig
import com.k2fsa.sherpa.onnx.OfflineSpeakerSegmentationPyannoteModelConfig
import com.k2fsa.sherpa.onnx.SpeakerEmbeddingExtractorConfig
import uniffi.bridge_core.SpeakerTurn

/**
 * Локальная диаризация: pyannote-сегментация + speaker embeddings через sherpa-onnx.
 * Модели лежат в assets/diarization; вход — 16 кГц mono float samples.
 *
 * Конструктор лёгкий, тяжёлые ONNX-модели инициализируются при первом [label].
 */
class SpeakerLabeler(context: Context) {
    private val assets = context.applicationContext.assets

    private val diarizer by lazy {
        OfflineSpeakerDiarization(
            assetManager = assets,
            config = OfflineSpeakerDiarizationConfig(
                segmentation = OfflineSpeakerSegmentationModelConfig(
                    pyannote = OfflineSpeakerSegmentationPyannoteModelConfig(
                        model = "diarization/segmentation.onnx",
                    ),
                ),
                embedding = SpeakerEmbeddingExtractorConfig(
                    model = "diarization/embedding.onnx",
                ),
                // -1: число говорящих оценивается по порогу, а не задаётся заранее.
                clustering = FastClusteringConfig(numClusters = -1, threshold = CLUSTER_THRESHOLD),
                minDurationOn = MIN_DURATION_ON_S,
                minDurationOff = MIN_DURATION_OFF_S,
            ),
        )
    }

    /** Блокирующий вызов: выполнять на фоновом потоке. */
    fun label(samples: FloatArray): List<SpeakerTurn> =
        diarizer.process(samples).map { segment ->
            SpeakerTurn(
                startMs = (segment.start * 1000f).toLong().coerceAtLeast(0L).toULong(),
                endMs = (segment.end * 1000f).toLong().coerceAtLeast(0L).toULong(),
                speaker = segment.speaker.toUInt(),
            )
        }

    private companion object {
        // Расстояние между кластерами, при котором они считаются одним говорящим.
        // Меньше значение — больше говорящих: 0.4 давало новые id уже знакомым голосам.
        const val CLUSTER_THRESHOLD = 0.5f

        // Короткая речь (сек.) отбрасывается, короткая пауза (сек.) внутри речи заполняется.
        const val MIN_DURATION_ON_S = 0.3f
        const val MIN_DURATION_OFF_S = 0.5f
    }
}
