package com.example.bridge

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Records mono 16-bit PCM and streams it as 16 kHz f32 chunks, which is what bridge-core expects. */
class AudioCapture(private val sampleRate: Int = 16_000) {
    private var record: AudioRecord? = null
    private var job: Job? = null
    private var channel: Channel<FloatArray>? = null
    private val pieces = mutableListOf<FloatArray>()

    /**
     * Starts recording. The returned channel delivers audio chunks in order
     * and closes after [stop]. Caller must hold the RECORD_AUDIO permission.
     */
    @SuppressLint("MissingPermission")
    fun start(scope: CoroutineScope): ReceiveChannel<FloatArray> {
        val minBuf = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        val rec = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBuf * 2,
        )
        val out = Channel<FloatArray>(Channel.UNLIMITED)
        pieces.clear()
        rec.startRecording()
        record = rec
        channel = out

        job = scope.launch(Dispatchers.IO) {
            val bytes = ByteArray(minBuf)
            while (isActive) {
                val n = rec.read(bytes, 0, bytes.size)
                if (n > 0) {
                    val samples = pcm16ToFloat(bytes, n)
                    pieces.add(samples)
                    out.trySend(samples)
                }
            }
        }
        return out
    }

    /** Stops recording and returns everything captured, for speaker labeling. */
    suspend fun stop(): FloatArray {
        val rec = record ?: return FloatArray(0)
        record = null
        rec.stop() // unblocks the pending read()
        job?.cancelAndJoin()
        rec.release()
        channel?.close()
        channel = null

        val total = pieces.sumOf { it.size }
        val all = FloatArray(total)
        var offset = 0
        for (piece in pieces) {
            piece.copyInto(all, offset)
            offset += piece.size
        }
        pieces.clear()
        return all
    }

    private fun pcm16ToFloat(bytes: ByteArray, length: Int): FloatArray {
        val out = FloatArray(length / 2)
        val bb = ByteBuffer.wrap(bytes, 0, length).order(ByteOrder.LITTLE_ENDIAN)
        for (i in out.indices) {
            out[i] = bb.short / 32768f
        }
        return out
    }
}
