package ru.mephi.tikho.model

import androidx.compose.runtime.Immutable
import org.json.JSONArray
import org.json.JSONObject
import ru.mephi.tikho.data.formatClock

/**
 * Аудио, выбранное пользователем или записанное в приложении.
 *
 * [uriString] — content:// из Storage Access Framework либо file:// для
 * записи, созданной внутри приложения.
 */
@Immutable
data class AudioItem(
    val id: String,
    val fileName: String,
    val displayName: String,
    val durationMillis: Long,
    val sizeLabel: String,
    val language: String = "Авто",
    val uriString: String? = null,
) {
    val durationLabel: String
        get() = formatClock(durationMillis)
}

@Immutable
data class Speaker(
    val id: String,
    val name: String,
)

@Immutable
data class Utterance(
    val speakerId: String,
    val startMs: Long,
    val endMs: Long,
    val text: String,
)

/**
 * Готовая расшифровка диалога. Хранит участников и реплики с временными
 * метками — именно эти данные нужны экрану результата и JSON-экспорту.
 */
@Immutable
data class Recording(
    val id: String,
    val title: String,
    val fileName: String,
    val dateLabel: String,
    val durationLabel: String,
    val durationMillis: Long,
    val language: String,
    val participants: List<Speaker>,
    val utterances: List<Utterance>,
    val uriString: String? = null,
) {
    val metaLine: String
        get() = "$durationLabel, $language, ${speakersLabel(participants.size)}"

    val listSubtitle: String
        get() = "$dateLabel, $durationLabel, ${speakersLabel(participants.size)}"

    val utteranceCount: Int
        get() = utterances.size

    val plainText: String
        get() = utterances.joinToString(separator = "\n\n") { utterance ->
            val speaker = speakerName(utterance.speakerId)
            "${utterance.timeRange()}, $speaker\n${utterance.text}"
        }

    fun speakerName(speakerId: String): String =
        participants.firstOrNull { it.id == speakerId }?.name ?: speakerId

    fun speakerIndex(speakerId: String): Int =
        participants.indexOfFirst { it.id == speakerId }.coerceAtLeast(0)

    /**
     * JSON ровно в формате из требований хакатона:
     * {"transcript":[{"speaker_id","start_ms","end_ms","text"}]}.
     */
    fun toJson(): String {
        val transcript = JSONArray()
        utterances.forEach { utterance ->
            transcript.put(
                JSONObject()
                    .put("speaker_id", utterance.speakerId)
                    .put("start_ms", utterance.startMs)
                    .put("end_ms", utterance.endMs)
                    .put("text", utterance.text),
            )
        }
        return JSONObject().put("transcript", transcript).toString(2)
    }
}

fun Utterance.timeRange(): String = "${formatClock(startMs)}–${formatClock(endMs)}"

fun speakersLabel(count: Int): String = when (count % 100) {
    11, 12, 13, 14 -> "$count говорящих"
    else -> when (count % 10) {
        1 -> "$count говорящий"
        2, 3, 4 -> "$count говорящих"
        else -> "$count говорящих"
    }
}

fun utterancesLabel(count: Int): String = when (count % 100) {
    11, 12, 13, 14 -> "$count реплик"
    else -> when (count % 10) {
        1 -> "$count реплика"
        2, 3, 4 -> "$count реплики"
        else -> "$count реплик"
    }
}
