package ru.mephi.tikho.ui

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.mephi.tikho.data.TranscriptionEngine
import ru.mephi.tikho.data.bridge.RustTranscriptionEngine
import ru.mephi.tikho.data.todayLabel
import ru.mephi.tikho.model.AudioItem
import ru.mephi.tikho.model.Recording
import ru.mephi.tikho.model.Speaker
import ru.mephi.tikho.model.Utterance
import java.util.UUID

/**
 * Единственное состояние приложения: экран, список диалогов и прогресс
 * текущей расшифровки.
 */
class TikhoViewModel(application: Application) : AndroidViewModel(application) {

    private val engine: TranscriptionEngine = RustTranscriptionEngine(application)

    var screen by mutableStateOf<Screen>(Screen.Library)
        private set

    val recordings = mutableStateListOf<Recording>()

    var progress by mutableFloatStateOf(0f)
        private set

    var visibleUtterances by mutableStateOf<List<Utterance>>(emptyList())
        private set

    /** null — обработка идёт; иначе человекочитаемая ошибка на экране обработки. */
    var processingError by mutableStateOf<String?>(null)
        private set

    private var transcriptionJob: Job? = null

    fun openLibrary() {
        screen = Screen.Library
    }

    fun openRecorder() {
        screen = Screen.Record
    }

    fun openRecording(recording: Recording) {
        screen = Screen.Result(recording)
    }

    fun openReview(audio: AudioItem) {
        screen = Screen.Review(audio)
    }

    fun startTranscription(audio: AudioItem) {
        transcriptionJob?.cancel()
        progress = 0f
        visibleUtterances = emptyList()
        processingError = null
        screen = Screen.Processing(audio)

        transcriptionJob = viewModelScope.launch {
            try {
                val utterances = engine.transcribe(audio) { update ->
                    withContext(Dispatchers.Main.immediate) {
                        progress = update.fraction
                        visibleUtterances = update.visibleUtterances
                    }
                }

                if (utterances.isEmpty()) {
                    processingError = "В записи не найдено речи. Попробуйте другой файл."
                    return@launch
                }

                val recording = Recording(
                    id = UUID.randomUUID().toString(),
                    title = audio.displayName,
                    fileName = audio.fileName,
                    dateLabel = todayLabel(),
                    durationLabel = audio.durationLabel,
                    durationMillis = audio.durationMillis,
                    language = audio.language,
                    participants = participantsFor(utterances),
                    utterances = utterances,
                    uriString = audio.uriString,
                )
                recordings.removeAll { it.id == recording.id }
                recordings.add(0, recording)
                screen = Screen.Result(recording)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                Log.e(TAG, "Transcription failed", error)
                processingError = "Не удалось расшифровать запись. Проверьте файл и попробуйте ещё раз."
            }
        }
    }

    fun retryTranscription() {
        val audio = (screen as? Screen.Processing)?.audio ?: return
        startTranscription(audio)
    }

    fun cancelTranscription() {
        transcriptionJob?.cancel()
        processingError = null
        val processing = screen as? Screen.Processing ?: return
        screen = Screen.Review(processing.audio)
    }

    fun deleteRecording(recording: Recording) {
        recordings.removeAll { it.id == recording.id }
        if ((screen as? Screen.Result)?.recording?.id == recording.id) {
            screen = Screen.Library
        }
    }

    fun back() {
        when (val current = screen) {
            Screen.Library -> Unit
            Screen.Record -> screen = Screen.Library
            is Screen.Review -> screen = Screen.Library
            is Screen.Processing -> cancelTranscription()
            is Screen.Result -> screen = Screen.Library
        }
    }

    override fun onCleared() {
        transcriptionJob?.cancel()
        super.onCleared()
    }

    private fun participantsFor(utterances: List<Utterance>): List<Speaker> =
        utterances
            .map { it.speakerId }
            .distinct()
            .mapIndexed { index, speakerId ->
                val number = speakerId.removePrefix("speaker_").toIntOrNull() ?: (index + 1)
                Speaker(id = speakerId, name = "Говорящий $number")
            }

    private companion object {
        const val TAG = "TikhoViewModel"
    }
}
