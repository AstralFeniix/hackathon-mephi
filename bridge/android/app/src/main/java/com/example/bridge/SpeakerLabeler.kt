package com.example.bridge

import android.content.Context
import com.k2fsa.sherpa.onnx.FastClusteringConfig
import com.k2fsa.sherpa.onnx.OfflineSpeakerDiarization
import com.k2fsa.sherpa.onnx.OfflineSpeakerDiarizationConfig
import com.k2fsa.sherpa.onnx.OfflineSpeakerSegmentationModelConfig
import com.k2fsa.sherpa.onnx.OfflineSpeakerSegmentationPyannoteModelConfig
import com.k2fsa.sherpa.onnx.SpeakerEmbeddingExtractorConfig
import uniffi.bridge_core.SpeakerTurn

/**
 * Finds who speaks when, using pyannote-style segmentation and speaker embeddings from sherpa-onnx.
 * Models live in assets/diarization. Input must be 16 kHz mono samples.
 */
class SpeakerLabeler(context: Context) {
    private val diarizer by lazy {
        OfflineSpeakerDiarization(
            assetManager = context.assets,
            config = OfflineSpeakerDiarizationConfig(
                segmentation = OfflineSpeakerSegmentationModelConfig(
                    pyannote = OfflineSpeakerSegmentationPyannoteModelConfig(
                        model = "diarization/segmentation.onnx",
                    ),
                ),
                embedding = SpeakerEmbeddingExtractorConfig(
                    model = "diarization/embedding.onnx",
                ),
                // numClusters -1 means the number of speakers is estimated from the threshold.
                clustering = FastClusteringConfig(numClusters = -1, threshold = 0.5f),
            ),
        )
    }

    /** Blocking call: run it on a background thread. */
    fun label(samples: FloatArray): List<SpeakerTurn> =
        diarizer.process(samples).map { segment ->
            SpeakerTurn(
                startMs = (segment.start * 1000f).toLong().toULong(),
                endMs = (segment.end * 1000f).toLong().toULong(),
                speaker = segment.speaker.toUInt(),
            )
        }
}
