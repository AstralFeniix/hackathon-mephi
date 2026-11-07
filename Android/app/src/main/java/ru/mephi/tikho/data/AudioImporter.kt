package ru.mephi.tikho.data

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import ru.mephi.tikho.model.AudioItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Читает из content:// URI всё, что нужно карточке аудио:
 * имя, размер и длительность.
 */
object AudioImporter {

    fun read(context: Context, uri: Uri): AudioItem {
        var displayName = "Аудиофайл"
        var sizeBytes = 0L

        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex >= 0) {
                    cursor.getString(nameIndex)?.takeIf { it.isNotBlank() }?.let { displayName = it }
                }
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                    sizeBytes = cursor.getLong(sizeIndex)
                }
            }
        }

        val durationMillis = readDuration(context, uri)
        val baseName = displayName
            .substringBeforeLast('.', displayName)
            .replace('_', ' ')
            .replace(Regex("\\s+"), " ")
            .trim()
            .ifBlank { "Аудиофайл" }

        return AudioItem(
            id = UUID.randomUUID().toString(),
            fileName = displayName,
            displayName = baseName,
            durationMillis = durationMillis.coerceAtLeast(0L),
            sizeLabel = formatFileSize(sizeBytes),
            language = "Авто",
            uriString = uri.toString(),
        )
    }

    private fun readDuration(context: Context, uri: Uri): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
                ?: 0L
        } catch (_: Exception) {
            0L
        } finally {
            runCatching { retriever.release() }
        }
    }
}

fun todayLabel(): String {
    val time = SimpleDateFormat("HH:mm", Locale.forLanguageTag("ru-RU")).format(Date())
    return "Сегодня, $time"
}
