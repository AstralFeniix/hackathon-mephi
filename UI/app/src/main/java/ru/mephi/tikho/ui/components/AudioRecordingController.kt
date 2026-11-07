package ru.mephi.tikho.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.SystemClock
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import ru.mephi.tikho.data.formatFileSize
import ru.mephi.tikho.model.AudioItem
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

/**
 * Локальная запись с микрофона в WAV 16 кГц mono PCM16.
 *
 * Именно такой формат без перекодирования принимает Rust-ядро (Symphonia),
 * а частота 16 кГц совпадает с входом Whisper и sherpa-onnx.
 */
@Stable
class AudioRecordingController(
    private val context: Context,
) {
    private var record: AudioRecord? = null
    private var outputFile: File? = null
    private var writer: RandomAccessFile? = null
    private var worker: Thread? = null
    private var startedAt = 0L
    private val bytesWritten = AtomicLong(0L)

    @Volatile
    private var running = false

    @Volatile
    private var workerError = false

    var isRecording by mutableStateOf(false)
        private set

    var elapsedMs by mutableLongStateOf(0L)
        private set

    var hasError by mutableStateOf(false)
        private set

    @SuppressLint("MissingPermission")
    fun start(): Boolean {
        if (isRecording) return true
        hasError = false
        workerError = false
        bytesWritten.set(0L)

        val minBuffer = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        if (minBuffer <= 0) {
            hasError = true
            return false
        }
        val bufferSize = minBuffer * 2
        val newRecord = try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
            )
        } catch (_: Exception) {
            hasError = true
            return false
        }
        if (newRecord.state != AudioRecord.STATE_INITIALIZED) {
            runCatching { newRecord.release() }
            hasError = true
            return false
        }

        val directory = File(context.cacheDir, "recordings").apply { mkdirs() }
        val file = File(directory, "record_${System.currentTimeMillis()}.wav")
        val fileWriter = try {
            RandomAccessFile(file, "rw").apply { write(ByteArray(WAV_HEADER_SIZE)) }
        } catch (_: Exception) {
            runCatching { newRecord.release() }
            file.delete()
            hasError = true
            return false
        }

        return try {
            newRecord.startRecording()
            record = newRecord
            outputFile = file
            writer = fileWriter
            startedAt = SystemClock.elapsedRealtime()
            elapsedMs = 0L
            running = true
            isRecording = true
            worker = Thread({ readLoop(newRecord, fileWriter, bufferSize) }, "tikho-wav-recorder").apply {
                isDaemon = true
                start()
            }
            true
        } catch (_: Exception) {
            running = false
            runCatching { newRecord.release() }
            runCatching { fileWriter.close() }
            file.delete()
            hasError = true
            false
        }
    }

    fun tick() {
        if (isRecording) {
            elapsedMs = (SystemClock.elapsedRealtime() - startedAt).coerceAtLeast(0L)
            if (workerError) {
                hasError = true
            }
        }
    }

    @Synchronized
    fun stop(): AudioItem? {
        if (!isRecording) return null
        tick()
        isRecording = false
        running = false

        val recordToStop = record
        record = null
        runCatching { recordToStop?.stop() }
        worker?.join(1_000)
        if (worker?.isAlive == true) {
            runCatching { recordToStop?.release() }
            worker?.join(1_000)
        }
        worker = null
        runCatching { recordToStop?.release() }

        val file = outputFile
        outputFile = null
        val fileWriter = writer
        writer = null
        val dataSize = bytesWritten.get()
        if (fileWriter != null) {
            runCatching {
                writeWavHeader(fileWriter, dataSize)
                fileWriter.close()
            }
        }

        if (workerError || file == null || !file.exists() || dataSize == 0L) {
            file?.delete()
            hasError = true
            return null
        }

        val actualDuration = (dataSize * 1000L / (SAMPLE_RATE * 2L)).coerceAtLeast(0L)
        val duration = actualDuration.takeIf { it > 0L } ?: elapsedMs
        elapsedMs = duration
        val time = SimpleDateFormat("HH:mm", Locale.forLanguageTag("ru-RU")).format(Date())
        return AudioItem(
            id = UUID.randomUUID().toString(),
            fileName = "Разговор $time.wav",
            displayName = "Разговор в $time",
            durationMillis = duration,
            sizeLabel = formatFileSize(file.length()),
            language = "Авто",
            uriString = file.toUri().toString(),
        )
    }

    fun cancel() {
        isRecording = false
        running = false
        val recordToStop = record
        record = null
        runCatching { recordToStop?.stop() }
        worker?.join(1_000)
        if (worker?.isAlive == true) {
            runCatching { recordToStop?.release() }
            worker?.join(1_000)
        }
        worker = null
        runCatching { recordToStop?.release() }
        runCatching { writer?.close() }
        writer = null
        outputFile?.delete()
        outputFile = null
        bytesWritten.set(0L)
        elapsedMs = 0L
    }

    fun dispose() {
        if (isRecording) {
            cancel()
        } else {
            runCatching { record?.release() }
            record = null
            runCatching { writer?.close() }
            writer = null
        }
    }

    private fun readLoop(record: AudioRecord, file: RandomAccessFile, bufferSize: Int) {
        val buffer = ByteArray(maxOf(bufferSize, 4096))
        try {
            while (running) {
                val read = record.read(buffer, 0, buffer.size)
                if (read > 0) {
                    synchronized(file) {
                        file.write(buffer, 0, read)
                    }
                    bytesWritten.addAndGet(read.toLong())
                } else if (read < 0) {
                    workerError = true
                    break
                }
            }
        } catch (_: Throwable) {
            workerError = true
        }
    }

    private fun writeWavHeader(file: RandomAccessFile, dataSize: Long) {
        val safeSize = dataSize.coerceAtMost(Int.MAX_VALUE.toLong() - WAV_HEADER_SIZE).toInt()
        val header = ByteBuffer.allocate(WAV_HEADER_SIZE).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray(Charsets.US_ASCII))
            putInt(36 + safeSize)
            put("WAVE".toByteArray(Charsets.US_ASCII))
            put("fmt ".toByteArray(Charsets.US_ASCII))
            putInt(16)
            putShort(1)
            putShort(1)
            putInt(SAMPLE_RATE)
            putInt(SAMPLE_RATE * 2)
            putShort(2)
            putShort(16)
            put("data".toByteArray(Charsets.US_ASCII))
            putInt(safeSize)
        }
        file.seek(0)
        file.write(header.array())
    }

    private companion object {
        const val SAMPLE_RATE = 16_000
        const val WAV_HEADER_SIZE = 44
    }
}
