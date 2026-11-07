package ru.mephi.tikho.data

import ru.mephi.tikho.model.AudioItem
import ru.mephi.tikho.model.Recording
import ru.mephi.tikho.model.Speaker
import ru.mephi.tikho.model.Utterance

/**
 * Демонстрационные диалоги для UI-прототипа.
 *
 * Когда появится локальная модель, [DemoData] заменится реальными
 * расшифровками — экраны уже работают с участниками и репликами.
 */
object DemoData {

    val anna = Speaker(id = "speaker_1", name = "Анна")
    val mikhail = Speaker(id = "speaker_2", name = "Михаил")
    val olga = Speaker(id = "speaker_3", name = "Ольга")

    val demoSpeakers = listOf(anna, mikhail, olga)

    val demoAudio = AudioItem(
        id = "demo-plan",
        fileName = "План на неделю.wav",
        displayName = "План на неделю",
        durationMillis = 384_000L, // 06:24
        sizeLabel = "5,9 МБ",
        language = "Русский",
    )

    val demoUtterances = listOf(
        Utterance(
            speakerId = anna.id,
            startMs = 0L,
            endMs = 11_000L,
            text = "Привет! Давайте начнём с главного: что показываем на демо?",
        ),
        Utterance(
            speakerId = mikhail.id,
            startMs = 14_000L,
            endMs = 29_000L,
            text = "Импорт записи и автоматическое разделение говорящих — это уже сильный сценарий.",
        ),
        Utterance(
            speakerId = olga.id,
            startMs = 32_000L,
            endMs = 47_000L,
            text = "Тогда сразу добавим таймкоды и переход к нужной реплике.",
        ),
        Utterance(
            speakerId = anna.id,
            startMs = 50_000L,
            endMs = 67_000L,
            text = "Согласна. И экспорт в JSON — жюри будет считать WER и RTF.",
        ),
        Utterance(
            speakerId = mikhail.id,
            startMs = 222_000L,
            endMs = 240_000L,
            text = "На экране обработки покажем этапы: распознавание, диаризация, сборка текста.",
        ),
        Utterance(
            speakerId = olga.id,
            startMs = 245_000L,
            endMs = 264_000L,
            text = "И не забываем: всё должно работать офлайн, без облака.",
        ),
    )

    val demoRecording = Recording(
        id = "plan-nedelyu",
        title = "План на неделю",
        fileName = demoAudio.fileName,
        dateLabel = "Сегодня, 09:41",
        durationLabel = demoAudio.durationLabel,
        durationMillis = demoAudio.durationMillis,
        language = demoAudio.language,
        participants = demoSpeakers,
        utterances = demoUtterances,
    )

    val recentRecordings = listOf(
        demoRecording.copy(
            id = "meeting",
            title = "Планёрка команды",
            fileName = "Планёрка команды.m4a",
            dateLabel = "Сегодня, 09:12",
            durationLabel = "06:24",
            durationMillis = 384_000L,
        ),
        demoRecording.copy(
            id = "project-ideas",
            title = "Идеи для проекта",
            fileName = "Идеи для проекта.m4a",
            dateLabel = "Вчера",
            durationLabel = "12:40",
            durationMillis = 760_000L,
        ),
        demoRecording.copy(
            id = "design-lecture",
            title = "Лекция о дизайне",
            fileName = "Лекция о дизайне.m4a",
            dateLabel = "4 ноября",
            durationLabel = "38:10",
            durationMillis = 2_290_000L,
        ),
    )
}
