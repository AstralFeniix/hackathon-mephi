package ru.mephi.tikho.data.bridge

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import ru.mephi.tikho.model.AudioItem
import uniffi.bridge_core.Engine
import uniffi.bridge_core.decodeToPcm
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID

/**
 * Держит тяжёлые нативные ресурсы: Whisper-модель, sherpa-onnx диаризацию
 * и временные файлы. Создаётся один раз на приложение.
 */
internal class BridgeRuntime(context: Context) {

    private val appContext = context.applicationContext
    private val initMutex = Mutex()

    @Volatile
    private var engine: Engine? = null

    @Volatile
    private var labeler: SpeakerLabeler? = null

    suspend fun ensureEngine(onProgress: suspend (Float) -> Unit): Engine {
        engine?.let { return it }
        return initMutex.withLock {
            engine ?: run {
                onProgress(0.03f)
                val model = withContext(Dispatchers.IO) { copyModelFromAssets() }
                onProgress(0.07f)
                val created = withContext(Dispatchers.Default) {
                    Engine(model.absolutePath)
                }
                engine = created
                onProgress(0.12f)
                created
            }
        }
    }

    suspend fun ensureLabeler(onProgress: suspend (Float) -> Unit): SpeakerLabeler {
        labeler?.let { return it }
        return initMutex.withLock {
            labeler ?: run {
                onProgress(0.80f)
                withContext(Dispatchers.Default) {
                    SpeakerLabeler(appContext)
                }.also { labeler = it }
            }
        }
    }

    /**
     * Возвращает локальный файл, который умеет открыть Symphonia:
     * WAV/MP3 напрямую, content:// — через копию в кэш.
     */
    fun resolveAudioFile(audio: AudioItem): File {
        val uriString = audio.uriString
            ?: throw IllegalStateException("У записи нет аудиофайла")
        val uri = Uri.parse(uriString)
        val file = when (uri.scheme) {
            "file" -> File(uri.path ?: error("Некорректный file:// URI"))
            "content" -> copyContentToCache(uri, audio.fileName)
            null -> File(uriString)
            else -> {
                if (uriString.startsWith("/")) File(uriString)
                else throw IllegalStateException("Неподдерживаемый источник аудио: ${uri.scheme}")
            }
        }
        if (!file.exists() || file.length() == 0L) {
            throw IllegalStateException("Аудиофайл не найден или пуст")
        }
        return file
    }

    /** Декодирует WAV/MP3 в сырой поток f32 16 кГц mono. */
    fun decodeToPcm(input: File): File {
        val output = File(cacheDir(), "bridge-${UUID.randomUUID()}.pcm")
        val count = decodeToPcm(input.absolutePath, output.absolutePath)
        if (count == 0UL || output.length() < 4L) {
            output.delete()
            throw IllegalStateException("В аудиофайле не найдено звука")
        }
        return output
    }

    fun readPcm(file: File): FloatArray {
        val bytes = file.readBytes()
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer()
        return FloatArray(buffer.remaining()).also { buffer.get(it) }
    }

    fun cleanup(vararg files: File?) {
        files.forEach { file ->
            runCatching { file?.delete() }
        }
    }

    private fun cacheDir(): File = File(appContext.cacheDir, "bridge").apply { mkdirs() }

    private fun copyModelFromAssets(): File {
        val target = File(appContext.filesDir, MODEL_NAME)
        val expectedSize = runCatching {
            appContext.assets.openFd(MODEL_NAME).use { it.length }
        }.getOrDefault(-1L)

        if (target.exists() && target.length() > 0L &&
            (expectedSize < 0L || target.length() == expectedSize)
        ) {
            return target
        }

        val tmp = File(appContext.filesDir, "$MODEL_NAME.tmp")
        appContext.assets.open(MODEL_NAME).use { input ->
            tmp.outputStream().use { output ->
                input.copyTo(output, 64 * 1024)
            }
        }
        if (!tmp.renameTo(target)) {
            tmp.copyTo(target, overwrite = true)
            tmp.delete()
        }
        Log.i(TAG, "Whisper model ready: ${target.length()} bytes")
        return target
    }

    private fun copyContentToCache(uri: Uri, fallbackName: String): File {
        val displayName = appContext.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        } ?: fallbackName

        val safeName = displayName
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .ifBlank { "audio" }
        val target = File(cacheDir(), "input-${UUID.randomUUID()}-$safeName")
        val input = appContext.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Не удалось открыть выбранный файл")
        input.use { source ->
            target.outputStream().use { destination ->
                source.copyTo(destination, 64 * 1024)
            }
        }
        return target
    }

    private companion object {
        const val TAG = "TikhoBridge"
        const val MODEL_NAME = "ggml-base.bin"
    }
}
